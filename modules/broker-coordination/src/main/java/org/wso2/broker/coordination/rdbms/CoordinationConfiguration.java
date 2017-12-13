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

/**
 * Configuration for RDBMS HA.
 */
public class CoordinationConfiguration  {

    public static final String RDBMS_COORDINATION_CONFIG_FILE = "rdbmsCoordination.yaml";

    /**
     * Represents configuration for RDBMS coordination.
     */
    private RdbmsCoordinationConfiguration rdbmsCoordinationConfig;

    public RdbmsCoordinationConfiguration getRdbmsCoordinationConfig() {
        return rdbmsCoordinationConfig;
    }

    public void setRdbmsCoordinationConfig(RdbmsCoordinationConfiguration rdbmsCoordinationConfig) {
        this.rdbmsCoordinationConfig = rdbmsCoordinationConfig;
    }

    /**
     * Represents configuration for RDBMS coordination.
     */
    public static class RdbmsCoordinationConfiguration {

        private String nodeId;

        private int heartbeatInterval;

        private int coordinatorEntryCreationWaitTime;

        private int eventPollingInterval;

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public int getHeartbeatInterval() {
            return heartbeatInterval;
        }

        public void setHeartbeatInterval(int heartbeatInterval) {
            this.heartbeatInterval = heartbeatInterval;
        }

        public int getCoordinatorEntryCreationWaitTime() {
            return coordinatorEntryCreationWaitTime;
        }

        public void setCoordinatorEntryCreationWaitTime(int coordinatorEntryCreationWaitTime) {
            this.coordinatorEntryCreationWaitTime = coordinatorEntryCreationWaitTime;
        }

        public int getEventPollingInterval() {
            return eventPollingInterval;
        }

        public void setEventPollingInterval(int eventPollingInterval) {
            this.eventPollingInterval = eventPollingInterval;
        }

        @Override
        public String toString() {
            return "RDBMSCoordinationConfiguration [nodeID=" + nodeId
                    + ", heartbeatInterval=" + heartbeatInterval
                    + ", coordinatorEntryCreationWaitTime=" + coordinatorEntryCreationWaitTime
                    + ", eventPollingInterval=" + eventPollingInterval + "]";
        }
    }

}
