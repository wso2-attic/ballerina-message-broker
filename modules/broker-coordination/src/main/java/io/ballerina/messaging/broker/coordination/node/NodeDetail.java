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
 * Hold information related to a node to be used with coordination.
 */
public class NodeDetail {

    /**
     * Node ID of the node.
     */
    private final String nodeId;

    /**
     * Variable to indicate if the node is the coordinator node.
     */
    private final boolean isCoordinator;

    /**
     * Constructor for node details.
     *
     * @param nodeId        the node ID of the node
     * @param isCoordinator whether the node represented by the node details is the coordinator.
     */
    public NodeDetail(String nodeId, boolean isCoordinator) {
        this.nodeId = nodeId;
        this.isCoordinator = isCoordinator;
    }

    /**
     * Getter method for the node ID.
     *
     * @return node ID
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Retrieve whether the node represented by the node details is the coordinator.
     *
     * @return true if this node is the coordinator
     */
    public boolean isCoordinator() {
        return isCoordinator;
    }

}
