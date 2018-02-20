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

package io.ballerina.messaging.broker.core.metrics;

import io.ballerina.messaging.broker.core.Broker;
import org.wso2.carbon.metrics.core.Counter;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Timer;
import org.wso2.carbon.metrics.core.Timer.Context;

/**
 * Default implementation of {@link BrokerMetricManager}.
 */
public class DefaultBrokerMetricManager implements BrokerMetricManager {
    private final Meter totalPublishedCounter;
    private final Counter totalEnqueueCounter;
    private final Meter totalAckCounter;
    private final Timer messageWriteTimer;
    private final Timer messageDeleteTimer;
    private final Timer messageReadTimer;

    public DefaultBrokerMetricManager(MetricService metrics) {
        totalPublishedCounter = metrics.meter(MetricService.name(Broker.class, "node", "totalPublished"), Level.INFO);
        totalAckCounter = metrics.meter(MetricService.name(Broker.class, "node", "totalAcknowledged"), Level.INFO);
        totalEnqueueCounter = metrics.counter(MetricService.name(Broker.class, "node", "totalInMemoryMessages"),
                                              Level.INFO);
        messageWriteTimer = metrics.timer(MetricService.name(Broker.class, "node", "messageWrite"), Level.INFO);
        messageDeleteTimer = metrics.timer(MetricService.name(Broker.class, "node", "messageDelete"), Level.INFO);
        messageReadTimer = metrics.timer(MetricService.name(Broker.class, "node", "messageRead"), Level.INFO);
    }

    @Override
    public void markPublish() {
        totalPublishedCounter.mark();
    }

    @Override
    public void addInMemoryMessage() {
        totalEnqueueCounter.inc();
    }

    @Override
    public void removeInMemoryMessage() {
        totalEnqueueCounter.dec();
    }

    @Override
    public void markAcknowledge() {
        totalAckCounter.mark();
    }

    @Override
    public Context startMessageWriteTimer() {
        return messageWriteTimer.start();
    }

    @Override
    public Context startMessageDeleteTimer() {
        return messageDeleteTimer.start();
    }

    @Override
    public Context startMessageReadTimer() {
        return messageReadTimer.start();
    }
}
