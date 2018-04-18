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

package io.ballerina.messaging.broker.amqp.codec;

import io.ballerina.messaging.broker.amqp.AmqpServerConfiguration;
import io.ballerina.messaging.broker.amqp.codec.flow.ChannelFlowManager;
import io.ballerina.messaging.broker.amqp.consumer.AckData;
import io.ballerina.messaging.broker.amqp.consumer.AmqpConsumer;
import io.ballerina.messaging.broker.amqp.consumer.AmqpDeliverMessage;
import io.ballerina.messaging.broker.amqp.metrics.AmqpMetricManager;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Consumer;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.transaction.AutoCommitTransaction;
import io.ballerina.messaging.broker.core.transaction.BrokerTransaction;
import io.ballerina.messaging.broker.core.util.MessageTracer;
import io.ballerina.messaging.broker.core.util.TraceField;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.transaction.xa.Xid;

/**
 * AMQP channel representation.
 */
public class AmqpChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpChannel.class);

    private static final String ACKNOWLEDGE_RECEIVED = "Acknowledgement received from AMQP transport.";

    private static final String UNKNOWN_ACKNOWLEDGEMENT = "Matching message for acknowledgment not found.";

    private static final String REJECT_RECEIVED = "Message reject received from AMQP transport.";

    private static final String UNKNOWN_REJECT = "Matching message for reject not found.";

    public static final String DELIVERY_TAG_FIELD_NAME = "deliveryTag";

    public static final String CHANNEL_ID_FIELD_NAME = "channelId";

    private static final String REQUEUE_FLAG_FIELD_NAME = "requeueFlag";

    private final Broker broker;

    private final int channelId;

    private final AmqpMetricManager metricManager;

    private final Map<ShortString, AmqpConsumer> consumerMap;

    private final InMemoryMessageAggregator messageAggregator;

    private final ChannelFlowManager flowManager;

    private final int maxRedeliveryCount;

    private AtomicBoolean closed = new AtomicBoolean(false);
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
     * Used to handle the acknowledgment data matching to a delivery id.
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

    private final TraceField traceChannelIdField;

    /**
     * Represent the underlying transaction implementation.
     */
    private BrokerTransaction transaction;

    /**
     * Max window size.
     */
    private int prefetchCount;

    public AmqpChannel(AmqpServerConfiguration configuration,
                       Broker broker,
                       int channelId,
                       AmqpMetricManager metricManager) {
        this.broker = broker;
        this.channelId = channelId;
        this.metricManager = metricManager;
        this.consumerMap = new HashMap<>();
        this.transaction = new AutoCommitTransaction(broker);
        this.messageAggregator = new InMemoryMessageAggregator(transaction);
        this.flowManager = new ChannelFlowManager(this,
                                                  configuration.getChannelFlow().getLowLimit(),
                                                  configuration.getChannelFlow().getHighLimit());
        this.maxRedeliveryCount = Integer.parseInt(configuration.getMaxRedeliveryCount());
        traceChannelIdField = new TraceField(CHANNEL_ID_FIELD_NAME, channelId);
    }

    public void declareExchange(String exchangeName, String exchangeType, boolean passive, boolean durable)
            throws BrokerException, ValidationException {
        broker.declareExchange(exchangeName, exchangeType, passive, durable);
    }

    public void deleteExchange(String exchangeName, boolean ifUnused)
            throws BrokerException, ValidationException, ResourceNotFoundException {
        broker.deleteExchange(exchangeName, ifUnused);
    }

    public void declareQueue(ShortString queue, boolean passive,
                             boolean durable, boolean autoDelete)
            throws BrokerException, ValidationException {
        broker.createQueue(queue.toString(), passive, durable, autoDelete);
    }

    public void bind(ShortString queue, ShortString exchange,
                     ShortString routingKey, FieldTable arguments) throws BrokerException, ValidationException {
        broker.bind(queue.toString(), exchange.toString(), routingKey.toString(), arguments);
    }

    public void unbind(ShortString queue, ShortString exchange, ShortString routingKey)
            throws BrokerException, ValidationException {
        broker.unbind(queue.toString(), exchange.toString(), routingKey.toString());
    }

    public AmqpConsumer consume(ShortString queueName, ShortString consumerTag, boolean exclusive,
                               ChannelHandlerContext ctx) throws BrokerException {
        ShortString tag = consumerTag;
        if (tag.isEmpty()) {
            tag = ShortString.parseString("sgen" + getNextConsumerTag());
        }
        AmqpConsumer amqpConsumer = new AmqpConsumer(ctx, broker, this, queueName.toString(), tag, exclusive);
        consumerMap.put(tag, amqpConsumer);
        broker.addConsumer(amqpConsumer);
        metricManager.incrementConsumerCount();
        return amqpConsumer;
    }

    public void close() {
        closed.set(true);
        for (Consumer consumer : consumerMap.values()) {
            closeConsumer(consumer);
        }
        transaction.onClose();
        consumerMap.clear();
        requeueUnackedMessages();
    }

    private void requeueUnackedMessages() {
        Collection<AckData> ackDataList = unackedMessageMap.removeAll();

        for (AckData ackData : ackDataList) {
            Message message = ackData.getMessage();
            String queueName = ackData.getQueueName();
            try {
                broker.requeue(queueName, message);
            } catch (BrokerException e) {
                LOGGER.error("Error while requeueing message [{}] for queue ()", message, queueName, e);
            } catch (ResourceNotFoundException e) {
                LOGGER.warn("Cannot requeue message [{}] since queue {} is not found", message, queueName, e);
            }
        }
    }

    public void cancelConsumer(ShortString consumerTag) throws ChannelException {
        AmqpConsumer amqpConsumer = consumerMap.remove(consumerTag);
        if (amqpConsumer != null) {
            closeConsumer(amqpConsumer);
        } else {
            throw new ChannelException(ChannelException.NOT_FOUND,
                                       "Invalid Consumer tag [ " + consumerTag + " ] for the channel: " + channelId);
        }
    }

    private void closeConsumer(Consumer consumer) {
        try {
            boolean queueDeleted = broker.removeConsumer(consumer);
            if (queueDeleted) {
                unackedMessageMap.releaseAndRemoveMessages(consumer.getQueueName());
            }
        } finally {
            metricManager.decrementConsumerCount();
        }
    }

    public InMemoryMessageAggregator getMessageAggregator() {
        return messageAggregator;
    }

    public void acknowledge(long deliveryTag, boolean multiple) throws BrokerException {
        //TODO handle multiple
        AckData ackData = unackedMessageMap.markAcknowledgement(deliveryTag);
        if (MessageTracer.isTraceEnabled()) {
            String description = Objects.nonNull(ackData) ? ACKNOWLEDGE_RECEIVED : UNKNOWN_ACKNOWLEDGEMENT;
            MessageTracer.trace(description, traceChannelIdField, new TraceField(DELIVERY_TAG_FIELD_NAME, deliveryTag));
        }
        if (ackData != null) {
            transaction.dequeue(ackData.getQueueName(), ackData.getMessage().getDetachableMessage());
            if (!transaction.inTransactionBlock()) {
                unackedMessageMap.removeMarkedAcknowledgment(deliveryTag);
                ackData.getMessage().release();
            }
        } else {
            LOGGER.warn("Could not find a matching ack data for acking the delivery tag {}", deliveryTag);
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

    public void reject(long deliveryTag, boolean requeue) throws BrokerException, ResourceNotFoundException {
        metricManager.markReject();
        AckData ackData = unackedMessageMap.negativeAcknowledge(deliveryTag);
        if (MessageTracer.isTraceEnabled()) {
            String description = Objects.nonNull(ackData) ? REJECT_RECEIVED : UNKNOWN_REJECT;
            MessageTracer.trace(description, traceChannelIdField,
                                new TraceField(DELIVERY_TAG_FIELD_NAME, deliveryTag),
                                new TraceField(REQUEUE_FLAG_FIELD_NAME, requeue));
        }

        if (ackData != null) {
            Message message = ackData.getMessage();
            if (requeue) {
                setRedeliverAndRequeue(message, ackData.getQueueName());
            } else {
                message.release();
                LOGGER.debug("Dropping message for delivery tag {}", deliveryTag);
            }
        } else {
            LOGGER.warn("Could not find a matching ack data for rejecting the delivery tag {}", deliveryTag);
        }
    }

    private void setRedeliverAndRequeue(Message message, String queueName)
            throws BrokerException, ResourceNotFoundException {
        int redeliveryCount = message.setRedeliver();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Redelivery count is {} for message {}",
                         redeliveryCount,
                         message.getInternalId());
        }
        if (redeliveryCount <= maxRedeliveryCount) {
            broker.requeue(queueName, message);
        } else {
            broker.moveToDlc(queueName, message);
        }
    }

    /**
     * Get all the unacknowledged messages and clear the unackedMessageMap.
     *
     * @return all unacknowledged messages
     */
    public Collection<AckData> recover() {
        return unackedMessageMap.removeAll();
    }

    public void requeueAll() throws BrokerException {
        Collection<AckData> entries = unackedMessageMap.removeAll();
        for (AckData ackData : entries) {
            String queueName = ackData.getQueueName();
            Message message = ackData.getMessage();
            try {
                broker.requeue(queueName, message);
            } catch (ResourceNotFoundException e) {
                LOGGER.warn("Cannot requeue message [" + message + "] since queue [" + queueName + "] is not found", e);
            }
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
        return flow.get() && hasRoom.get() && !closed.get();
    }

    /**
     * Indicate if the channel is closed by client.
     * @return true if channel is closed, false otherwise
     */
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * Indicate if client enforced flow control is enabled.
     *
     * @return true if flow is enabled. false otherwise
     */
    public boolean isFlowEnabled() {
        return flow.get();
    }

    /**
     * Getter for flowManager.
     */
    public ChannelFlowManager getFlowManager() {
        return flowManager;
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

    public AmqpDeliverMessage createDeliverMessage(Message message, ShortString consumerTag, String queueName) {
        return new AmqpDeliverMessage(message, consumerTag, this, queueName, broker);
    }

    /**
     * Start local transaction on the channel.
     */
    public void setLocalTransactional() {
        transaction = broker.newLocalTransaction();
        messageAggregator.setTransaction(transaction);
    }

    /**
     * Start distributed transaction on the channel.
     */
    public void setDistributedTransactional() {
        transaction = broker.newDistributedTransaction();
        messageAggregator.setTransaction(transaction);
    }

    /**
     * Commit the transaction on the channel.
     */
    public void commit() throws ValidationException, BrokerException {
        transaction.commit();
        releasePendingAcknowledgements();
    }

    private void releasePendingAcknowledgements() {
        Collection<AckData> acknowledgments = unackedMessageMap.removeMarkedAcknowledgments();
        for (AckData ackData: acknowledgments) {
            ackData.getMessage().release();
        }
    }

    /**
     * Rollback the transaction on the channel.
     */
    public void rollback() throws ValidationException {
        transaction.rollback();
        unackedMessageMap.resetMarkedAcknowledgments();
    }

    /**
     * Check whether the channel is in a transaction mode.
     *
     * @return transaction started or not. True if not in a transaction false otherwise
     */
    public boolean isNonTransactional() {
        return transaction instanceof AutoCommitTransaction;
    }

    public void startDtx(Xid xid, boolean join, boolean resume) throws ValidationException {
        transaction.start(xid, channelId, join, resume);
    }

    public void endDtx(Xid xid, boolean fail, boolean suspend) throws ValidationException {
        transaction.end(xid, channelId,  fail, suspend);
    }

    public void prepare(Xid xid) throws BrokerException, ValidationException {
        transaction.prepare(xid);
    }

    public void commit(Xid xid, boolean onePhase) throws ValidationException, BrokerException {
        transaction.commit(xid, onePhase);
        releasePendingAcknowledgements();
    }

    public void rollback(Xid xid) throws ValidationException, BrokerException {
        transaction.rollback(xid);
        unackedMessageMap.resetMarkedAcknowledgments();
    }

    public void forget(Xid xid) throws ValidationException {
        transaction.forget(xid);
    }

    public void setTimeout(Xid xid, long timeout) throws ValidationException {
        transaction.setTimeout(xid, timeout, TimeUnit.SECONDS);
    }

    /**
     * Data-structure to handle unacknowledge messages. This class will update the has room variable depending on the
     * number of messages in the unackedMessageMap.
     */
    private class UnackedMessageMap {

        /**
         * Acknowledgment pending messages.
         */
        private final Map<Long, AckData> pendingAcknowledgments = new LinkedHashMap<>();

        /**
         * Acknowledgments that are waiting for a subsequent commit or rollback.
         */
        private final Map<Long, AckData> markedAcknowledgments = new LinkedHashMap<>();

        /**
         * Mark the specific delivery tag as acknowledgment received and return the specific {@link AckData} object.
         *
         * @param deliveryTag delivery tag of the acknowledged message delivery
         * @return AckData object for the corresponding delivery tag
         */
        AckData markAcknowledgement(long deliveryTag) {
            AckData ackData = pendingAcknowledgments.remove(deliveryTag);
            if (Objects.nonNull(ackData)) {
                markedAcknowledgments.put(deliveryTag, ackData);
            }
            return ackData;
        }

        AckData negativeAcknowledge(long deliveryTag) {
            AckData ackData = pendingAcknowledgments.remove(deliveryTag);
            checkAndEnableHasRoom();
            return ackData;
        }

        void put(long deliveryTag, AckData ackData) {
            pendingAcknowledgments.put(deliveryTag, ackData);
            checkAndDisableHasRoom();
        }

        Collection<AckData> removeAll() {
            Collection<AckData> entries = new ArrayList<>(pendingAcknowledgments.values());
            pendingAcknowledgments.clear();
            entries.addAll(markedAcknowledgments.values());
            markedAcknowledgments.clear();
            hasRoom.set(true);
            return entries;
        }

        AckData removeMarkedAcknowledgment(long deliveryTag) {
            AckData ackData = markedAcknowledgments.remove(deliveryTag);
            checkAndEnableHasRoom();
            return ackData;
        }

        private void checkAndEnableHasRoom() {
            if (!hasRoom.get() && pendingAcknowledgments.size() < prefetchCount) {
                hasRoom.set(true);
            }
        }

        void resetMarkedAcknowledgments() {
            pendingAcknowledgments.putAll(markedAcknowledgments);
            markedAcknowledgments.clear();
            checkAndDisableHasRoom();
        }

        Collection<AckData> removeMarkedAcknowledgments() {
            ArrayList<AckData> ackedMessages = new ArrayList<>(markedAcknowledgments.values());
            markedAcknowledgments.clear();
            checkAndEnableHasRoom();
            return ackedMessages;
        }
        private void checkAndDisableHasRoom() {
            if (hasRoom.get() && pendingAcknowledgments.size() >= prefetchCount) {
                hasRoom.set(false);
            }
        }

        void releaseAndRemoveMessages(String queueName) {
            releaseAndRemoveMessagesFromMap(queueName, pendingAcknowledgments);
            releaseAndRemoveMessagesFromMap(queueName, markedAcknowledgments);
            checkAndEnableHasRoom();
        }

        private void releaseAndRemoveMessagesFromMap(String queueName, Map<Long, AckData> map) {
            Iterator<Map.Entry<Long, AckData>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, AckData> entry = iterator.next();
                AckData ackData = entry.getValue();
                if (ackData.getQueueName().equals(queueName)) {
                    ackData.getMessage().release();
                    iterator.remove();
                }
            }
        }
    }
}
