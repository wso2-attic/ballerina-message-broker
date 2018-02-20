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

package io.ballerina.messaging.broker.coordination;

import io.ballerina.messaging.broker.coordination.node.NodeDetail;

import java.util.List;

/**
 * The interface representing the coordination algorithm used to elect/identify the coordinator in the cluster.
 */
public interface CoordinationStrategy {

    /**
     * Used to query if the current node is the  coordinator.
     *
     * @return true if the current node is the coordinator, false otherwise
     */
    boolean isCoordinator();

    /**
     * Retrieve the node ID of the coordinator node.
     *
     * @return node ID of the coordinator
     */
    String getNodeIdentifierOfCoordinator() throws CoordinationException;

    /**
     * Retrieve IDs of all the nodes.
     *
     * @return list of member IDs
     */
    List<String> getAllNodeIdentifiers() throws CoordinationException;

    /**
     * Retrieve node details of all the nodes.
     *
     * @return list of node details
     */
    List<NodeDetail> getAllNodeDetails() throws CoordinationException;

    /**
     * Meant to be invoked when the coordination algorithm should start working. This is typically invoked during the
     * server start up.
     */
    void start();

    /**
     * Meant to be invoked when the coordination algorithm should stop working. This is typically during the server
     * shutdown.
     */
    void stop();

}
