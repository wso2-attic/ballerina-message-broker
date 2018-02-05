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

package org.wso2.broker.core;

import org.wso2.broker.core.metrics.BrokerMetricManager;
import org.wso2.broker.core.queue.DbBackedQueueImpl;
import org.wso2.broker.core.queue.MemQueueImpl;
import org.wso2.broker.core.store.dao.SharedMessageStore;

/**
 * Factory for creating queue handler objects.
 */
public class QueueHandlerFactory {
    private final SharedMessageStore sharedMessageStore;
    private final BrokerMetricManager metricManager;

    public QueueHandlerFactory(SharedMessageStore sharedMessageStore, BrokerMetricManager metricManager) {
        this.sharedMessageStore = sharedMessageStore;
        this.metricManager = metricManager;
    }

    /**
     * Create a durable queue handler with the give arguments.
     *
     * @param queueName  name of the queue
     * @param autoDelete true if auto deletable
     * @return QueueHandler object
     * @throws BrokerException if cannot create queue handler
     */
    QueueHandler createDurableQueueHandler(String queueName, boolean autoDelete) throws BrokerException {
        Queue queue = new DbBackedQueueImpl(queueName, autoDelete, sharedMessageStore);
        return new QueueHandler(queue, metricManager);
    }

    /**
     * Create a non durable queue handler with the give arguments.
     *
     * @param queueName  name of the queue
     * @param capacity   max capacity
     * @param autoDelete true if auto deletable
     * @return QueueHandler object
     */
    QueueHandler createNonDurableQueueHandler(String queueName, int capacity, boolean autoDelete) {
        Queue queue = new MemQueueImpl(queueName, capacity, autoDelete);
        return new QueueHandler(queue, metricManager);
    }

}
