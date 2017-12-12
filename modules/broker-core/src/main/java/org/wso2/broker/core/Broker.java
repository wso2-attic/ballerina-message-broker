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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.common.data.types.FieldTable;
import org.wso2.broker.core.configuration.BrokerConfiguration;

/**
 * Broker API class.
 */
public final class Broker {

    private static final Logger log = LoggerFactory.getLogger(Broker.class);

    private final MessagingEngine messagingEngine;

    private final BrokerConfiguration brokerConfiguration;
    
    public Broker(BrokerConfiguration configuration) {
        this.messagingEngine = new MessagingEngine(configuration);
        this.brokerConfiguration = configuration;
    }

    public void publish(Message message) throws BrokerException {
        messagingEngine.publish(message);
    }

    /**
     * Acknowledge single or a given set of messages. Removes the message from underlying queue
     *
     * @param queueName   name of the queue the relevant messages belongs to
     * @param deliveryTag delivery tag of the message sent by the broker
     * @param multiple    if true acknowledge multiple messages
     */
    public void acknowledge(String queueName, long deliveryTag, boolean multiple) {
        messagingEngine.acknowledge(queueName, deliveryTag, multiple);
    }

    /**
     * Adds a consumer for a queue. Queue will be the queue returned from {@link Consumer#getQueueName()}
     *
     * @param consumer {@link Consumer} implementation
     * @throws BrokerException throws {@link BrokerException} if unable to add the consumer
     */
    public void addConsumer(Consumer consumer) throws BrokerException {
        messagingEngine.consume(consumer);
    }

    public void removeConsumer(Consumer consumer) {
        messagingEngine.closeConsumer(consumer);
    }

    public void createExchange(String exchangeName, String type,
                               boolean passive, boolean durable) throws BrokerException {
        messagingEngine.createExchange(exchangeName, type, passive, durable);
    }

    public void deleteExchange(String exchangeName, String type, boolean ifUnused) throws BrokerException {
        messagingEngine.deleteExchange(exchangeName, type, ifUnused);
    }

    public void createQueue(String destination, boolean passive,
                            boolean durable, boolean autoDelete) throws BrokerException {
        messagingEngine.createQueue(destination, passive, durable, autoDelete);
    }

    public void deleteQueue(String queueName, boolean ifUnused, boolean ifEmpty) throws BrokerException {
        messagingEngine.deleteQueue(queueName, ifUnused, ifEmpty);
    }

    public void bind(String queueName, String exchangeName,
                     String routingKey, FieldTable arguments) throws BrokerException {
        messagingEngine.bind(queueName, exchangeName, routingKey, arguments);
    }

    public void unbind(String queueName, String exchangeName, String routingKey) throws BrokerException {
        messagingEngine.unbind(queueName, exchangeName, routingKey);
    }

    public void startMessageDelivery() {
        log.info("Starting message delivery threads.");
        messagingEngine.startMessageDelivery();
    }

    public void stopMessageDelivery() {
        log.info("Stopping message delivery threads.");
        messagingEngine.stopMessageDelivery();
    }

    public long getNextMessageId() {
        return messagingEngine.getNextMessageId();
    }
}
