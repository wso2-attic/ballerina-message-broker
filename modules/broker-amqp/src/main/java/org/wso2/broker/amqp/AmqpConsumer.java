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

package org.wso2.broker.amqp;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqpChannel;
import org.wso2.broker.common.data.types.ShortString;
import org.wso2.broker.core.Consumer;
import org.wso2.broker.core.Message;

/**
 * AMQP based message consumer.
 */
public class AmqpConsumer implements Consumer {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpConsumer.class);

    private final String queueName;

    private final ShortString consumerTag;

    private final boolean isExclusive;

    private final ChannelHandlerContext context;

    private final AmqpChannel channel;
    private ChannelFutureListener errorLogger;

    public AmqpConsumer(ChannelHandlerContext ctx, AmqpChannel channel,
                        String queueName, ShortString consumerTag, boolean isExclusive) {
        this.queueName = queueName;
        this.consumerTag = consumerTag;
        this.isExclusive = isExclusive;
        this.context = ctx;
        this.channel = channel;
        this.errorLogger = new ErrorLogger(queueName);
    }

    @Override
    public void send(Message message) {

        AmqpDeliverMessage deliverMessage = new AmqpDeliverMessage(message,
                                                                   consumerTag,
                                                                   channel,
                                                                   queueName);

        ChannelFuture channelFuture = context.channel().writeAndFlush(deliverMessage);
        channelFuture.addListener(errorLogger);
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
    public boolean isActive() {
        return channel.isActive();
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
}
