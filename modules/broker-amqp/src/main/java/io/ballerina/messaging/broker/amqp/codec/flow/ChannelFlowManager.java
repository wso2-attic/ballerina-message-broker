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

package io.ballerina.messaging.broker.amqp.codec.flow;

import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.amqp.codec.frames.ChannelFlow;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ChannelFlowManager is responsible for managing flow rate of publishers. The flow should be disabled and enabled
 * depending on the server load.
 */
public class ChannelFlowManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelFlowManager.class);

    private final int highLimit;
    private final int lowLimit;
    private int messagesInFlight = 0;
    private boolean inflowEnabled = true;
    private final AmqpChannel channel;

    public ChannelFlowManager(AmqpChannel channel, int lowLimit, int highLimit) {
        this.channel = channel;
        this.lowLimit = lowLimit;
        this.highLimit = highLimit;
    }

    public void notifyMessageAddition(ChannelHandlerContext ctx) {
        messagesInFlight++;
        if (messagesInFlight > highLimit && inflowEnabled) {
            inflowEnabled = false;
            ctx.writeAndFlush(new ChannelFlow(channel.getChannelId(), false));
            LOGGER.info("Inflow disabled for channel {}-{}", channel.getChannelId(), ctx.channel().remoteAddress());
        }
    }

    public void notifyMessageRemoval(ChannelHandlerContext ctx) {
        messagesInFlight--;
        if (messagesInFlight < lowLimit && !inflowEnabled) {
            inflowEnabled = true;
            ctx.writeAndFlush(new ChannelFlow(channel.getChannelId(), true));
            LOGGER.info("Inflow enabled for channel {}-{}", channel.getChannelId(), ctx.channel().remoteAddress());
        }
    }
}
