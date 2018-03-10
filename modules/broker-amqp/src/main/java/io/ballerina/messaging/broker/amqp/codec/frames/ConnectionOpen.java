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
 * AMQP frame for connection.open.
 * Parameter Summary:
 *     1. virtual­host (path) - virtual host name
 *     2. reserved-1 (byte) - deprecated param
 *     3. reserved-2 (byte) - deprecated param
 */
public class ConnectionOpen extends MethodFrame {
    private final ShortString virtualHost;

    public ConnectionOpen(ShortString virtualHost) {
        super(0, (short) 10, (short) 40);
        this.virtualHost = virtualHost;
    }

    @Override
    protected long getMethodBodySize() {
        return virtualHost.getSize() + 1L + 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        virtualHost.write(buf);
        buf.writeByte(0);
        buf.writeByte(1);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        ctx.writeAndFlush(new ConnectionOpenOk());
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            ShortString virtualHost = ShortString.parse(buf);

            // read the size of deprecated short string value
            short stringSize = buf.readUnsignedByte();
            // skip the other deprecated byte as well
            buf.skipBytes(stringSize + 1);
            return new ConnectionOpen(virtualHost);
        };
    }
}
