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

package io.ballerina.messaging.broker.amqp;

import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Consumer;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.util.MessageTracer;
import io.ballerina.messaging.broker.core.util.TraceField;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * AMQP based message consumer.
 */
public class AmqpConsumer extends Consumer {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpConsumer.class);

    public static final String CONSUMER_TAG_FIELD_NAME = "consumerTag";

    private final String queueName;

    private final ShortString consumerTag;

    private final boolean isExclusive;

    private final ChannelHandlerContext context;

    private final AmqpChannel channel;

    private ChannelFutureListener errorLogger;

    private final List<TraceField> tracingProperties;

    public AmqpConsumer(ChannelHandlerContext ctx, AmqpChannel channel,
                        String queueName, ShortString consumerTag, boolean isExclusive) {
        this.queueName = queueName;
        this.consumerTag = consumerTag;
        this.isExclusive = isExclusive;
        this.context = ctx;
        this.channel = channel;
        this.errorLogger = new ErrorLogger(queueName);
        this.tracingProperties = new ArrayList<>(2);
        tracingProperties.add(new TraceField(AmqpChannel.CHANNEL_ID_FIELD_NAME, channel.getChannelId()));
        tracingProperties.add(new TraceField(CONSUMER_TAG_FIELD_NAME, consumerTag));
    }

    @Override
    public void send(Message message) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Adding message to AMQP Netty outbound; messageId: {}, consumerTag: {}, queueName: {}",
                         message.getInternalId(),
                         consumerTag,
                         queueName);
        }

        AmqpDeliverMessage deliverMessage = channel.createDeliverMessage(message, consumerTag, queueName);

        ChannelFutureListener channelListener;
        if (MessageTracer.isTraceEnabled()) {
            channelListener = new TracingChannelFutureListener(message, this);
        } else {
            channelListener = errorLogger;
        }
        ChannelFuture channelFuture = context.channel().writeAndFlush(deliverMessage);
        channelFuture.addListener(channelListener);
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isExclusive() {
        return isExclusive;
    }

    @Override
    public boolean isReady() {
        return channel.isReady();
    }

    private static class ErrorLogger implements ChannelFutureListener {
        private final String queueName;

        private ErrorLogger(String queueName) {
            this.queueName = queueName;
        }

        @Override
        public void operationComplete(ChannelFuture future) {
            if (!future.isSuccess()) {
                LOGGER.warn("Error while sending message for " + queueName, future.cause());
            }
        }
    }

    @Override
    public String toString() {
        return "AmqpConsumer{"
                + "queueName='" + queueName + '\''
                + ", consumerTag=" + consumerTag
                + ", isExclusive=" + isExclusive
                + '}';
    }

    /**
     * Channel listener to trace errors with message written to the socket.
     */
    private class TracingChannelFutureListener implements ChannelFutureListener {

        private static final String TRANSPORT_DELIVERY_FAILURE = "Message delivery failed. AMQP transport error.";

        private static final String SENT_FROM_TRANSPORT = "Message sent from transport.";

        private final Message message;

        private final AmqpConsumer consumer;

        TracingChannelFutureListener(Message message, AmqpConsumer consumer) {
            this.message = message;
            this.consumer = consumer;
        }

        @Override
        public void operationComplete(ChannelFuture channelFuture) {
            if (channelFuture.isSuccess()) {
                MessageTracer.trace(message, consumer, SENT_FROM_TRANSPORT, tracingProperties);
            } else {
                LOGGER.warn("Error while sending message for " + queueName, channelFuture.cause());
                MessageTracer.trace(message, consumer, TRANSPORT_DELIVERY_FAILURE, tracingProperties);
            }
        }
    }
}
