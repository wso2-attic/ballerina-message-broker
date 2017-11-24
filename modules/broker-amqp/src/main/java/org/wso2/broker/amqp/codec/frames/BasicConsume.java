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
import org.wso2.broker.amqp.codec.data.FieldTable;
import org.wso2.broker.amqp.codec.data.ShortString;

/**
 * AMQP frame for basic.consume
 * Parameter Summary:
 *     1. reserved-1 (short) - deprecated
 *     2. queue (ShortString) - queue name
 *     3. consumer­tag (ShortString) - consumer tag
 *     4. no-local (bit) - no local
 *     5. no-ack (bit) - no ack
 *     6. exclusive (bit) - request exclusive access
 *     7. no­wait (bit) - no­wait
 *     8. arguments (FieldTable) - arguments for declaration
 */
public class BasicConsume extends MethodFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicConsume.class);

    private final ShortString queue;
    private final ShortString consumerTag;
    private final boolean noLocal;
    private final boolean noAck;
    private final boolean exclusive;
    private final boolean noWait;
    private final FieldTable arguments;

    public BasicConsume(int channel, ShortString queue, ShortString consumerTag, boolean noLocal, boolean noAck,
            boolean exclusive, boolean noWait, FieldTable arguments) {
        super(channel, (short) 60, (short) 20);
        this.queue = queue;
        this.consumerTag = consumerTag;
        this.noLocal = noLocal;
        this.noAck = noAck;
        this.exclusive = exclusive;
        this.noWait = noWait;
        this.arguments = arguments;
    }

    @Override
    protected long getMethodBodySize() {
        return 2L + queue.getSize() + consumerTag.getSize() + 1L + arguments.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeShort(0);
        queue.write(buf);
        consumerTag.write(buf);

        byte flags = 0x0;
        if (noLocal) {
            flags |= 0x1;
        }
        if (noAck) {
            flags |= 0x2;
        }
        if (exclusive) {
            flags |= 0x4;
        }
        if (noWait) {
            flags |= 0x8;
        }
        buf.writeByte(flags);

        arguments.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // TODO handle basic consume frame
        ctx.writeAndFlush(new BasicConsumeOk(getChannel(), consumerTag));
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            buf.skipBytes(2);
            ShortString queue = ShortString.parse(buf);
            ShortString consumerTag = ShortString.parse(buf);
            byte flags = buf.readByte();
            boolean noLocal = (flags & 0x1) == 0x1;
            boolean noAck = (flags & 0x2) == 0x2;
            boolean exclusive = (flags & 0x4) == 0x4;
            boolean noWait = (flags & 0x8) == 0x8;
            FieldTable arguments = FieldTable.parse(buf);
            return new BasicConsume(channel, queue, consumerTag, noLocal, noAck, exclusive, noWait, arguments);
        };
    }
}
