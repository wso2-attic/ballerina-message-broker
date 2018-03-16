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

import io.ballerina.messaging.broker.amqp.codec.BlockingTask;
import io.ballerina.messaging.broker.amqp.codec.ChannelException;
import io.ballerina.messaging.broker.amqp.codec.ConnectionException;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerAuthException;
import io.ballerina.messaging.broker.core.BrokerAuthNotFoundException;
import io.ballerina.messaging.broker.core.BrokerException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP frame for queue.delete
 * Parameter Summary:
 *     1. reserved-1 (short) - deprecated
 *     2. queue (ShortString) - queue name
 *     3. ifUnused (bit) - delete only if unused
 *     4. ifEmpty (bit) - delete only if empty
 *     5. no-wait (bit) - No wait
 */
public class QueueDelete extends MethodFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueDelete.class);

    private static final short CLASS_ID = 50;

    private static final short METHOD_ID = 40;

    private final ShortString queue;

    private final boolean ifUnused;

    private final boolean ifEmpty;

    private final boolean noWait;

    QueueDelete(int channel, ShortString queue, boolean ifUnused, boolean ifEmpty, boolean noWait) {
        super(channel, CLASS_ID, METHOD_ID);
        this.queue = queue;
        this.ifUnused = ifUnused;
        this.ifEmpty = ifEmpty;
        this.noWait = noWait;
    }

    @Override
    protected long getMethodBodySize() {
        return 2L + queue.getSize() + 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeShort(0);
        queue.write(buf);
        int flags = 0x00;
        if (ifUnused) {
            flags |= 0x1;
        }
        if (ifEmpty) {
            flags |= 0x2;
        }
        if (noWait) {
            flags |= 0x4;
        }
        buf.writeByte(flags);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        Broker broker = connectionHandler.getBroker();
        ctx.fireChannelRead((BlockingTask) () -> {
            try {
                int messageCount = broker.deleteQueue(queue.toString(), ifUnused, ifEmpty);
                ctx.writeAndFlush(new QueueDeleteOk(getChannel(), messageCount));
            } catch (ResourceNotFoundException | BrokerAuthNotFoundException e) {
                // For AMQP clients this is not an exception. Respond with message count zero.
                ctx.writeAndFlush(new QueueDeleteOk(getChannel(), 0));
            } catch (ValidationException e) {
                LOGGER.debug("Queue delete validation failure", e);
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
                LOGGER.warn("Error deleting queue.", e);
                ctx.writeAndFlush(new ConnectionClose(ConnectionException.INTERNAL_ERROR,
                        ShortString.parseString(e.getMessage()),
                        CLASS_ID,
                        METHOD_ID));
            }

        });
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            buf.skipBytes(2);
            ShortString queue = ShortString.parse(buf);
            long flags = buf.readByte();
            boolean ifUnused = (flags & 0x1) == 0x1;
            boolean ifEmpty = (flags & 0x2) == 0x2;
            boolean noWait = (flags & 0x4) == 0x4;
            return new QueueDelete(channel, queue, ifUnused, ifEmpty, noWait);
        };
    }
}
