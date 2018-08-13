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
 * This class handles idle connections.The class is added to channelHandlerContext when ConnectionTuneOK received.The
 * method userEventTriggered invokes when the channel is idle for negotiated heartbeat interval.
 */
public class IdleConnectionListener extends ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdleConnectionListener.class);
    /*
     * If a peer detects no incoming traffic (i.e. received octets) for two heartbeat intervals or longer, it should
     * close the connection without following the connection.
     */
    private static final int MAXIMUM_HEARTBEAT_COUNT = 2;
    private int heartbeatCount;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {

        if (heartbeatCount == MAXIMUM_HEARTBEAT_COUNT) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Two heartbeats sent and timed out. Closing channel.");
            }
            ctx.channel().close();
        } else {
            heartbeatCount++;
            ctx.writeAndFlush(new HeartbeatFrame());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        resetHeartbeatCount();
        super.channelRead(ctx, msg);
    }

    /**
     * Reset heartbeatCount variable to zero.
     */
    public void resetHeartbeatCount() {

        this.heartbeatCount = 0;
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
