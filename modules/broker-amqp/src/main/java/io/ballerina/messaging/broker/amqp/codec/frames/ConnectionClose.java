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
import io.ballerina.messaging.broker.amqp.codec.ConnectionException;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP frame for connection.close
 * Parameter Summary:
 *     1. reply­code (short) - reply code
 *     2. reply-text (ShortString) - reply-text
 *     3. class-id (short) - failing method class
 *     4. method-id (method-id) - failing method ID
 */
public class ConnectionClose extends MethodFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionClose.class);

    private final int replyCode;
    private final ShortString replyText;
    private final int classId;
    private final int methodId;

    public ConnectionClose(int replyCode, ShortString replyText, int classId, int methodId) {
        super(0, (short) 10, (short) 50);
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
        ctx.fireChannelRead((BlockingTask) () -> {
            connectionHandler.closeAllChannels();
            ctx.writeAndFlush(new ConnectionCloseOk(getChannel())).addListener(ChannelFutureListener.CLOSE);
        });
    }

    /**
     * Getter for replyCode.
     */
    public int getReplyCode() {
        return replyCode;
    }

    /**
     * Getter for replyText.
     */
    public ShortString getReplyText() {
        return replyText;
    }

    /**
     * Getter for classId.
     */
    public int getClassId() {
        return classId;
    }

    /**
     * Getter for methodId.
     */
    public int getMethodId() {
        return methodId;
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            int replyCode = buf.readUnsignedShort();
            ShortString replyText = ShortString.parse(buf);
            int classId = buf.readUnsignedShort();
            int methodId = buf.readUnsignedShort();
            return new ConnectionClose(replyCode, replyText, classId, methodId);
        };
    }

    public static ConnectionClose getInstance(short classId, short methodId, ConnectionException exception) {
        int replyCode = exception.getReplyCode();
        ShortString replyText = ShortString.parseString(exception.getMessage());
        return new ConnectionClose(replyCode, replyText, classId, methodId);
    }
}
