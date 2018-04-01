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
import io.ballerina.messaging.broker.amqp.consumer.AmqpDeliverMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP frame for channel.flow.
 * Parameter Summary:
 *     1.active (bit) - current flow setting
 */
public class ChannelFlow extends MethodFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelFlow.class);

    private final boolean active;

    public ChannelFlow(int channel, boolean active) {
        super(channel, (short) 20, (short) 20);
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
        AmqpChannel channel = connectionHandler.getChannel(getChannel());
        channel.setFlow(active);
        LOGGER.debug("Channel.flow method received. ChannelId: {} active: {} ", getChannel(), active);
        ctx.write(new ChannelFlowOk(getChannel(), active));

        if (active) {
            for (AmqpDeliverMessage message : channel.getPendingMessages()) {
                ctx.channel().write(message);
            }
        }
        ctx.flush();
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            boolean active = buf.readBoolean();
            return new ChannelFlow(channel, active);
        };
    }
}
