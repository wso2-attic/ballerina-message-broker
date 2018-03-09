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
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP frame for queue.purge
 * Parameter Summary:
 * 1. reserved-1 (short) - deprecated
 * 2. queue (ShortString) - queue name
 * 3. no-wait (bit) - No wait
 */
public class QueuePurge extends MethodFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueuePurge.class);
    private static final short CLASS_ID = 50;
    private static final short METHOD_ID = 30;

    private final ShortString queue;
    private final boolean noWait;

    public QueuePurge(int channel, ShortString queue, boolean noWait) {
        super(channel, CLASS_ID, METHOD_ID);
        this.queue = queue;
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
        buf.writeBoolean(noWait);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        Broker broker = connectionHandler.getBroker();
        ctx.fireChannelRead((BlockingTask) () -> {
            try {
                int numberMessages = broker.purgeQueue(queue.toString());
                ctx.writeAndFlush(new QueuePurgeOk(getChannel(), numberMessages));

            } catch (ResourceNotFoundException e) {
                LOGGER.debug("Queue purge failure due to resource not found", e);
                ctx.writeAndFlush(new ChannelClose(getChannel(),
                                                   ChannelException.NOT_FOUND,
                                                   ShortString.parseString(e.getMessage()),
                                                   CLASS_ID,
                                                   METHOD_ID));
            } catch (ValidationException e) {
                LOGGER.debug("Queue delete validation failure", e);
                ctx.writeAndFlush(new ChannelClose(getChannel(),
                                                   ChannelException.PRECONDITION_FAILED,
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
            boolean noWait = buf.readBoolean();

            return new QueuePurge(channel, queue, noWait);
        };
    }
}
