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
 */

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.EventSync;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Represents an Consumer which trigger events for the broker.
 */
public class ObservableConsumer extends Consumer {
    private Consumer consumer;
    private EventSync eventSync;

    ObservableConsumer(Consumer consumer, EventSync eventSync) {
        this.consumer = consumer;
        this.eventSync = eventSync;
    }

    @Override
    protected void send(Message message) throws BrokerException {
        consumer.send(message);
    }

    @Override
    public String getQueueName() {
        return consumer.getQueueName();
    }

    @Override
    protected void close() throws BrokerException {
        publishConsumerEvent(this.consumer);
        consumer.close();
    }

    @Override
    public boolean isExclusive() {
        return consumer.isExclusive();
    }

    @Override
    public boolean isReady() {
        return consumer.isReady();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ObservableConsumer) {
            return consumer.getId() == ((ObservableConsumer) obj).consumer.getId();
        }
        if (obj instanceof Consumer) {
            return consumer.getId() == ((Consumer) obj).getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(consumer.getId());
    }
    @Override
    public Properties getTransportProperties() {
        return consumer.getTransportProperties();
    }

    private void publishConsumerEvent(Consumer consumer) {
        Map<String, String> properties = new HashMap<>();
        properties.put("consumerID", String.valueOf(consumer.getId()));
        properties.put("queueName", consumer.getQueueName());
        properties.put("ready", String.valueOf(consumer.isReady()));
        properties.put("exclusive", String.valueOf(consumer.isExclusive()));
        eventSync.publish("consumer.removed", properties);
    }
}
