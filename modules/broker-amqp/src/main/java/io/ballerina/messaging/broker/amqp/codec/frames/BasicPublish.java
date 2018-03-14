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
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP frame for basic.publish
 * Parameter Summary:
 *     1. reserved-1 (short) - deprecated
 *     2. exchange (ShortString) - exchange name
 *     3. routing­key (ShortString) - message routing key
 *     4. mandatory (bit) - indicate mandatory routing
 *     5. immediate (bit) - request immediate delivery
 */
public class BasicPublish extends MethodFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicPublish.class);
    public static final short CLASS_ID = 60;
    public static final short METHOD_ID = 40;

    private final ShortString exchange;
    private final ShortString routingKey;
    private final boolean mandatory;
    private final boolean immediate;

    public BasicPublish(int channel, ShortString exchange, ShortString routingKey, boolean mandatory,
            boolean immediate) {
        super(channel, CLASS_ID, METHOD_ID);
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.mandatory = mandatory;
        this.immediate = immediate;
    }

    @Override
    protected long getMethodBodySize() {
        return 2L + exchange.getSize() + routingKey.getSize() + 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeShort(0);
        exchange.write(buf);
        routingKey.write(buf);

        byte flags = 0x0;
        if (mandatory) {
            flags |= 0x1;
        }
        if (immediate) {
            flags |= 0x2;
        }
        buf.writeByte(flags);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        AmqpChannel channel = connectionHandler.getChannel(getChannel());

        channel.getFlowManager().notifyMessageAddition(ctx);
        channel.getMessageAggregator().basicPublishReceived(routingKey, exchange);
    }

    /**
     * Getter for exchange.
     */
    public ShortString getExchange() {
        return exchange;
    }

    /**
     * Getter for routingKey.
     */
    public ShortString getRoutingKey() {
        return routingKey;
    }

    /**
     * Getter for mandatory.
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Getter for immediate.
     */
    public boolean isImmediate() {
        return immediate;
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            buf.skipBytes(2);
            ShortString exchange = ShortString.parse(buf);
            ShortString routingKey = ShortString.parse(buf);
            byte flags = buf.readByte();
            boolean mandatory = (flags & 0x1) == 0x1;
            boolean immediate = (flags & 0x2) == 0x2;
            return new BasicPublish(channel, exchange, routingKey, mandatory, immediate);
        };
    }
}
