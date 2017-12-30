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

package org.wso2.broker.amqp.codec.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqpChannel;
import org.wso2.broker.amqp.codec.BlockingTask;
import org.wso2.broker.amqp.codec.ConnectionException;
import org.wso2.broker.amqp.codec.frames.AmqpBadMessage;
import org.wso2.broker.amqp.codec.frames.ConnectionStart;
import org.wso2.broker.amqp.codec.frames.GeneralFrame;
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

    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().closeFuture().addListener(future -> closeConnection(ctx));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ProtocolInitFrame) {
            handleProtocolInit(ctx, (ProtocolInitFrame) msg);
        } else if (msg instanceof GeneralFrame) {
            ((GeneralFrame) msg).handle(ctx, this);
        } else if (msg instanceof AmqpBadMessage) {
            LOGGER.warn("Bad message received", ((AmqpBadMessage) msg).getCause());
            // TODO need to send error back to client
            closeConnection(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.warn("Exception while handling request", cause);
        closeConnection(ctx);
    }

    private ChannelHandlerContext closeConnection(ChannelHandlerContext ctx) {
        return ctx.fireChannelRead((BlockingTask) () -> {
            try {
                closeAllChannels();
            } finally {
                ctx.close();
            }
        });
    }

    private void handleProtocolInit(ChannelHandlerContext ctx, ProtocolInitFrame msg) {
        if (ProtocolInitFrame.V_091.equals(msg)) {
            ctx.writeAndFlush(ConnectionStart.DEFAULT_FRAME);
        } else {
            ctx.writeAndFlush(ProtocolInitFrame.V_091);
        }
    }

    public void createChannel(int channelId) throws ConnectionException {
        AmqpChannel channel = channels.get(channelId);
        if (channel != null) {
            throw new ConnectionException(ConnectionException.CHANNEL_ERROR,
                                          "Channel ID " + channelId + " Already exists");
        }
        channels.put(channelId, new AmqpChannel(broker, channelId));
    }

    /**
     * Returns the {@link AmqpChannel} for the specified channelId.
     *
     * @param channelId channel id
     * @return AmqpChannel
     */
    public AmqpChannel getChannel(int channelId) {
        return channels.get(channelId);
    }

    public void closeChannel(int channel) {
        channels.remove(channel);
    }

    public void closeAllChannels() {
        for (AmqpChannel channel: channels.values()) {
            channel.close();
        }
    }
}
