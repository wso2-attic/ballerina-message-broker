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

import io.ballerina.messaging.broker.amqp.codec.BlockingTask;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP frame for channel.close-ok.
 * Parameter Summary:
 *     No parameters
 */
public class ChannelCloseOk extends MethodFrame {

    ChannelCloseOk(int channel) {
        super(channel, (short) 20, (short) 41);
    }

    @Override
    protected long getMethodBodySize() {
        return 0L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        // No fields to be written.
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        ctx.fireChannelRead((BlockingTask) () -> connectionHandler.closeChannel(getChannel()));
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> new ChannelCloseOk(channel);
    }
}
