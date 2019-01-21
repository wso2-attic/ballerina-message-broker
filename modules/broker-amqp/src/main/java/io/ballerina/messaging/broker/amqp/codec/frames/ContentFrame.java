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

package io.ballerina.messaging.broker.amqp.codec.frames;

import io.ballerina.messaging.broker.amqp.AmqpException;
import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.amqp.codec.BlockingTask;
import io.ballerina.messaging.broker.amqp.codec.ChannelException;
import io.ballerina.messaging.broker.amqp.codec.InMemoryMessageAggregator;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP content frame.
 */
public class ContentFrame extends GeneralFrame {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentFrame.class);

    private final long length;
    private final ByteBuf payload;

    public ContentFrame(int channel, long length, ByteBuf payload) {
        super((byte) 3, channel);
        this.length = length;
        this.payload = payload;
    }

    @Override
    public long getPayloadSize() {
        return length;
    }

    @Override
    public void writePayload(ByteBuf buf) {
        try {
            buf.writeBytes(payload);
        } finally {
            payload.release();
        }
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        AmqpChannel channel = connectionHandler.getChannel(getChannel());

        boolean allContentReceived;
        InMemoryMessageAggregator messageAggregator = channel.getMessageAggregator();

        try {
            allContentReceived = messageAggregator.contentBodyReceived(length, payload);
        } catch (AmqpException e) {
            LOGGER.warn("Content receiving failed", e);
            return;
        }

        if (allContentReceived) {
            Message message = messageAggregator.popMessage();

            ctx.fireChannelRead((BlockingTask) () -> {
                try {
                    messageAggregator.publish(message);
                    // flow manager should always be executed through the event loop
                    ctx.executor().submit(() -> channel.getFlowManager().notifyMessageRemoval(ctx));
                } catch (BrokerException e) {
                    LOGGER.warn("Content receiving failed", e);
                } catch (AuthException | AuthNotFoundException e) {
                    ctx.writeAndFlush(new ChannelClose(getChannel(),
                                                       ChannelException.ACCESS_REFUSED,
                                                       ShortString.parseString(e.getMessage()),
                                                       BasicPublish.CLASS_ID,
                                                       BasicPublish.METHOD_ID));
                }
            });
        }
    }

    public static ContentFrame parse(ByteBuf buf, int channel, long payloadSize) {
        ByteBuf payload = buf.retainedSlice(buf.readerIndex(), (int) payloadSize);
        buf.skipBytes((int) payloadSize);

        return new ContentFrame(channel, payloadSize, payload);
    }
}
