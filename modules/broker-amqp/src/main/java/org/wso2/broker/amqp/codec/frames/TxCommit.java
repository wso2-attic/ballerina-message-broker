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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqpChannel;
import org.wso2.broker.amqp.codec.ChannelException;
import org.wso2.broker.amqp.codec.handlers.AmqpConnectionHandler;
import org.wso2.broker.common.ValidationException;
import org.wso2.broker.common.data.types.ShortString;

/**
 * AMQP frame for tx.commit
 */
public class TxCommit extends MethodFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(TxCommit.class);
    private static final short CLASS_ID = 90;
    private static final short METHOD_ID = 20;

    public TxCommit(int channel) {
        super(channel, CLASS_ID, METHOD_ID);
    }

    @Override
    protected long getMethodBodySize() {
        return 0L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        int channelId = getChannel();
        AmqpChannel channel = connectionHandler.getChannel(channelId);
        try {
            channel.commit();
            ctx.writeAndFlush(new TxCommitOk(channelId));
        } catch (ValidationException e) {
            LOGGER.error("Error while commit transaction", e);
            ctx.writeAndFlush(new ChannelClose(channelId, ChannelException.PRECONDITION_FAILED,
                    ShortString.parseString(e.getMessage()),
                    CLASS_ID,
                    METHOD_ID));
        }
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> new TxCommit(channel);
    }
}
