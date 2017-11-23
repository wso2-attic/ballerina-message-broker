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

package org.wso2.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqpConnectionHandler;

/**
 * AMQP frame for basic.qos
 * Parameter Summary:
 *     1. prefetch­size (long) - prefetch window in octets
 *     2. prefetch­count (short) - prefetch window in messages
 *     3. global (bit) - apply to entire connection
 */
public class BasicQos extends MethodFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicQos.class);

    private final long prefetchWindowSize;
    private final int prefetchCount;
    private final boolean global;

    public BasicQos(int channel, long prefetchWindowSize, int prefetchCount, boolean global) {
        super(channel, (short) 60, (short) 10);
        this.prefetchWindowSize = prefetchWindowSize;
        this.prefetchCount = prefetchCount;
        this.global = global;
    }

    @Override
    protected long getMethodBodySize() {
        return 4L + 2L + 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeInt((int) prefetchWindowSize);
        buf.writeShort(prefetchCount);
        buf.writeBoolean(global);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // TODO QoS Parameters should be propogated to message prefetch logic
        int channelId = getChannel();
        ctx.writeAndFlush(new BasicQosOk(channelId));
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            long prefetchWindowSize = buf.readUnsignedInt();
            int prefetchCount = buf.readUnsignedShort();
            boolean global = buf.readBoolean();
            return new BasicQos(channel, prefetchWindowSize, prefetchCount, global);
        };
    }
}
