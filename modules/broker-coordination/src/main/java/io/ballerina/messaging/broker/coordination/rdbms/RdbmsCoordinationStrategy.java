/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.coordination.rdbms;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.ballerina.messaging.broker.coordination.CoordinationException;
import io.ballerina.messaging.broker.coordination.CoordinationStrategy;
import io.ballerina.messaging.broker.coordination.node.NodeDetail;
import io.ballerina.messaging.broker.coordination.node.NodeHeartbeatData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * RdbmsCoordinationStrategy uses a RDBMS based approached to identify membership events related to the cluster.
 * This includes electing the coordinator and notifying member added and left events.
 */
public class RdbmsCoordinationStrategy implements CoordinationStrategy {

    /**
     * Class logger.
     */
    private Logger logger = LoggerFactory.getLogger(RdbmsCoordinationStrategy.class);

    /**
     * Time to wait before notifying others about coordinator change.
     */
    private final int coordinatorEntryCreationWaitTime;

    /**
     * Possible node states.
     *
     *               +----------+
     *     +-------->+ Election +<---------+
     *     |         +----------+          |
     *     |            |    |             |
     *     |            |    |             |
     *  +-----------+   |    |   +-------------+
     *  | Candidate +<--+    +-->+ Coordinator |
     *  +-----------+            +-------------+
     */
    private enum NodeState {
        COORDINATOR, CANDIDATE, ELECTION
    }

    /**
     * Heartbeat interval in seconds.
     */
    private final int heartBeatInterval;

    /**
     * Amount of time after which the node is assumed to have left the cluster.
     */
    private final int heartbeatMaxAge;

    /**
     * Long running coordinator election task.
     */
    private CoordinatorElectionTask coordinatorElectionTask;

    /**
     * Current state of the node.
     */
    private NodeState currentNodeState;

    /**
     * For communication with the database.
     */
    private RdbmsCoordinationDaoImpl coordinationDao;

    /**
     * Used to uniquely identify a node in the cluster.
     */
    private String localNodeId;

    /**
     * Thread executor used to run the coordination algorithm.
     */
    private final ExecutorService threadExecutor;

    /**
     * Thread executor used to expire the coordinator state.
     */
    private final ScheduledExecutorService scheduledExecutorService;

    /**
     * Listeners expecting notification on coordinator change.
     */
    private List<RdbmsCoordinationListener> coordinationListeners = new ArrayList<>();

    /**
     * Default constructor.
     *
     * @param rdbmsCoordinationDaoImpl       the RdbmsCoordinationDaoImpl to use for communication with the database
     * @param rdbmsCoordinationOptions       the configuration for RDBMS coordination
     */
    public RdbmsCoordinationStrategy(RdbmsCoordinationDaoImpl rdbmsCoordinationDaoImpl,
                                     Map<String, String> rdbmsCoordinationOptions) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("RdbmsCoordinationStrategy-%d")
                .build();
        threadExecutor = Executors.newSingleThreadExecutor(namedThreadFactory);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        if (rdbmsCoordinationOptions.get(RdbmsCoordinationConstants.HEARTBEAT_INTERVAL) != null) {
            heartBeatInterval = Integer.parseInt(
                    rdbmsCoordinationOptions.get(RdbmsCoordinationConstants.HEARTBEAT_INTERVAL));
        } else {
            heartBeatInterval = 5000;
        }
        if (rdbmsCoordinationOptions.get(RdbmsCoordinationConstants.COORDINATOR_ENTRY_CREATION_WAIT_TIME) != null) {
            coordinatorEntryCreationWaitTime = Integer.parseInt(
                    rdbmsCoordinationOptions.get(RdbmsCoordinationConstants.COORDINATOR_ENTRY_CREATION_WAIT_TIME));
        } else {
            coordinatorEntryCreationWaitTime = 3000;
        }
        localNodeId = rdbmsCoordinationOptions.get(RdbmsCoordinationConstants.NODE_IDENTIFIER);
        if (localNodeId == null) {
            localNodeId = UUID.randomUUID().toString();
        }

        // Maximum age of a heartbeat. After this much of time, the heartbeat is considered invalid and node is
        // considered to have left the cluster.
        heartbeatMaxAge = heartBeatInterval * 2;

        if (heartBeatInterval <= coordinatorEntryCreationWaitTime) {
            throw new RuntimeException("Configuration error. " + heartBeatInterval + " * 2 should be greater than " +
                    coordinatorEntryCreationWaitTime);
        }

        coordinationDao = rdbmsCoordinationDaoImpl;
    }

    /**
     * Method to be invoked once the node becomes the coordinator node.
     */
    private void becameCoordinatorNode() {
        for (RdbmsCoordinationListener rdbmsCoordinationListener : coordinationListeners) {
            scheduledExecutorService.submit(rdbmsCoordinationListener::becameCoordinatorNode);
        }
    }

    /**
     * Method to be invoked once the node loses coordinator state.
     */
    private void lostCoordinatorState() {
        for (RdbmsCoordinationListener rdbmsCoordinationListener : coordinationListeners) {
            scheduledExecutorService.submit(rdbmsCoordinationListener::lostCoordinatorState);
        }
    }

    /**
     * Method to pause the coordination strategy.
     */
    void pause() {
        /* Stops running the tasks (coordinator/standby/election) - if the node was the coordinator, the state
        expiration task will set the node state to election. */
        coordinatorElectionTask.stop();
    }

    /**
     * Method to resume the paused coordination strategy.
     */
    void resume() {
        start();
    }

    /*
    * ======================== Methods from CoordinationStrategy ============================
    */

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        setCurrentNodeState(NodeState.ELECTION);
        coordinatorElectionTask = new CoordinatorElectionTask();
        threadExecutor.execute(coordinatorElectionTask);

        // Wait until node state become Candidate/Coordinator because thrift server needs to start after that.
        int timeout = 500;
        int waitTime = 0;
        int maxWaitTime = heartbeatMaxAge * 5;
        while (currentNodeState == NodeState.ELECTION) {
            try {
                TimeUnit.MILLISECONDS.sleep(timeout);
                waitTime = waitTime + timeout;
                if (waitTime == maxWaitTime) {
                    throw new RuntimeException("Node is stuck in the ELECTION state for "
                            + waitTime + " milliseconds.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("An error occurred while waiting to get current node state.", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCoordinator() {
        return currentNodeState == NodeState.COORDINATOR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNodeIdentifierOfCoordinator() throws CoordinationException {
        return coordinationDao.getCoordinatorNodeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAllNodeIdentifiers() throws CoordinationException {
        List<NodeHeartbeatData> allNodeInformation = coordinationDao.getAllHeartBeatData();
        return getNodeIds(allNodeInformation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NodeDetail> getAllNodeDetails() throws CoordinationException {
        List<NodeDetail> nodeDetails = new ArrayList<>();
        List<NodeHeartbeatData> allHeartBeatData = coordinationDao.getAllHeartBeatData();
        String coordinatorNodeId = coordinationDao.getCoordinatorNodeId();
        for (NodeHeartbeatData nodeHeartBeatData : allHeartBeatData) {
            boolean isCoordinatorNode = coordinatorNodeId.equals(nodeHeartBeatData.getNodeId());
            nodeDetails.add(new NodeDetail(nodeHeartBeatData.getNodeId(), isCoordinatorNode));
        }
        return nodeDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        // TODO detect if started
        if (isCoordinator()) {
            try {
                coordinationDao.removeCoordinator();
            } catch (CoordinationException e) {
                logger.error("Error occurred while removing coordinator when shutting down", e);
            }
        }
        coordinatorElectionTask.stop();
        threadExecutor.shutdown();
        scheduledExecutorService.shutdown();
    }

    /**
     * Return a list of node ids from the heartbeat data list.
     *
     * @param allHeartbeatData list of heartbeat data
     * @return list of node IDs
     */
    private List<String> getNodeIds(List<NodeHeartbeatData> allHeartbeatData) {
        List<String> allNodeIds = new ArrayList<>(allHeartbeatData.size());
        for (NodeHeartbeatData nodeHeartBeatData : allHeartbeatData) {
            allNodeIds.add(nodeHeartBeatData.getNodeId());
        }
        return allNodeIds;
    }


    /**
     * Method to set listeners expecting notifications regarding changes in the coordinator.
     *
     * @param rdbmsCoordinationListener a listener to set
     */
    public void addCoordinationListener(RdbmsCoordinationListener rdbmsCoordinationListener) {
        coordinationListeners.add(rdbmsCoordinationListener);
    }

    /**
     * Method to remove a listener set expecting notifications regarding changes in the coordinator.
     *
     * @param rdbmsCoordinationListener the listener to remove
     */
    public void removeCoordinationListener(RdbmsCoordinationListener rdbmsCoordinationListener) {
        coordinationListeners.remove(rdbmsCoordinationListener);
    }

    /**
     * The main task used to run the coordination algorithm.
     */
    private class CoordinatorElectionTask implements Runnable {

        /**
         * Indicate if the task should run.
         */
        private boolean running;

        /**
         * Scheduled future for the COORDINATOR state expiration task.
         */
        private ScheduledFuture<?> scheduledFuture;

        private CoordinatorElectionTask() {
            running = true;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Current node state: " + currentNodeState);
                    }
                    switch (currentNodeState) {
                        case CANDIDATE:
                            setCurrentNodeState(performStandByTask());
                            break;
                        case COORDINATOR:
                            setCurrentNodeState(performCoordinatorTask());
                            break;
                        case ELECTION:
                            setCurrentNodeState(performElectionTask());
                            break;
                    }
                } catch (Throwable e) {
                    logger.error("Error detected while running coordination algorithm. Node became a "
                            + NodeState.ELECTION + " node", e);
                    cancelStateExpirationTask();
                    setCurrentNodeState(NodeState.ELECTION);
                }
            }
        }

        /**
         * Perform periodic task that should be done by a CANDIDATE node.
         *
         * @return next NodeState
         * @throws CoordinationException if an error occurs checking if the coordinator is valid or when removing the
         * coordinator
         * @throws InterruptedException if interrupted
         */
        private NodeState performStandByTask() throws CoordinationException, InterruptedException {
            NodeState nextState;
            updateNodeHeartBeat();
            // Read current coordinator's validity. We can improve this by returning the status (TIMED_OUT or DELETED or
            // VALID)from this call. If DELETED we do not have to check a second time.
            boolean coordinatorValid = coordinationDao.checkIfCoordinatorValid(heartbeatMaxAge);
            TimeUnit.MILLISECONDS.sleep(heartBeatInterval);
            if (coordinatorValid) {
                nextState = NodeState.CANDIDATE;
            } else {
                coordinatorValid = coordinationDao.checkIfCoordinatorValid(heartbeatMaxAge);
                if (coordinatorValid) {
                    nextState = NodeState.CANDIDATE;
                } else {
                    logger.info("Going for election since the Coordinator is invalid");
                    coordinationDao.removeCoordinator();
                    nextState = NodeState.ELECTION;
                }
            }
            return nextState;
        }

        /**
         * Try to update the heart beat entry for the local node in the DB. If the entry is deleted by the coordinator,
         * this will recreate the entry.
         *
         * @throws CoordinationException if an error occurs while updating or recreating the node heartbeat entry
         */
        private void updateNodeHeartBeat() throws CoordinationException {
            boolean heartbeatEntryExists = coordinationDao.updateNodeHeartbeat(localNodeId);
            if (!heartbeatEntryExists) {
                coordinationDao.createNodeHeartbeatEntry(localNodeId);
            }
        }

        /**
         * Perform periodic task that should be done by the coordinator node.
         *
         * @return next NodeState
         * @throws CoordinationException if an error occurs in notifying events or retrieving data from/updating the
         * database.
         * @throws InterruptedException if interrupted
         */
        private NodeState performCoordinatorTask() throws CoordinationException, InterruptedException {
            // Try to update the coordinator heartbeat
            boolean stillCoordinator = coordinationDao.updateCoordinatorHeartbeat(localNodeId);
            if (stillCoordinator) {
                resetScheduleStateExpirationTask();
                long startTime = System.currentTimeMillis();
                updateNodeHeartBeat();
                long currentTimeMillis = System.currentTimeMillis();
                List<NodeHeartbeatData> allNodeInformation = coordinationDao.getAllHeartBeatData();
                List<String> allActiveNodeIds = getNodeIds(allNodeInformation);
                List<String> newNodes = new ArrayList<>();
                List<String> removedNodes = new ArrayList<>();

                for (NodeHeartbeatData nodeHeartBeatData : allNodeInformation) {
                    long heartbeatAge = currentTimeMillis - nodeHeartBeatData.getLastHeartbeat();
                    String nodeId = nodeHeartBeatData.getNodeId();
                    if (nodeHeartBeatData.isNewNode()) {
                        newNodes.add(nodeId);
                        // update node info as read
                        coordinationDao.markNodeAsNotNew(nodeId);
                    } else if (heartbeatAge >= heartbeatMaxAge) {
                        removedNodes.add(nodeId);
                        allActiveNodeIds.remove(nodeId);
                        coordinationDao.removeNodeHeartbeat(nodeId);
                    }
                }

                for (String newNode : newNodes) {
                    logger.info("Member added " + newNode);
                }

                for (String removedNode : removedNodes) {
                    logger.info("Member removed " + removedNode);
                }

                // Reduce the time spent in updating membership events from wait time
                long endTime = System.currentTimeMillis();
                long timeToWait = heartBeatInterval - (endTime - startTime);

                if (timeToWait > 0) {
                    TimeUnit.MILLISECONDS.sleep(timeToWait);
                } else {
                    logger.warn("Sending membership events took more than the heart beat interval");
                }

                return NodeState.COORDINATOR;
            } else {
                logger.info("Going for election since Coordinator state is lost");
                cancelStateExpirationTask();
                return NodeState.ELECTION;
            }
        }

        /**
         * Perform new coordinator election task.
         *
         * @return next NodeState
         * @throws InterruptedException  if interrupted
         */
        private NodeState performElectionTask() throws InterruptedException {
            NodeState nextState;
            try {
                nextState = tryToElectSelfAsCoordinator();
            } catch (CoordinationException e) {
                logger.info("Current node became a " + NodeState.CANDIDATE + " node", e);
                nextState = NodeState.CANDIDATE;
            }
            return nextState;
        }

        /**
         * Try to elect the local node as the coordinator by creating the coordinator entry.
         *
         * @return next NodeState
         * @throws CoordinationException if an error occurs in notifying events or retrieving data from/updating the
         * database.
         * @throws InterruptedException if interrupted
         */
        private NodeState tryToElectSelfAsCoordinator() throws CoordinationException, InterruptedException {
            NodeState nextState;
            boolean electedAsCoordinator = coordinationDao.createCoordinatorEntry(localNodeId);
            if (electedAsCoordinator) {
                // backoff
                TimeUnit.MILLISECONDS.sleep(coordinatorEntryCreationWaitTime);
                boolean isCoordinator = coordinationDao.checkIsCoordinator(localNodeId);

                if (isCoordinator) {
                    coordinationDao.updateCoordinatorHeartbeat(localNodeId);
                    resetScheduleStateExpirationTask();
                    logger.info("Elected current node as the coordinator");
                    nextState = NodeState.COORDINATOR;
                } else {
                    logger.info("Election resulted in current node becoming a " + NodeState.CANDIDATE + " node");
                    nextState = NodeState.CANDIDATE;
                }
            } else {
                logger.info("Election resulted in current node becoming a " + NodeState.CANDIDATE + " node");
                nextState = NodeState.CANDIDATE;
            }
            return nextState;
        }

        /**
         * Stop coordination task.
         */
        public void stop() {
            running = false;
        }

        /**
         * Cancel the coordinator expiration task if one exists.
         */
        private void cancelStateExpirationTask() {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
                scheduledFuture = null;
            }
        }

        /**
         * Reset the coordinator expiration task. This method is called when the COORDINATOR state is reassigned.
         */
        private void resetScheduleStateExpirationTask() {
            cancelStateExpirationTask();
            scheduledFuture = scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    setCurrentNodeState(NodeState.ELECTION);
                }
            }, heartbeatMaxAge, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Method to set the new node state and notify gaining/losing coordinator state if required.
     *
     * @param newNodeState the new state of the node
     */
    private void setCurrentNodeState(NodeState newNodeState) {
        if (NodeState.COORDINATOR.equals(currentNodeState)) {
            if (NodeState.ELECTION.equals(newNodeState)) {
                lostCoordinatorState();
            }
        } else if (NodeState.ELECTION.equals(currentNodeState)) {
            if (NodeState.COORDINATOR.equals(newNodeState)) {
                becameCoordinatorNode();
            }
        }
        currentNodeState = newNodeState;
    }

}
