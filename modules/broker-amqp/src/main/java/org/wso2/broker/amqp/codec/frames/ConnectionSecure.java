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

package org.wso2.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;


import org.wso2.broker.common.data.types.LongString;

/**
 * AMQP connection.secure frame.
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
}
