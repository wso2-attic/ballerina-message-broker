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
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.BrokerAuthException;
import io.ballerina.messaging.broker.core.BrokerException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP frame for exchange.delete
 * Parameter Summary:
 *     1. reserved-1 (short) - deprecated
 *     2. exchange (ShortString) - exchange name
 *     3. ifUnused (bit) - delete only if unused
 *     4. no-wait (bit) - No wait
 */
public class ExchangeDelete extends MethodFrame {

    private static final short CLASS_ID = 40;

    private static final short METHOD_ID = 20;

    private final ShortString exchange;

    private final boolean ifUnused;

    private ExchangeDelete(int channel, ShortString exchangeName, boolean ifUnused) {
        super(channel, CLASS_ID, METHOD_ID);
        this.exchange = exchangeName;
        this.ifUnused = ifUnused;
    }

    @Override
    protected long getMethodBodySize() {
        return 2L + exchange.getSize() + 1L + 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeShort(0);
        exchange.write(buf);
        buf.writeBoolean(ifUnused);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        AmqpChannel channel = connectionHandler.getChannel(getChannel());
        ctx.fireChannelRead((BlockingTask) () -> {
            try {
                channel.deleteExchange(exchange.toString(), ifUnused);
                ctx.writeAndFlush(new ExchangeDeleteOk(getChannel()));
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
            } catch (ResourceNotFoundException e) {
                // For AMQP clients this is not an exception. Respond with message count zero.
                ctx.writeAndFlush(new ExchangeDeleteOk(getChannel()));
            }
        });
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            buf.skipBytes(2);
            ShortString exchangeName = ShortString.parse(buf);
            boolean ifUnused = buf.readBoolean();
            return new ExchangeDelete(channel, exchangeName, ifUnused);
        };
    }
}
