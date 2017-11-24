/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.broker.amqp.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.frames.AmqpBadMessage;
import org.wso2.broker.amqp.codec.frames.ConnectionStart;
import org.wso2.broker.amqp.codec.frames.MethodFrame;
import org.wso2.broker.amqp.codec.frames.ProtocolInitFrame;
import org.wso2.broker.core.Broker;

import java.util.HashMap;
import java.util.Map;

/**
 * Netty handler for handling an AMQP connection.
 */
public class AmqpConnectionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpConnectionHandler.class);
    private final Map<Integer, AmqpChannel> channels = new HashMap<>();
    private final Broker broker;

    public AmqpConnectionHandler(Broker broker) {
        this.broker = broker;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ProtocolInitFrame) {
            handleProtocolInit(ctx, (ProtocolInitFrame) msg);
        } else if (msg instanceof MethodFrame) {
            ((MethodFrame) msg).handle(ctx, this);
        } else if (msg instanceof AmqpBadMessage) {
            LOGGER.warn("Bad message received", ((AmqpBadMessage) msg).getCause());
            // TODO need to send error back to client
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.warn("Exception while handling request", cause);
        ctx.close();
    }

    private void handleProtocolInit(ChannelHandlerContext ctx, ProtocolInitFrame msg) {
        if (ProtocolInitFrame.V_091.equals(msg)) {
            ctx.writeAndFlush(ConnectionStart.DEFAULT_FRAME);
        } else {
            ctx.writeAndFlush(ProtocolInitFrame.V_091);
        }
    }

    public void createChannel(int channelId) throws Exception {
        AmqpChannel channel = channels.get(channelId);
        if (channel != null) {
            throw new Exception("Channel Already exists");
        }

        channels.put(channelId, new AmqpChannel(broker));
    }

    /**
     * Returns the {@link AmqpChannel} for the specified channelId
     *
     * @param channelId channel id
     * @return AmqpChannel
     */
    public AmqpChannel getChannel(int channelId) {
        return channels.get(channelId);
    }
}
