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

package io.ballerina.messaging.broker.core.store.dao;

import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Queue;

/**
 * Defines functionality required at persistence layer for managing {@link Queue}s.
 */
public interface QueueDao {

    /**
     * Save a Queue in persistent storage.
     * 
     * @param queue the queue
     */
    void persist(Queue queue) throws BrokerException;

    /**
     * Remove a queue from persistent storage.
     * 
     * @param queue the queue.
     */
    void delete(Queue queue) throws BrokerException;

    void retrieveAll(QueueCollector queueNameCollector) throws BrokerException;

    /**
     * Queue name collector interface to retrieve all the queues.
     */
    @FunctionalInterface
    interface QueueCollector {

        void addQueue(String name) throws BrokerException;
    }
}
