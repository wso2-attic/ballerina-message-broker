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

    private static final short CLASS_ID = 40;
    private static final short METHOD_ID = 10;
    private final ShortString exchange;
    private final ShortString type;
    private final boolean passive;
    private final boolean durable;
    private final boolean noWait;
    private final FieldTable arguments;

    public ExchangeDeclare(int channel, ShortString exchange, ShortString type, boolean passive, boolean durable,
            boolean noWait, FieldTable arguments) {
        super(channel, CLASS_ID, METHOD_ID);
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

        ctx.fireChannelRead((BlockingTask) () -> {
            try {
                channel.declareExchange(exchange.toString(), type.toString(), passive, durable);
                ctx.writeAndFlush(new ExchangeDeclareOk(getChannel()));
            } catch (BrokerAuthException e) {
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
            } catch (ValidationException e) {
                ctx.writeAndFlush(new ChannelClose(getChannel(),
                                                   ChannelException.PRECONDITION_FAILED,
                                                   ShortString.parseString(e.getMessage()),
                                                   CLASS_ID,
                                                   METHOD_ID));
            }
        });
    }

    /**
     * Getter for exchange.
     */
    public ShortString getExchange() {
        return exchange;
    }

    /**
     * Getter for type.
     */
    public ShortString getType() {
        return type;
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
