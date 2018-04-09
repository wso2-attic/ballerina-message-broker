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

package io.ballerina.messaging.broker.amqp.consumer;

import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.Consumer;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.util.MessageTracer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final ChannelFutureListenerFactory channelFutureListenerFactory;

    private boolean isReady;

    public AmqpConsumer(ChannelHandlerContext ctx,
                        Broker broker,
                        AmqpChannel channel,
                        String queueName,
                        ShortString consumerTag,
                        boolean isExclusive) {
        this.queueName = queueName;
        this.consumerTag = consumerTag;
        this.isExclusive = isExclusive;
        this.context = ctx;
        this.channel = channel;
        if (MessageTracer.isTraceEnabled()) {
            this.channelFutureListenerFactory = new ConsumerErrorHandlerFactory(broker, queueName);
        } else {
            this.channelFutureListenerFactory = new TracingChannelFutureListenerFactory(broker,
                                                                                        queueName,
                                                                                        channel.getChannelId(),
                                                                                        consumerTag,
                                                                                        this);
        }
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

        ChannelFuture channelFuture = context.channel().writeAndFlush(deliverMessage);
        channelFuture.addListener(channelFutureListenerFactory.createListener(message));
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public void close() {
        // There is no frame to close a consumer from server side.
    }

    @Override
    public boolean isExclusive() {
        return isExclusive;
    }

    public void enableConsume() {
        this.isReady = true;
    }

    public ShortString getConsumerTag() {
        return consumerTag;
    }

    @Override
    public boolean isReady() {
        return channel.isReady() && isReady;
    }

    @Override
    public String toString() {
        return "AmqpConsumer{"
                + "queueName='" + queueName + '\''
                + ", consumerTag=" + consumerTag
                + ", isExclusive=" + isExclusive
                + '}';
    }

}
