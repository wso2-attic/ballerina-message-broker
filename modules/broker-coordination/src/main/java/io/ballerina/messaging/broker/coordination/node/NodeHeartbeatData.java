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

package io.ballerina.messaging.broker.coordination.node;

/**
 * Hold information related to a node heartbeat entry. This can be used to pass information related to node heartbeat
 * to/from the persistence layer.
 */
public class NodeHeartbeatData {

    /**
     * Node ID of the node to which heartbeat data belongs to.
     */
    private final String nodeId;

    /**
     * The last updated heartbeat value.
     */
    private final long lastHeartbeat;

    /**
     * Indicate if the node addition is already identified by the coordinator.
     */
    private final boolean isNewNode;

    /**
     * Constructor for NodeHeartbeatData.
     *
     * @param nodeId        node ID
     * @param lastHeartbeat last heartbeat received from the node
     * @param isNewNode     true if new node
     */
    public NodeHeartbeatData(String nodeId, long lastHeartbeat, boolean isNewNode) {
        this.nodeId = nodeId;
        this.lastHeartbeat = lastHeartbeat;
        this.isNewNode = isNewNode;
    }

    /**
     * Getter method for Node ID.
     *
     * @return node ID
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Getter method for last heartbeat.
     *
     * @return last heartbeat received form the node
     */
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    /**
     * Getter method for isNewNode.
     *
     * @return true if this is a new node
     */
    public boolean isNewNode() {
        return isNewNode;
    }

}
