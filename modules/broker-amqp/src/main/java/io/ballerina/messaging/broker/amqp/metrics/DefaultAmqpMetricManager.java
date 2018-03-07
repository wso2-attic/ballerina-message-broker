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

package io.ballerina.messaging.broker.amqp.metrics;

import io.ballerina.messaging.broker.amqp.Server;
import org.wso2.carbon.metrics.core.Counter;
import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.MetricService;

/**
 * Default implementation of {@link AmqpMetricManager}.
 */
public class DefaultAmqpMetricManager implements AmqpMetricManager {

    private final Counter totalChannelCounter;
    private final Counter totalConnectionCounter;
    private final Counter totalConsumerCounter;
    private final Meter rejectMeter;

    public DefaultAmqpMetricManager(MetricService metrics) {
        totalChannelCounter = metrics.counter(MetricService.name(Server.class, "node", "totalChannels"), Level.INFO);
        totalConnectionCounter = metrics.counter(MetricService.name(Server.class, "node", "totalConnections"),
                                                 Level.INFO);
        totalConsumerCounter = metrics.counter(MetricService.name(Server.class, "node", "totalConsumers"), Level.INFO);
        rejectMeter = metrics.meter(MetricService.name(Server.class, "node", "messageRejects"), Level.INFO);
    }

    @Override
    public void incrementChannelCount() {
        totalChannelCounter.inc();
    }

    @Override
    public void decrementChannelCount() {
        totalChannelCounter.dec();
    }

    @Override
    public void incrementConnectionCount() {
        totalConnectionCounter.inc();
    }

    @Override
    public void decrementConnectionCount() {
        totalConnectionCounter.dec();
    }

    @Override
    public void incrementConsumerCount() {
        totalConsumerCounter.inc();
    }

    @Override
    public void decrementConsumerCount() {
        totalConsumerCounter.dec();
    }

    @Override
    public void markReject() {
        rejectMeter.mark();
    }
}
