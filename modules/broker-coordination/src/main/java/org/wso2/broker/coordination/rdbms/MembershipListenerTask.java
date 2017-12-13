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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.coordination.CoordinationException;

import java.util.ArrayList;
import java.util.List;

/**
 * The task that runs periodically to detect membership change events.
 */
class MembershipListenerTask implements Runnable {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(MembershipListenerTask.class);

    /**
     * For communication with the database.
     */
    private RdbmsCoordinationDaoImpl coordinationDao;

    /**
     * Node id of the node for which the reader reads membership changes.
     */
    private String nodeID;

    /**
     * List used to hold all the registered subscribers.
     */
    private List<RdbmsMembershipListener> listeners;

    /**
     * Default Constructor.
     *
     * @param nodeId local node ID used to uniquely identify the node within cluster
     */
    MembershipListenerTask(String nodeId, RdbmsCoordinationDaoImpl coordinationDao) {
        this.nodeID = nodeId;
        this.coordinationDao = coordinationDao;
        listeners = new ArrayList<>();
    }

    /**
     * The task that is periodically run to read membership events and to notify the listeners.
     */
    @Override
    public void run() {
        try {
            // Read the membership changes from the store and notify the changes
            List<MembershipEvent> membershipEvents = readMembershipEvents();
            if (!membershipEvents.isEmpty()) {
                for (MembershipEvent event : membershipEvents) {
                    switch (event.getMembershipEventType()) {
                        case MEMBER_ADDED:
                            notifyMemberAddition(event.getMember());
                            break;
                        case MEMBER_REMOVED:
                            notifyMemberRemoval(event.getMember());
                            break;
                        case COORDINATOR_CHANGED:
                            notifyCoordinatorChangeEvent(event.getMember());
                            break;
                        default:
                            logger.error("Unknown cluster event type: " + event.getMembershipEventType());
                            break;
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No membership events to sync");
                }
            }
        } catch (Throwable e) {
            logger.warn("Error occurred while reading membership events.", e);
        }
    }

    private void notifyCoordinatorChangeEvent(String member) {
        for (RdbmsMembershipListener listener : listeners) {
            listener.coordinatorChanged(member);
        }
    }

    private void notifyMemberRemoval(String member) {
        for (RdbmsMembershipListener listener : listeners) {
            listener.memberRemoved(member);
        }
    }

    private void notifyMemberAddition(String member) {
        for (RdbmsMembershipListener listener : listeners) {
            listener.memberAdded(member);
        }
    }

    /**
     * Method to read membership events. This will read all membership events that are recorded for a particular node
     * and clear all of those events once read.
     *
     * @return list membership events
     * @throws CoordinationException if an error occurs in reading membership events
     */
    private List<MembershipEvent> readMembershipEvents() throws CoordinationException {
        return coordinationDao.readMembershipEvents(nodeID);
    }

    /**
     * Add a listener to be notified of the cluster membership events.
     *
     * @param membershipListener membership listener object
     */
    void addEventListener(RdbmsMembershipListener membershipListener) {
        listeners.add(membershipListener);
    }

    /**
     * Remove a previously added listener.
     *
     * @param membershipListener membership listener object
     */
    void removeEventListener(RdbmsMembershipListener membershipListener) {
        listeners.remove(membershipListener);
    }

}
