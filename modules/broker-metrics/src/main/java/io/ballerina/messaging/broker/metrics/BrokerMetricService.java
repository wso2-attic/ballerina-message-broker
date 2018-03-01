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

package io.ballerina.messaging.broker.metrics;

import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Metrics;

/**
 * Handles metrics server related tasks.
 */
public class BrokerMetricService {

    private final Metrics metrics;

    public BrokerMetricService(StartupContext context) {
        BrokerConfigProvider configProvider = context.getService(BrokerConfigProvider.class);
        metrics = new Metrics(new CarbonConfigAdapter(configProvider));
        context.registerService(MetricService.class, metrics.getMetricService());
    }

    public void start() {
        metrics.activate();
    }

    public void stop() {
        metrics.deactivate();
    }
}
