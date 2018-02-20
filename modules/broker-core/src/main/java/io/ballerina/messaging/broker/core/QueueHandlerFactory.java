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

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.core.configuration.BrokerConfiguration;
import io.ballerina.messaging.broker.core.metrics.BrokerMetricManager;
import io.ballerina.messaging.broker.core.queue.DbBackedQueueImpl;
import io.ballerina.messaging.broker.core.queue.MemQueueImpl;
import io.ballerina.messaging.broker.core.queue.QueueBufferFactory;
import io.ballerina.messaging.broker.core.store.SharedMessageStore;

/**
 * Factory for creating queue handler objects.
 */
public class QueueHandlerFactory {
    private final SharedMessageStore sharedMessageStore;
    private final BrokerMetricManager metricManager;
    private QueueBufferFactory queueBufferFactory;

    public QueueHandlerFactory(SharedMessageStore sharedMessageStore, BrokerMetricManager metricManager,
            BrokerConfiguration configuration) {
        this.sharedMessageStore = sharedMessageStore;
        this.metricManager = metricManager;
        queueBufferFactory = new QueueBufferFactory(configuration);
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
        Queue queue = new DbBackedQueueImpl(queueName, autoDelete, sharedMessageStore, queueBufferFactory);
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
