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

package io.ballerina.messaging.broker.amqp.codec.handlers;

import io.ballerina.messaging.broker.amqp.codec.frames.HeartbeatFrame;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles idle connections.
 */
public class IdleConnectionListener extends ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdleConnectionListener.class);
    private int heartbeatCount;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {

        heartbeatCount++;
        if (heartbeatCount == 3) {
            LOGGER.info("Two heartbeats sent and timed out.Closing channel.");
            ctx.channel().close();
        }

        if (ctx.channel().isWritable()) {
            // Send a heartbeat frame to the client
            ctx.writeAndFlush(new HeartbeatFrame());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        IdleConnectionListener idleConnectionListener = (IdleConnectionListener) (ctx.pipeline().context
                ("idleConnectionListener").handler());
        idleConnectionListener.setHeartbeatCount(0);
    }

    /**
     * Set heartbeatCount variable.
     *
     * @param value new value to set for heartbeatCount variable
     */
    public void setHeartbeatCount(int value) {

        this.heartbeatCount = value;
    }

    /**
     * Get value of the variable heartbeatCount.
     *
     * @return The value of the heartbeatCount variable
     */
    public int getHeartbeatCount() {

        return this.heartbeatCount;
    }

}
