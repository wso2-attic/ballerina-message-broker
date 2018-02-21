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

import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP frame for basic.deliver.
 * Parameter Summary:
 *     1. consumer­tag (ShortString) - consumer tag
 *     2. delivery­tag (longlong) - delivery tag
 *     3. redelivered (bit) - redelivered
 *     4. exchange (ShortString) - exchange name
 *     5. routing­key (ShortString) - Message routing key
 */
public class BasicDeliver extends MethodFrame {

    private final ShortString consumerTag;
    private final long deliveryTag;
    private final boolean redelivered;
    private final ShortString exchange;
    private final ShortString routingKey;


    public BasicDeliver(int channel, ShortString consumerTag, long deliveryTag, boolean redelivered,
            ShortString exchange, ShortString routingKey) {
        super(channel, (short) 60, (short) 60);
        this.consumerTag = consumerTag;
        this.deliveryTag = deliveryTag;
        this.redelivered = redelivered;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    protected long getMethodBodySize() {
        return consumerTag.getSize() + 8L + 1L + exchange.getSize() + routingKey.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        consumerTag.write(buf);
        buf.writeLong(deliveryTag);
        buf.writeBoolean(redelivered);
        exchange.write(buf);
        routingKey.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // Server does not handle basic deliver
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            ShortString consumerTag = ShortString.parse(buf);
            long deliveryTag = buf.readLong();
            boolean redelivered = buf.readBoolean();
            ShortString exchange = ShortString.parse(buf);
            ShortString routingKey = ShortString.parse(buf);
            return new BasicDeliver(channel, consumerTag, deliveryTag, redelivered, exchange, routingKey);
        };
    }
}
