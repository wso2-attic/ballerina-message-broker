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
import io.ballerina.messaging.broker.core.queue.MemQueueImpl;

/**
 * Memory backed factory for creating queue handler objects.
 */
public class MemBackedQueueHandlerFactory extends QueueHandlerFactory {
    private final BrokerMetricManager metricManager;
    private final int nonDurableQueueMaxDepth;
    private final BrokerCoreConfiguration.QueueEvents queueEventConfiguration;
    public MemBackedQueueHandlerFactory(BrokerMetricManager metricManager,
                                        BrokerCoreConfiguration configuration, EventSync eventSync) {
        super(configuration.getEventConfig().getQueueEvents(), eventSync);
        this.metricManager = metricManager;
        this.nonDurableQueueMaxDepth = Integer.parseInt(configuration.getNonDurableQueueMaxDepth());
        this.queueEventConfiguration = configuration.getEventConfig().getQueueEvents();
    }

    @Override
    public QueueHandler createDurableQueueHandler(String queueName, boolean autoDelete, FieldTable arguments) {
        return getQueueHandler(queueName, true, autoDelete, arguments);
    }

    @Override
    public QueueHandler createNonDurableQueueHandler(String queueName, boolean autoDelete,
                                                     FieldTable arguments) {
        return getQueueHandler(queueName, false, autoDelete, arguments);
    }

    private QueueHandler getQueueHandler(String queueName, boolean durable, boolean autoDelete,
                                         FieldTable arguments) {
        Queue queue = new MemQueueImpl(queueName, durable, nonDurableQueueMaxDepth, autoDelete);
        return createQueueHandler(queue, this.metricManager, arguments, queueEventConfiguration);

    }

}
