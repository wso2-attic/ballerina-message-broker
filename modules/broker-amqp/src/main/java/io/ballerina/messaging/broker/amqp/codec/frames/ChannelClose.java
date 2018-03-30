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
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP frame for channel.close
 * Parameter Summary:
 *     1. reply­code (short) - reply code
 *     2. reply-text (ShortString) - reply-text
 *     3. class-id (short) - failing method class
 *     4. method-id (method-id) - failing method ID
 */
public class ChannelClose extends MethodFrame {

    private final int replyCode;
    private final ShortString replyText;
    private final int classId;
    private final int methodId;

    public ChannelClose(int channel, int replyCode, ShortString replyText, int classId, int methodId) {
        super(channel, (short) 20, (short) 40);
        this.replyCode = replyCode;
        this.replyText = replyText;
        this.classId = classId;
        this.methodId = methodId;
    }

    @Override
    protected long getMethodBodySize() {
        return 2L + replyText.getSize() + 2L + 2L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeShort(replyCode);
        replyText.write(buf);
        buf.writeShort(classId);
        buf.writeShort(methodId);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        int channelId = getChannel();
        ctx.fireChannelRead((BlockingTask) () -> {
            connectionHandler.closeChannel(channelId);
            ctx.writeAndFlush(new ChannelCloseOk(channelId));
        });
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            int replyCode = buf.readUnsignedShort();
            ShortString replyText = ShortString.parse(buf);
            int classId = buf.readUnsignedShort();
            int methodId = buf.readUnsignedShort();
            return new ChannelClose(channel, replyCode, replyText, classId, methodId);
        };
    }
}
