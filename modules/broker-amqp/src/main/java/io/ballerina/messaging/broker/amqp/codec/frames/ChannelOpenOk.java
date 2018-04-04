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
 * AMQP frame for channel.open-ok
 * Parameter Summary:
 *     1. reserved-1 (LongString) - deprecated param
 */
public class ChannelOpenOk extends MethodFrame {

    ChannelOpenOk(int channel) {
        super(channel, (short) 20, (short) 11);
    }

    @Override
    protected long getMethodBodySize() {
        return 4L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        // write deprecated LongString size as 0
        buf.writeInt(0);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // Server does not handle channel open ok
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            // read the size of deprecated short string value
            int stringSize = buf.readInt();
            buf.skipBytes(stringSize);
            return new ChannelOpenOk(channel);
        };
    }
}
