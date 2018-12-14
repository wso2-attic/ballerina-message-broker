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

package io.ballerina.messaging.broker.core.eventingutil;

import io.ballerina.messaging.broker.core.Consumer;
import io.ballerina.messaging.broker.core.Message;

import java.util.Properties;

public class TestConsumer extends Consumer {

    private boolean exclusive;
    private boolean ready;
    private String queueName;

    public TestConsumer(boolean exclusive, boolean ready, String queueName) {
        this.exclusive = exclusive;
        this.ready = ready;
        this.queueName = queueName;
    }
    @Override
    protected void send(Message message) {

    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    protected void close() {
    }

    @Override
    public boolean isExclusive() {
        return exclusive;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public Properties getTransportProperties() {
        return null;
    }
}
