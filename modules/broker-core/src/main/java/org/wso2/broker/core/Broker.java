/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.core;

/**
 * Broker API class.
 */
public final class Broker {

    private final MessagingEngine messagingEngine;

    Broker() {
        this.messagingEngine = new MessagingEngine();
    }

    public void publish(Message message) throws BrokerException {
        messagingEngine.publish(message);
    }

    public void consumeFromQueue(Consumer consumer) throws BrokerException {
        messagingEngine.consume(consumer);
    }

    public void createExchange(String exchangeName, Exchange.Type type,
                               boolean passive, boolean durable) throws BrokerException {
        messagingEngine.createExchange(exchangeName, type, passive, durable);
    }

    public void deleteExchange(String exchangeName, Exchange.Type type, boolean ifUnused) throws BrokerException {
        messagingEngine.deleteExchange(exchangeName, type, ifUnused);
    }

    public void createQueue(String destination, boolean passive,
                            boolean durable, boolean autoDelete) throws BrokerException {
        messagingEngine.createQueue(destination, passive, durable, autoDelete);
    }

    public void deleteQueue(String queueName, boolean ifUnused, boolean ifEmpty) throws BrokerException {
        messagingEngine.deleteQueue(queueName, ifUnused, ifEmpty);
    }

    void bind(String queueName, String exchangeName, String routingKey) throws BrokerException {
        messagingEngine.bind(queueName, exchangeName, routingKey);
    }

    void unbind(String queueName, String exchangeName, String routingKey) throws BrokerException {
        messagingEngine.unbind(queueName, exchangeName, routingKey);
    }

    public void startMessageDelivery() {
        messagingEngine.startMessageDelivery();
    }

    public void stopMessageDelivery() {
        messagingEngine.stopMessageDelivery();
    }
}
