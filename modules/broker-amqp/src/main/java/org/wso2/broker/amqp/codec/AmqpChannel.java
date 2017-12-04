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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.amqp.codec;

import io.netty.channel.ChannelHandlerContext;
import org.wso2.broker.amqp.AmqpConsumer;
import org.wso2.broker.amqp.AmqpException;
import org.wso2.broker.amqp.codec.data.ShortString;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Consumer;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AMQP channel representation
 */
public class AmqpChannel {

    private final Broker broker;

    private final int channelId;

    private final Map<ShortString, AmqpConsumer> consumerMap;

    private final InboundMessageHandler inboundMessageHandler;

    AmqpChannel(Broker broker, int channelId) {
        this.broker = broker;
        this.channelId = channelId;
        this.consumerMap = new HashMap<>();
        this.inboundMessageHandler = new InboundMessageHandler(broker);
    }

    public void declareExchange(String exchangeName, String exchangeType,
                                boolean passive, boolean durable) throws BrokerException {
        broker.createExchange(exchangeName, exchangeType, passive, durable);
    }

    public void declareQueue(ShortString queue, boolean passive,
                             boolean durable, boolean autoDelete) throws BrokerException {
        broker.createQueue(queue.toString(), passive, durable, autoDelete);
    }

    public void bind(ShortString queue, ShortString exchange, ShortString routingKey) throws BrokerException {
        broker.bind(queue.toString(), exchange.toString(), routingKey.toString());
    }

    public ShortString consume(ShortString queueName, ShortString consumerTag, boolean exclusive,
                               ChannelHandlerContext ctx) throws BrokerException {
        String tag = consumerTag.toString();
        if (tag.isEmpty()) {
            tag = UUID.randomUUID().toString();
        }
        AmqpConsumer amqpConsumer = new AmqpConsumer(ctx, channelId, queueName.toString(), tag, exclusive);
        consumerMap.put(consumerTag, amqpConsumer);
        broker.addConsumer(amqpConsumer);
        return new ShortString(tag.length(), tag.getBytes(StandardCharsets.UTF_8));
    }

    public void close() {
        for (Consumer consumer : consumerMap.values()) {
            broker.removeConsumer(consumer);
        }
    }

    public void cancelConsumer(ShortString consumerTag) throws AmqpException {
        AmqpConsumer amqpConsumer = consumerMap.get(consumerTag);
        if (amqpConsumer != null) {
            broker.removeConsumer(amqpConsumer);
        } else {
            throw new AmqpException("Invalid Consumer tag [ " + consumerTag + " ] for the channel: " + channelId);
        }
    }

    public InboundMessageHandler getInboundMessageHandler() {
        return inboundMessageHandler;
    }
}
