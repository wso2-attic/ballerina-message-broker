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
 * AMQP frame for channel.flow-ok.
 * Parameter Summary:
 *     1.active (bit) - current flow setting
 */
public class ChannelFlowOk extends MethodFrame {

    private final boolean active;

    ChannelFlowOk(int channel, boolean active) {
        super(channel, (short) 20, (short) 21);
        this.active = active;
    }

    @Override
    protected long getMethodBodySize() {
        return 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeBoolean(active);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // TODO handle channel flow-ok. maybe we need to signal a future
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            boolean active = buf.readBoolean();
            return new ChannelFlowOk(channel, active);
        };
    }
}
