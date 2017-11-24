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
import org.wso2.broker.amqp.codec.AmqpChannel;
import org.wso2.broker.amqp.codec.AmqpConnectionHandler;
import org.wso2.broker.amqp.codec.data.FieldTable;
import org.wso2.broker.amqp.codec.data.ShortString;
import org.wso2.broker.core.BrokerException;

/**
 * AMQP frame for exchange.declare
 * Parameter Summary:
 *     1. reserved-1 (short) - deprecated
 *     2. exchange (ShortString) - exchange name
 *     3. type (ShortString) - exchange type
 *     4. passive (bit) - do not create exchange
 *     5. durable (bit) - request a durable exchange
 *     6. reserved-2 (bit) - deprecated
 *     7. reserved-3 (bit) - deprecated
 *     8. no-wait (bit) - No wait
 *     9. arguments (FieldTable) - arguments for declaration
 */
public class ExchangeDeclare extends MethodFrame {

    private final ShortString exchange;
    private final ShortString type;
    private final boolean passive;
    private final boolean durable;
    private final boolean noWait;
    private final FieldTable arguments;

    public ExchangeDeclare(int channel, ShortString exchange, ShortString type, boolean passive, boolean durable,
            boolean noWait, FieldTable arguments) {
        super(channel, (short) 40, (short) 10);
        this.exchange = exchange;
        this.type = type;
        this.passive = passive;
        this.durable = durable;
        this.noWait = noWait;
        this.arguments = arguments;
    }

    @Override
    protected long getMethodBodySize() {
        return 2L + exchange.getSize() + type.getSize() + 1L + arguments.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeShort(0);
        exchange.write(buf);
        type.write(buf);

        // prepare flags and write
        byte flags = 0x0;
        if (passive) {
            flags |= 0x1;
        }
        if (durable) {
            flags |= 0x2;
        }
        if (noWait) {
            flags |= 0x10;
        }
        buf.writeByte(flags);

        arguments.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        AmqpChannel channel = connectionHandler.getChannel(getChannel());

        try {
            channel.declareExchange(exchange.toString(), type.toString(), passive, durable);
            ctx.writeAndFlush(new ExchangeDeclareOk(getChannel()));
        } catch (BrokerException e) {
            // TODO: update with a channel error rather than a frame error
            ctx.writeAndFlush(new AmqpBadMessage(e));
        }

    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            buf.skipBytes(2);
            ShortString exchange = ShortString.parse(buf);
            ShortString type = ShortString.parse(buf);
            byte flags = buf.readByte();
            boolean passive = (flags & 0x1) == 0x1;
            boolean durable = (flags & 0x2) == 0x2;
            boolean noWait = (flags & 0x10) == 0x10;
            FieldTable arguments = FieldTable.parse(buf);
            return new ExchangeDeclare(channel, exchange, type, passive, durable, noWait, arguments);
        };
    }
}
