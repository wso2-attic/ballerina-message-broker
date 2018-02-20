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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP frame for basic.consume
 * Parameter Summary:
 *     1. consumer­tag (ShortString) - consumer tag
 */
public class BasicConsumeOk extends MethodFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicConsumeOk.class);

    private final ShortString consumerTag;

    public BasicConsumeOk(int channel, ShortString consumerTag) {
        super(channel, (short) 60, (short) 21);
        this.consumerTag = consumerTag;
    }

    @Override
    protected long getMethodBodySize() {
        return consumerTag.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        consumerTag.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // Server does not handle
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            ShortString consumerTag = ShortString.parse(buf);
            return new BasicConsumeOk(channel, consumerTag);
        };
    }
}
