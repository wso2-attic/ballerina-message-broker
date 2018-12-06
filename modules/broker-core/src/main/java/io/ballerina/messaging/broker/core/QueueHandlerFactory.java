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

import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.metrics.BrokerMetricManager;
import io.ballerina.messaging.broker.eventing.EventSync;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Factory for creating queue handler objects.
 */
public abstract class QueueHandlerFactory {
    private List<Integer> commonLimits;
    private EventSync eventSync;
    private List<Integer> totalLimits = new ArrayList<>();
    QueueHandlerFactory(BrokerCoreConfiguration.QueueEvents queueEventConfiguration, EventSync eventSync) {
        this.eventSync = eventSync;
        if (Objects.nonNull(this.eventSync)) {
            commonLimits = queueEventConfiguration.getCommonLimits();
            if (Objects.nonNull(commonLimits)) {
                totalLimits.addAll(commonLimits);
            }
        }
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
    public abstract QueueHandler createDurableQueueHandler(String queueName,
                                                           boolean autoDelete,
                                                           FieldTable arguments)
            throws BrokerException;

    /**
     * Create a non durable queue handler with the give arguments.
     *
     * @param queueName  name of the queue
     * @param autoDelete true if auto deletable
     * @param  arguments arguments to modify the queue
     * @return QueueHandlerImpl object
     */
    public abstract QueueHandler createNonDurableQueueHandler(String queueName,
                                                              boolean autoDelete,
                                                              FieldTable arguments);

    /**
     * Create a observable or a non observable queue handler with the give arguments.
     *
     * @param queue queue which the queue handler handles
     * @param metricManager handles metrics in the queue
     * @param arguments arguments to modify the queue
     * @param queueEventConfiguration queue event configuration to modify queue events
     * @return QueueHandlerImpl object
     */
    QueueHandler createQueueHandler(Queue queue,
                                    BrokerMetricManager metricManager, FieldTable arguments,
                                    BrokerCoreConfiguration.QueueEvents queueEventConfiguration) {
        if (Objects.nonNull(this.eventSync)) {

            List<BrokerCoreConfiguration.QueueEvents.QueueLimitEvent> queueLimits = queueEventConfiguration.getQueues();

            if (Objects.nonNull(queueLimits)) {
                for (BrokerCoreConfiguration.QueueEvents.QueueLimitEvent queueConfig : queueLimits) {
                    if (queueConfig.getName().equals(queue.getName())) {
                        totalLimits.addAll(queueConfig.getLimits());
                    }
                }
            }

            if (Objects.nonNull(commonLimits)) {
                totalLimits.addAll(commonLimits);
            }

            if (Objects.nonNull(arguments)) {
                totalLimits.addAll(checkArgumentEvents(arguments));
            }
            Queue observableQueue = new ObservableQueue(queue, eventSync, totalLimits);

            return new ObservableQueueHandlerImpl(observableQueue, metricManager, eventSync, totalLimits);
        } else {
            return new QueueHandlerImpl(queue, metricManager);
        }
    }

    private List<Integer> checkArgumentEvents(FieldTable arguments) {
        List<Integer> argumentLimits = new ArrayList<>();
        FieldValue limits = arguments.getValue(ShortString.parseString("x-queue-limits"));
        if (Objects.nonNull(limits)) {
            String[] limitArray = limits.toString().replaceAll("\\s", "").split(",");
            for (String key : limitArray) {
                int value = Integer.parseInt(key);
                argumentLimits.add(value);
            }
        }
        return  argumentLimits;
    }
}
