/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.core;

/**
 * Represent a binding that connects a queue to and exchange using a routing key.
 */
final class Binding {

    private final String routingKey;

    private final String queueName;

    /**
     * Creates a binding object
     *
     * @param routingKey   routingKey
     * @param queueName    queueName
     */
    Binding(String routingKey, String queueName) {
        this.routingKey = routingKey;
        this.queueName = queueName;
    }

    String getRoutingKey() {
        return routingKey;
    }

    String getQueueName() {
        return queueName;
    }
}
