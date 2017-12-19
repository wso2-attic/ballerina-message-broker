/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.broker.coordination.rdbms;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.coordination.CoordinationException;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Class to detect membership changes and write them to the database.
 */
public class RdbmsMembershipEventingEngine {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RdbmsMembershipEventingEngine.class);

    /**
     * For communication with the database.
     */
    private RdbmsCoordinationDaoImpl coordinationDao;

    /**
     * Executor service used to run the event listening task.
     */
    private ScheduledExecutorService clusterMembershipReaderTaskScheduler;

    /**
     * Task used to get cluster events.
     */
    private MembershipListenerTask membershipListenerTask;

    /**
     * Default constructor.
     */
    public RdbmsMembershipEventingEngine(RdbmsCoordinationDaoImpl coordinationDao) {
        this.coordinationDao = coordinationDao;
    }

    /**
     * Method to start the membership listener task.
     */
    public void start(String nodeId, int eventPollingInterval) {
        //TODO: this needs to be fixed
        ThreadFactory namedThreadFactory =
                new ThreadFactoryBuilder().setNameFormat("ClusterEventReaderTask-%d").build();
        clusterMembershipReaderTaskScheduler = Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
        membershipListenerTask = new MembershipListenerTask(nodeId, coordinationDao);
        clusterMembershipReaderTaskScheduler.scheduleWithFixedDelay(membershipListenerTask, eventPollingInterval,
                eventPollingInterval, TimeUnit.MILLISECONDS);
        logger.info("RDBMS cluster event listener started.");
    }

    /**
     * Method to stop the membership listener task.
     */
    public void stop() {
        clusterMembershipReaderTaskScheduler.shutdown();
    }

    /**
     * Method to store membership events destined to be read by each node.
     *
     * @param membershipEventType the type of the membership event as an int
     * @param nodeID              the node id which triggered the event
     * @throws CoordinationException if an error occurs while storing the membership event
     */
    public void notifyMembershipEvent(List<String> nodes, MembershipEventType membershipEventType, String nodeID)
            throws CoordinationException {
        coordinationDao.storeMembershipEvent(nodes, membershipEventType.getCode(), nodeID);
    }

    /**
     * Method to add a listener to be notified of the cluster membership events.
     *
     * @param membershipListener membership listener object
     */
    public void addEventListener(RdbmsMembershipListener membershipListener) {
        membershipListenerTask.addEventListener(membershipListener);
    }

    /**
     * Method to remove a previously added listener.
     *
     * @param membershipListener membership listener object
     */
    public void removeEventListener(RdbmsMembershipListener membershipListener) {
        membershipListenerTask.removeEventListener(membershipListener);
    }

}
