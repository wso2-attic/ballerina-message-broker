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
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP frame for basic.ack.
 * Parameter Summary:
 *      1. delivery-tag (longlong) - delivery tag
 *      2. multiple (bit) - acknowledge multiple messages
 */
public class BasicAck extends MethodFrame {

    private final long deliveryTag;
    private final boolean multiple;

    public BasicAck(int channel, long deliveryTag, boolean multiple) {
        super(channel, (short) 60, (short) 80);
        this.deliveryTag = deliveryTag;
        this.multiple = multiple;
    }

    @Override
    protected long getMethodBodySize() {
        return 8L + 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeLong(deliveryTag);
        buf.writeBoolean(multiple);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        AmqpChannel channel = connectionHandler.getChannel(getChannel());
        ctx.fireChannelRead((BlockingTask) () -> channel.acknowledge(deliveryTag, multiple));
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            long deliveryTag = buf.readLong();
            boolean multiple = buf.readBoolean();
            return new BasicAck(channel, deliveryTag, multiple);
        };
    }
}
