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
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.amqp.consumer.AmqpConsumer;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.BrokerAuthException;
import io.ballerina.messaging.broker.core.BrokerAuthNotFoundException;
import io.ballerina.messaging.broker.core.BrokerException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final short CLASS_ID = 60;
    private static final short METHOD_ID = 20;

    private final ShortString queue;
    private final ShortString consumerTag;
    private final boolean noLocal;
    private final boolean noAck;
    private final boolean exclusive;
    private final boolean noWait;
    private final FieldTable arguments;

    public BasicConsume(int channel, ShortString queue, ShortString consumerTag, boolean noLocal, boolean noAck,
            boolean exclusive, boolean noWait, FieldTable arguments) {
        super(channel, CLASS_ID, METHOD_ID);
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
        AmqpChannel channel = connectionHandler.getChannel(getChannel());
        ctx.fireChannelRead((BlockingTask) () -> {
            try {
                AmqpConsumer consumer = channel.consume(queue, consumerTag, exclusive, ctx);
                ctx.writeAndFlush(new BasicConsumeOk(getChannel(), consumer.getConsumerTag()));
                consumer.enableConsume();
            } catch (BrokerAuthException | BrokerAuthNotFoundException e) {
                ctx.writeAndFlush(new ChannelClose(getChannel(),
                                                   ChannelException.ACCESS_REFUSED,
                                                   ShortString.parseString(e.getMessage()),
                                                   CLASS_ID,
                                                   METHOD_ID));
            } catch (BrokerException e) {
                ctx.writeAndFlush(new ChannelClose(getChannel(),
                                                   ChannelException.NOT_ALLOWED,
                                                   ShortString.parseString(e.getMessage()),
                                                   CLASS_ID,
                                                   METHOD_ID));
            }
        });
    }

    /**
     * Getter for queue.
     */
    public ShortString getQueue() {
        return queue;
    }

    /**
     * Getter for consumerTag.
     */
    public ShortString getConsumerTag() {
        return consumerTag;
    }

    /**
     * Getter for noLocal.
     */
    public boolean isNoLocal() {
        return noLocal;
    }

    /**
     * Getter for noAck.
     */
    public boolean isNoAck() {
        return noAck;
    }

    /**
     * Getter for exclusive.
     */
    public boolean isExclusive() {
        return exclusive;
    }

    /**
     * Getter for noWait.
     */
    public boolean isNoWait() {
        return noWait;
    }

    /**
     * Getter for arguments.
     */
    public FieldTable getArguments() {
        return arguments;
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
