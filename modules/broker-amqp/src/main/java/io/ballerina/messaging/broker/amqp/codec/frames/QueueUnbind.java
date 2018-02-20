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
import io.ballerina.messaging.broker.amqp.codec.ConnectionException;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.BrokerException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP frame for queue.unbind
 * Parameter Summary:
 * 1. reserved-1 (short) - deprecated
 * 2. queue (ShortString) - queue name
 * 3. exchange (ShortString) - name of the exchange to bind to
 * 4. routing­key (ShortString) - message routing key
 * 5. arguments (FieldTable) - arguments for declaration
 */
public class QueueUnbind extends MethodFrame {

    private static final short CLASS_ID = 50;

    private static final short METHOD_ID = 50;

    private final ShortString queue;

    private final ShortString exchange;

    private final ShortString routingKey;

    private final FieldTable arguments;

    public QueueUnbind(int channel, ShortString queue, ShortString exchange, ShortString routingKey,
                       FieldTable arguments) {
        super(channel, CLASS_ID, METHOD_ID);
        this.queue = queue;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.arguments = arguments;
    }

    @Override
    protected long getMethodBodySize() {
        return 2L + queue.getSize() + exchange.getSize() + routingKey.getSize() + arguments.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeShort(0);
        queue.write(buf);
        exchange.write(buf);
        routingKey.write(buf);
        arguments.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        AmqpChannel channel = connectionHandler.getChannel(getChannel());
        ctx.fireChannelRead((BlockingTask) () -> {
            try {
                channel.unbind(queue, exchange, routingKey);
                ctx.writeAndFlush(new QueueUnbindOk(channel.getChannelId()));
            } catch (BrokerException e) {
                ctx.writeAndFlush(new ConnectionClose(ConnectionException.INTERNAL_ERROR,
                                                      ShortString.parseString(e.getMessage()),
                                                      CLASS_ID,
                                                      METHOD_ID));
            } catch (ValidationException e) {
                ctx.writeAndFlush(new ConnectionClose(ConnectionException.NOT_ALLOWED,
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
            ShortString exchange = ShortString.parse(buf);
            ShortString routingKey = ShortString.parse(buf);
            FieldTable arguments = FieldTable.parse(buf);
            return new QueueUnbind(channel, queue, exchange, routingKey, arguments);
        };
    }
}
