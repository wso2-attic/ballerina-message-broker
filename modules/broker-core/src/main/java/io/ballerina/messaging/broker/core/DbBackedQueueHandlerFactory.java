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

import io.ballerina.messaging.broker.common.EventSync;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.metrics.BrokerMetricManager;
import io.ballerina.messaging.broker.core.queue.DbBackedQueueImpl;
import io.ballerina.messaging.broker.core.queue.MemQueueImpl;
import io.ballerina.messaging.broker.core.queue.QueueBufferFactory;
import io.ballerina.messaging.broker.core.store.DbMessageStore;

/**
 * DB backed factory for creating queue handler objects.
 */
public class DbBackedQueueHandlerFactory extends QueueHandlerFactory {
    private final DbMessageStore dbMessageStore;
    private final BrokerMetricManager metricManager;
    private final int nonDurableQueueMaxDepth;
    private QueueBufferFactory queueBufferFactory;
    private final BrokerCoreConfiguration.QueueEvents queueEventConfiguration;

    public DbBackedQueueHandlerFactory(DbMessageStore dbMessageStore, BrokerMetricManager metricManager,
                                       BrokerCoreConfiguration configuration, EventSync eventSync) {
        super(configuration.getEventConfig().getQueueEvents(), eventSync);
        this.dbMessageStore = dbMessageStore;
        this.metricManager = metricManager;
        nonDurableQueueMaxDepth = Integer.parseInt(configuration.getNonDurableQueueMaxDepth());
        queueBufferFactory = new QueueBufferFactory(configuration);
        this.queueEventConfiguration = configuration.getEventConfig().getQueueEvents();
    }
    /**
     * Create a durable queue handler with the give arguments.
     *
     * @param queueName  name of the queue
     * @param autoDelete true if auto deletable
     * @param arguments arguments to modify the queue
     * @return QueueHandlerImpl object
     * @throws BrokerException if cannot create queue handler
     */
    public QueueHandler createDurableQueueHandler(String queueName, boolean autoDelete, FieldTable arguments)
            throws BrokerException {
        Queue queue = new DbBackedQueueImpl(queueName, autoDelete, dbMessageStore, queueBufferFactory);
        return createQueueHandler(queue, this.metricManager, arguments, queueEventConfiguration);
    }

    /**
     * Create a non durable queue handler with the give arguments.
     *
     * @param queueName  name of the queue
     * @param autoDelete true if auto deletable
     * @param arguments arguments to modify the queue
     * @return QueueHandlerImpl object
     */
    public QueueHandler createNonDurableQueueHandler(String queueName, boolean autoDelete,
                                                     FieldTable arguments) {
        Queue queue = new MemQueueImpl(queueName, nonDurableQueueMaxDepth, autoDelete);
        return createQueueHandler(queue, this.metricManager, arguments, queueEventConfiguration);
    }
}
