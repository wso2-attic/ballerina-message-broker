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

import io.ballerina.messaging.broker.amqp.codec.ConnectionException;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP frame for channel.open
 * Parameter Summary:
 *     1. reserved-1 (ShortString) - deprecated param
 */
public class ChannelOpen extends MethodFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelOpen.class);
    private static final short CLASS_ID = 20;
    private static final short METHOD_ID = 10;

    public ChannelOpen(int channel) {
        super(channel, CLASS_ID, METHOD_ID);
    }

    @Override
    protected long getMethodBodySize() {
        return 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        // write deprecated ShortString size as 0
        buf.writeByte(0);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        try {
            int channelId = getChannel();
            connectionHandler.createChannel(channelId);
            ctx.writeAndFlush(new ChannelOpenOk(channelId));
        } catch (ConnectionException e) {
            LOGGER.warn("Error while creating channel for ID " + getChannel(), e);
            ctx.writeAndFlush(ConnectionClose.getInstance(CLASS_ID, METHOD_ID, e));
        }
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            // read the size of deprecated short string value
            short stringSize = buf.readUnsignedByte();
            buf.skipBytes(stringSize);
            return new ChannelOpen(channel);
        };
    }
}
