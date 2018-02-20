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
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP frame for connection.tune-ok.
 *
 * Parameter Summary:
 *     1. channel­max (short) - proposed maximum channels
 *     2. frame­max (long) - proposed maximum frame size
 *     3. heartbeat (short) - desired heartbeat delay
 */
public class ConnectionTuneOk extends MethodFrame {
    private final int channelMax;
    private final long frameMax;
    private final int heartbeat;

    public ConnectionTuneOk(int channelMax, long frameMax, int heartbeat) {
        super(0, (short) 10, (short) 30);
        this.channelMax = channelMax;
        this.frameMax = frameMax;
        this.heartbeat = heartbeat;
    }

    @Override
    protected long getMethodBodySize() {
        return 2L + 4L + 2L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeShort(channelMax);
        buf.writeInt((int) frameMax);
        buf.writeShort(heartbeat);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // TODO add tuning logic
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            int channelMax = buf.readUnsignedShort();
            long frameMax = buf.readUnsignedInt();
            int heartbeat = buf.readUnsignedShort();
            return new ConnectionTuneOk(channelMax, frameMax, heartbeat);
        };
    }
}
