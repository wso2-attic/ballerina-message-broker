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
import io.ballerina.messaging.broker.amqp.codec.BlockingTask;
import io.ballerina.messaging.broker.amqp.codec.ChannelException;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP frame for basic.cancel.
 */
public class BasicCancel extends MethodFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicCancel.class);

    private static final int CLASS_ID = 60;

    private static final int METHOD_ID = 30;

    private final ShortString consumerTag;

    private final boolean noWait;

    public BasicCancel(int channel, ShortString consumerTag, boolean noWait) {
        super(channel, (short) 60, (short) 30);
        this.consumerTag = consumerTag;
        this.noWait = noWait;
    }

    @Override
    protected long getMethodBodySize() {
        return consumerTag.getSize() + 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        consumerTag.write(buf);
        buf.writeBoolean(noWait);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        AmqpChannel channel = connectionHandler.getChannel(getChannel());

        ctx.fireChannelRead((BlockingTask) () -> {
            try {
                channel.cancelConsumer(consumerTag);
                ctx.writeAndFlush(new BasicCancelOk(getChannel(), consumerTag));
            } catch (ChannelException e) {
                LOGGER.error("Error occurred while closing consumer.", e);
                ctx.writeAndFlush(new ChannelClose(getChannel(), e.getReplyCode(),
                        ShortString.parseString(e.getMessage()), CLASS_ID, METHOD_ID));
            }
        });
    }

    /**
     * Getter for consumerTag.
     */
    public ShortString getConsumerTag() {
        return consumerTag;
    }

    /**
     * Getter for noWait.
     */
    public boolean isNoWait() {
        return noWait;
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            ShortString consumerTag = ShortString.parse(buf);
            boolean noWait = buf.readBoolean();
            return new BasicCancel(channel, consumerTag, noWait);
        };
    }
}
