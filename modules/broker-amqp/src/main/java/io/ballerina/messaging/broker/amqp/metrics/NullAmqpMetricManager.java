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

/**
 * Null object implementation for {@link AmqpMetricManager}.
 */
public class NullAmqpMetricManager implements AmqpMetricManager {
    @Override
    public void incrementChannelCount() {
        // do nothing
    }

    @Override
    public void decrementChannelCount() {
        // do nothing
    }

    @Override
    public void incrementConnectionCount() {
        // do nothing
    }

    @Override
    public void decrementConnectionCount() {
        // do nothing
    }

    @Override
    public void incrementConsumerCount() {
        // do nothing
    }

    @Override
    public void decrementConsumerCount() {
        // do nothing
    }

    @Override
    public void markReject() {
        // do nothing
    }
}
