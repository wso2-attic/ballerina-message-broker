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
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.BrokerAuthException;
import io.ballerina.messaging.broker.core.BrokerException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP frame for queue.declare
 * Parameter Summary:
 *     1. reserved-1 (short) - deprecated
 *     2. queue (ShortString) - queue name
 *     3. passive (bit) - do not create queue
 *     4. durable (bit) - request a durable queue
 *     5. exclusive (bit) - request an exclusive queue
 *     6. auto-delete (bit) - auto-delete queue when unused
 *     7. no-wait (bit) - No wait
 *     8. arguments (FieldTable) - arguments for declaration
 */
public class QueueDeclare extends MethodFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueDeclare.class);
    private static final short CLASS_ID = 50;
    private static final short METHOD_ID = 10;

    private final ShortString queue;
    private final boolean passive;
    private final boolean durable;
    private final boolean exclusive;
    private final boolean autoDelete;
    private final boolean noWait;
    private final FieldTable arguments;

    QueueDeclare(int channel, ShortString queue, boolean passive, boolean durable, boolean exclusive,
                 boolean autoDelete, boolean noWait, FieldTable arguments) {
        super(channel, CLASS_ID, METHOD_ID);
        this.queue = queue;
        this.passive = passive;
        this.durable = durable;
        this.exclusive = exclusive;
        this.autoDelete = autoDelete;
        this.noWait = noWait;
        this.arguments = arguments;
    }

    @Override
    protected long getMethodBodySize() {
        return 2L + queue.getSize() + 1L + arguments.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeShort(0);
        queue.write(buf);

        byte flags = 0x0;
        if (passive) {
            flags |= 0x1;
        }
        if (durable) {
            flags |= 0x2;
        }
        if (exclusive) {
            flags |= 0x4;
        }
        if (autoDelete) {
            flags |= 0x8;
        }
        if (noWait) {
            flags |= 0x10;
        }
        buf.writeByte(flags);
        arguments.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // TODO handle exclusive param
        AmqpChannel channel = connectionHandler.getChannel(getChannel());

        ctx.fireChannelRead((BlockingTask) () -> {
            try {
                channel.declareQueue(queue, passive, durable, autoDelete);
                ctx.writeAndFlush(new QueueDeclareOk(getChannel(), queue, 0, 0));
            } catch (ValidationException e) {
                ctx.writeAndFlush(new ChannelClose(getChannel(),
                                                   ChannelException.PRECONDITION_FAILED,
                                                   ShortString.parseString(e.getMessage()),
                                                   CLASS_ID,
                                                   METHOD_ID));
            } catch (BrokerAuthException e) {
                ctx.writeAndFlush(new ChannelClose(getChannel(),
                                                   ChannelException.ACCESS_REFUSED,
                                                   ShortString.parseString(e.getMessage()),
                                                   CLASS_ID,
                                                   METHOD_ID));
            } catch (BrokerException e) {
                LOGGER.warn("Error declaring queue.", e);
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
     * Getter for passive.
     */
    public boolean isPassive() {
        return passive;
    }

    /**
     * Getter for durable.
     */
    public boolean isDurable() {
        return durable;
    }

    /**
     * Getter for exclusive.
     */
    public boolean isExclusive() {
        return exclusive;
    }

    /**
     * Getter for autoDelete.
     */
    public boolean isAutoDelete() {
        return autoDelete;
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
            byte flags = buf.readByte();
            boolean passive = (flags & 0x1) == 1;
            boolean durable = (flags & 0x2) == 0x2;
            boolean exclusive = (flags & 0x4) == 0x4;
            boolean autoDelete = (flags & 0x8) == 0x8;
            boolean noWait = (flags & 0x10) == 0x10;
            FieldTable arguments = FieldTable.parse(buf);

            return new QueueDeclare(channel, queue, passive, durable, exclusive, autoDelete, noWait, arguments);
        };
    }
}
