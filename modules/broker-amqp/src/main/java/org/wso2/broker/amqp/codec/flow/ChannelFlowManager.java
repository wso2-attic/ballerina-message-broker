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

package org.wso2.broker.amqp.codec.flow;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqpChannel;
import org.wso2.broker.amqp.codec.frames.ChannelFlow;
import org.wso2.broker.amqp.codec.handlers.AmqpMessageWriter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ChannelFlowManager is responsible for managing flow rate of publishers. The flow should be disabled and enabled
 * depending on the server load.
 */
public class ChannelFlowManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelFlowManager.class);

    private final int highLimit;
    private final int lowLimit;
    private final AtomicInteger messagesOnFlight = new AtomicInteger(0);
    private final AtomicBoolean flowActive = new AtomicBoolean(true);
    private final AmqpChannel channel;

    public ChannelFlowManager(AmqpChannel channel, int lowLimit, int highLimit) {
        this.channel = channel;
        this.lowLimit = lowLimit;
        this.highLimit = highLimit;
    }

    public void notifyMessageAddition(ChannelHandlerContext ctx) {
        int unprocessedMessages = messagesOnFlight.incrementAndGet();
        if (unprocessedMessages > highLimit && flowActive.compareAndSet(true, false)) {
            ctx.writeAndFlush(new ChannelFlow(channel.getChannelId(), false));
            channel.setFlow(false);
            LOGGER.info("Flow disabled for channel {}-{}", channel.getChannelId(), ctx.channel().remoteAddress());
        }
    }

    public void notifyMessageRemoval(ChannelHandlerContext ctx) {
        int unprocessedMessages = messagesOnFlight.decrementAndGet();
        if (unprocessedMessages < lowLimit && flowActive.compareAndSet(false, true)) {
            channel.setFlow(true);
            ctx.write(new ChannelFlow(channel.getChannelId(), true));
            AmqpMessageWriter.write(ctx.channel(), channel.getPendingMessages());
            ctx.flush();
            LOGGER.info("Flow enabled for channel {}-{}", channel.getChannelId(), ctx.channel().remoteAddress());
        }
    }
}
