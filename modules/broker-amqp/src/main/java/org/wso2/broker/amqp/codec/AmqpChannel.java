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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.AckData;
import org.wso2.broker.amqp.AmqpConsumer;
import org.wso2.broker.amqp.AmqpDeliverMessage;
import org.wso2.broker.amqp.AmqpException;
import org.wso2.broker.common.data.types.FieldTable;
import org.wso2.broker.common.data.types.ShortString;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Consumer;
import org.wso2.broker.core.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AMQP channel representation.
 */
public class AmqpChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpChannel.class);

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
     * value of this represents the <b>last</b> tag sent out.
     */
    private AtomicLong deliveryTagGenerator = new AtomicLong(0);

    /**
     * Used to get the ack data matching to a delivery id.
     */
    private UnackedMessageMap unackedMessageMap = new UnackedMessageMap();

    /**
     * Indicate if channel is ready to consume messages.
     */
    private AtomicBoolean flow = new AtomicBoolean(true);

    /**
     * Indicate if unack count is within the prefetch limit.
     */
    private AtomicBoolean hasRoom = new AtomicBoolean(true);

    /**
     * List of messages blocked due to flow being disabled.
     */
    private List<AmqpDeliverMessage> deliveryPendingMessages = new ArrayList<>();

    /**
     * Max window size
     */
    private int prefetchCount;

    public AmqpChannel(Broker broker, int channelId) {
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
        ShortString tag = consumerTag;
        if (tag.isEmpty()) {
            tag = ShortString.parseString("sgen" + getNextConsumerTag());
        }
        AmqpConsumer amqpConsumer = new AmqpConsumer(ctx, this, queueName.toString(), tag, exclusive);
        consumerMap.put(consumerTag, amqpConsumer);
        broker.addConsumer(amqpConsumer);
        return tag;
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
        AckData ackData = unackedMessageMap.remove(deliveryTag);
        if (ackData != null) {
            ackData.getMessage().release();
            broker.acknowledge(ackData.getQueueName(), ackData.getMessage().getMetadata().getInternalId());
        } else {
            LOGGER.warn("Could not find a matching ack data for acking the delivery tag " + deliveryTag);
        }
    }

    public int getNextConsumerTag() {
        return consumerTagGenerator.incrementAndGet();
    }

    public long getNextDeliveryTag() {
        return deliveryTagGenerator.incrementAndGet();
    }

    /**
     * Getter for channelId.
     */
    public int getChannelId() {
        return channelId;
    }

    public void recordMessageDelivery(long deliveryTag, AckData ackData) {
        unackedMessageMap.put(deliveryTag, ackData);
    }

    public void reject(long deliveryTag, boolean requeue) {
        AckData ackData = unackedMessageMap.remove(deliveryTag);
        if (ackData != null) {
            Message message = ackData.getMessage();
            if (requeue) {
                message.setRedeliver();
                broker.requeue(ackData.getQueueName(), message);
            } else {
                message.release();
                LOGGER.debug("Dropping message for delivery tag {}", deliveryTag);
            }
        } else {
            LOGGER.warn("Could not find a matching ack data for rejecting the delivery tag " + deliveryTag);
        }
    }

    /**
     * Get all the unacknowledged messages and clear the unackedMessageMap.
     *
     * @return all unacknowledged messages
     */
    public Collection<AckData> recover() {
        return unackedMessageMap.clear();
    }

    public void rejectAll() {
        Collection<AckData> entries = unackedMessageMap.clear();
        for (AckData ackData : entries) {
            Message message = ackData.getMessage();
            message.setRedeliver();
            broker.requeue(ackData.getQueueName(), message);
        }
    }

    public void setFlow(boolean active) {
        flow.set(active);
    }

    /**
     * Channel is ready to deliver messages to clients.
     *
     * @return true if messages can be delivered through the channel, false otherwise
     */
    public boolean isReady() {
        return flow.get() && hasRoom.get();
    }

    /**
     * Indicate if client enforced flow control is enabled
     *
     * @return true if flow is enabled. false otherwise
     */
    public boolean isFlowEnabled() {
        return flow.get();
    }

    public void hold(AmqpDeliverMessage deliverMessage) {
        deliveryPendingMessages.add(deliverMessage);
    }

    public List<AmqpDeliverMessage> getPendingMessages() {
        List<AmqpDeliverMessage> pendingMessages = new ArrayList<>(deliveryPendingMessages);
        deliveryPendingMessages.clear();
        return pendingMessages;
    }

    public void setPrefetchCount(int prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    /**
     * Data-structure to handle unacknowledge messages. This class will update the has room variable depending on the
     * number of messages in the unackedMessageMap.
     */
    private class UnackedMessageMap {
        private Map<Long, AckData> unackedMessageMap = new LinkedHashMap<>();

        AckData remove(long deliveryTag) {
            AckData ackData = unackedMessageMap.remove(deliveryTag);

            if (!hasRoom.get() && unackedMessageMap.size() < prefetchCount) {
                hasRoom.set(true);
            }

            return ackData;
        }

        void put(long deliveryTag, AckData ackData) {
            unackedMessageMap.put(deliveryTag, ackData);

            if (hasRoom.get() && unackedMessageMap.size() >= prefetchCount) {
                hasRoom.set(false);
            }

        }

        Collection<AckData> clear() {
            Collection<AckData> entries = new ArrayList<>(unackedMessageMap.values());
            unackedMessageMap.clear();
            hasRoom.set(true);
            return entries;
        }
    }
}
