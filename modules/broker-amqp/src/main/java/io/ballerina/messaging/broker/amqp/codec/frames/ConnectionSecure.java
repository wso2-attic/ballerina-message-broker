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
import io.ballerina.messaging.broker.common.data.types.LongString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP frame for connection.secure.
 * Parameter Summary:
 *     1. challenge (LongString) - security challenge data
 */
public class ConnectionSecure extends MethodFrame {

    private final LongString challenge;

    public ConnectionSecure(int channel, LongString challenge) {
        super(channel, (short) 10, (short) 20);
        this.challenge = challenge;
    }

    @Override
    protected long getMethodBodySize() {
        return challenge.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        challenge.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // Server does not normally receive this message
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            LongString challenge = LongString.parse(buf);
            return new ConnectionSecure(channel, challenge);
        };
    }
}
