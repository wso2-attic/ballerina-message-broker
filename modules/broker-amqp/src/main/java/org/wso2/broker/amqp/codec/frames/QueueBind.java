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
 * AMQP frame for queue.bind
 * Parameter Summary:
 *     1. reserved-1 (short) - deprecated
 *     2. queue (ShortString) - queue name
 *     3. exchange (ShortString) - name of the exchange to bind to
 *     4. routing­key (ShortString) - message routing key
 *     5. no-wait (bit) - No wait
 *     8. arguments (FieldTable) - arguments for declaration
 */
public class QueueBind extends MethodFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueBind.class);

    private final ShortString queue;
    private final ShortString exchange;
    private final ShortString routingKey;
    private final boolean noWait;
    private final FieldTable arguments;

    public QueueBind(int channel, ShortString queue, ShortString exchange, ShortString routingKey, boolean noWait,
            FieldTable arguments) {
        super(channel, (short) 50, (short) 20);
        this.queue = queue;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.noWait = noWait;
        this.arguments = arguments;
    }

    @Override
    protected long getMethodBodySize() {
        return 2L + queue.getSize() + exchange.getSize() + routingKey.getSize() + 1L + arguments.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeShort(0);
        queue.write(buf);
        exchange.write(buf);
        routingKey.write(buf);
        buf.writeBoolean(noWait);
        arguments.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // TODO handle queue bind frame
        ctx.writeAndFlush(new QueueBindOk(getChannel()));
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            buf.skipBytes(2);
            ShortString queue = ShortString.parse(buf);
            ShortString exchange = ShortString.parse(buf);
            ShortString routingKey = ShortString.parse(buf);
            boolean noWait = buf.readBoolean();
            FieldTable arguments = FieldTable.parse(buf);

            return new QueueBind(channel, queue, exchange, routingKey, noWait, arguments);
        };
    }
}
