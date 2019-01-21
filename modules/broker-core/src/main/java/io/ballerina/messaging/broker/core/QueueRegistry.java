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
 */

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;

import java.util.Collection;

/**
 * Abstract class for Queue Registry objects.
 */
public abstract class QueueRegistry {

    abstract QueueHandler getQueueHandler(String queueName);

    abstract boolean addQueue(String queueName, boolean passive, boolean durable, boolean autoDelete,
                              FieldTable arguments)
            throws BrokerException;

    abstract int removeQueue(String queueName, boolean ifUnused, boolean ifEmpty) throws BrokerException,
            ValidationException,
            ResourceNotFoundException;

    public abstract Collection<QueueHandler> getAllQueues();

    /**
     * Method to reload queues on becoming the active node.
     *
     * @throws BrokerException if an error occurs loading messages from the database
     */
    abstract void reloadQueuesOnBecomingActive() throws BrokerException;
}
