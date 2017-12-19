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
import org.wso2.broker.amqp.AckData;
import org.wso2.broker.amqp.AmqpConsumer;
import org.wso2.broker.amqp.AmqpException;
import org.wso2.broker.common.data.types.FieldTable;
import org.wso2.broker.common.data.types.ShortString;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Consumer;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AMQP channel representation.
 */
public class AmqpChannel {

    private final Broker broker;

    private final int channelId;

    private final Map<ShortString, AmqpConsumer> consumerMap;

    private final InMemoryMessageAggregator messageAggregator;

    /**
     * This tag is unique per subscription to a queue. The server returns this in response
     * to a basic.consume request.
     */
    private AtomicInteger consumerTagGenerator = new AtomicInteger(0);

    /**
     * The delivery tag is unique per channel. This is pre-incremented before putting into the deliver frame so that
     * value of this represents the <b>last</b> tag sent out
     */
    private AtomicLong deliveryTagGenerator = new AtomicLong(0);

    /**
     * Used to get the ack data matching to a delivery id.
     */
    private Map<Long, AckData> unackedMessageMap = new HashMap<>();

    AmqpChannel(Broker broker, int channelId) {
        this.broker = broker;
        this.channelId = channelId;
        this.consumerMap = new HashMap<>();
        this.messageAggregator = new InMemoryMessageAggregator(broker);
    }

    public void declareExchange(String exchangeName, String exchangeType,
                                boolean passive, boolean durable) throws BrokerException {
        broker.createExchange(exchangeName, exchangeType, passive, durable);
    }

    public void declareQueue(ShortString queue, boolean passive,
                             boolean durable, boolean autoDelete) throws BrokerException {
        broker.createQueue(queue.toString(), passive, durable, autoDelete);
    }

    public void bind(ShortString queue, ShortString exchange,
                     ShortString routingKey, FieldTable arguments) throws BrokerException {
        broker.bind(queue.toString(), exchange.toString(), routingKey.toString(), arguments);
    }

    public ShortString consume(ShortString queueName, ShortString consumerTag, boolean exclusive,
                               ChannelHandlerContext ctx) throws BrokerException {
        String tag = consumerTag.toString();
        if (tag.isEmpty()) {
            tag = "sgen" + getNextConsumerTag();
        }
        AmqpConsumer amqpConsumer = new AmqpConsumer(ctx, this, queueName.toString(), tag, exclusive);
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

    public InMemoryMessageAggregator getMessageAggregator() {
        return messageAggregator;
    }

    public void acknowledge(long deliveryTag, boolean multiple) {
        //TODO handle multiple
        AckData ackData = unackedMessageMap.get(deliveryTag);
        broker.acknowledge(ackData.getQueueName(), ackData.getMessage().getMetadata().getInternalId());
    }

    public int getNextConsumerTag() {
        return consumerTagGenerator.incrementAndGet();
    }

    public long getNextDeliveryTag() {
        return deliveryTagGenerator.incrementAndGet();
    }

    /**
     * Getter for channelId
     */
    public int getChannelId() {
        return channelId;
    }

    public void recordMessageDelivery(long deliveryTag, AckData ackData) {
        unackedMessageMap.put(deliveryTag, ackData);
    }
}
