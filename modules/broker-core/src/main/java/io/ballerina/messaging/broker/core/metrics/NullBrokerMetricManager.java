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

import org.wso2.carbon.metrics.core.Timer.Context;

/**
 * Null object implementation for {@link BrokerMetricManager}.
 */
public class NullBrokerMetricManager implements BrokerMetricManager {
    private static NullContext nullContext = new NullContext();

    @Override
    public void markPublish() {
        // do nothing
    }

    @Override
    public void addInMemoryMessage() {
        // do nothing
    }

    @Override
    public void removeInMemoryMessage() {
        // do nothing
    }

    @Override
    public void markAcknowledge() {
        // do nothing
    }

    @Override
    public Context startMessageWriteTimer() {
        return nullContext;
    }

    @Override
    public Context startMessageDeleteTimer() {
        return nullContext;
    }

    @Override
    public Context startMessageReadTimer() {
        return nullContext;
    }

    /**
     * Null object representation for Timer context.
     */
    private static class NullContext implements Context {

        @Override
        public long stop() {
            return 0;
        }

        @Override
        public void close() {
            // do nothing
        }
    }
}
