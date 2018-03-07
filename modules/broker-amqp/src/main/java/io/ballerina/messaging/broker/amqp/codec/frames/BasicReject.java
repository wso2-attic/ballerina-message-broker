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

import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.amqp.codec.BlockingTask;
import io.ballerina.messaging.broker.amqp.codec.ChannelException;
import io.ballerina.messaging.broker.amqp.codec.ConnectionException;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.BrokerException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP frame for basic.reject
 * Parameter Summary:
 *      1. delivery-tag (longlong) - delivery tag
 *      2. requeue (bit) - requeue message
 */
public class BasicReject extends MethodFrame {
    /**
     * Class ID of the frame.
     */
    private static final int CLASS_ID = 60;

    /**
     * Method ID of the frame.
     */
    private static final int METHOD_ID = 90;

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicReject.class);

    private final long deliveryTag;
    private final boolean requeue;

    public BasicReject(int channel, long deliveryTag, boolean requeue) {
        super(channel, (short) CLASS_ID, (short) METHOD_ID);
        this.deliveryTag = deliveryTag;
        this.requeue = requeue;
    }

    @Override
    protected long getMethodBodySize() {
        return 8L + 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeLong(deliveryTag);
        buf.writeBoolean(requeue);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        AmqpChannel channel = connectionHandler.getChannel(getChannel());
        ctx.fireChannelRead((BlockingTask) () -> {
            try {
                channel.reject(deliveryTag, requeue);
            } catch (ResourceNotFoundException e) {
                LOGGER.debug("Error rejecting message for delivery tag " + deliveryTag, e);
                ctx.writeAndFlush(new ChannelClose(getChannel(),
                                                   ChannelException.NOT_FOUND,
                                                   ShortString.parseString(e.getMessage()),
                                                   CLASS_ID,
                                                   METHOD_ID));
            } catch (BrokerException e) {
                LOGGER.warn("Error rejecting message for delivery tag " + deliveryTag, e);
                ctx.writeAndFlush(new ConnectionClose(ConnectionException.INTERNAL_ERROR,
                                                      ShortString.parseString(e.getMessage()),
                                                      CLASS_ID,
                                                      METHOD_ID));
            }
        });
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            long deliveryTag = buf.readLong();
            boolean requeue = buf.readBoolean();
            return new BasicReject(channel, deliveryTag, requeue);
        };
    }
}
