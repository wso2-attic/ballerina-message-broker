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
import org.wso2.broker.amqp.codec.data.ShortString;
import org.wso2.broker.amqp.codec.frames.BasicDeliver;
import org.wso2.broker.amqp.codec.frames.ContentFrame;
import org.wso2.broker.amqp.codec.frames.HeaderFrame;
import org.wso2.broker.core.Consumer;
import org.wso2.broker.core.ContentChunk;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.Metadata;

/**
 * AMQP based message consumer
 */
public class AmqpConsumer implements Consumer {
    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpConsumer.class);

    private final String queueName;

    private final String consumerTag;

    private final boolean isExclusive;

    private final ChannelHandlerContext context;

    private final int channelId;
    private ChannelFutureListener errorLogger;

    public AmqpConsumer(ChannelHandlerContext ctx, int channelId,
                        String queueName, String consumerTag, boolean isExclusive) {
        this.queueName = queueName;
        this.consumerTag = consumerTag;
        this.isExclusive = isExclusive;
        this.context = ctx;
        this.channelId = channelId;
        this.errorLogger = new ErrorLogger(queueName);
    }

    @Override
    public void send(Message message, long deliveryTag) {
        Metadata metadata = message.getMetadata();

        BasicDeliver basicDeliver = new BasicDeliver(
                channelId,
                ShortString.parseString(consumerTag),
                deliveryTag,
                false,
                ShortString.parseString(metadata.getExchangeName()),
                ShortString.parseString(metadata.getRoutingKey()));

        HeaderFrame headerFrame = new HeaderFrame(channelId, 60, metadata.getContentLength());
        headerFrame.setRawMetadata(metadata.getRawMetadata());
        context.write(basicDeliver).addListener(errorLogger);
        context.write(headerFrame).addListener(errorLogger);
        for (ContentChunk chunk : message.getContentChunks()) {
            ContentFrame contentFrame = new ContentFrame(channelId, chunk.getBytes().capacity(), chunk.getBytes());
            context.write(contentFrame).addListener(errorLogger);
        }
        context.flush();
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public void close() {
    }

    @Override
    public String getConsumerTag() {
        return consumerTag;
    }

    @Override
    public boolean isExclusive() {
        return isExclusive;
    }

    private static class ErrorLogger implements ChannelFutureListener {
        private final String queueName;

        private ErrorLogger(String queueName) {
            this.queueName = queueName;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (!future.isSuccess()) {
                LOGGER.warn("Error while sending message for " + queueName, future.cause());
            }
        }
    }
}
