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

import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.amqp.codec.AmqpChannelFactory;
import io.ballerina.messaging.broker.amqp.codec.BlockingTask;
import io.ballerina.messaging.broker.amqp.codec.ConnectionException;
import io.ballerina.messaging.broker.amqp.codec.frames.AmqpBadMessage;
import io.ballerina.messaging.broker.amqp.codec.frames.ConnectionStart;
import io.ballerina.messaging.broker.amqp.codec.frames.GeneralFrame;
import io.ballerina.messaging.broker.amqp.codec.frames.ProtocolInitFrame;
import io.ballerina.messaging.broker.amqp.metrics.AmqpMetricManager;
import io.ballerina.messaging.broker.core.Broker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Netty handler for handling an AMQP connection.
 */
public class AmqpConnectionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpConnectionHandler.class);
    private final Map<Integer, AmqpChannel> channels = new HashMap<>();
    private Broker broker;
    private final AmqpMetricManager metricManager;

    /**
     * Used to create AMQP channel Objects.
     */
    private AmqpChannelFactory amqpChannelFactory;

    /**
     * Underline netty connection.
     */
    private Channel nettyChannel;

    public AmqpConnectionHandler(AmqpMetricManager metricManager, AmqpChannelFactory amqpChannelFactory) {
        this.metricManager = metricManager;
        this.amqpChannelFactory = amqpChannelFactory;
        metricManager.incrementConnectionCount();
    }

    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        nettyChannel = ctx.channel();
        nettyChannel.closeFuture().addListener(future -> ctx.fireChannelRead((BlockingTask) this::onConnectionClose));
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
            ctx.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        if (!ctx.channel().isWritable()) {
            ctx.channel().config().setAutoRead(false);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Auto read set to false in channel {}", getRemoteAddress(ctx));
            }
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        if (ctx.channel().isWritable()) {
            ctx.channel().config().setAutoRead(true);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Auto read set to true in channel {}", getRemoteAddress(ctx));
            }
        }
    }

    private SocketAddress getRemoteAddress(ChannelHandlerContext ctx) {
        return ctx.channel().remoteAddress();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.warn("Exception while handling request", cause);
        ctx.close();
    }

    private void onConnectionClose() {
        closeAllChannels();
        metricManager.decrementConnectionCount();
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
        channels.put(channelId, amqpChannelFactory.createChannel(broker, channelId, this));
        metricManager.incrementChannelCount();
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

    public void closeChannel(int channelId) {
        AmqpChannel channel = channels.remove(channelId);
        if (Objects.nonNull(channel)) {
            closeChannel(channel);
        }
    }

    private void closeChannel(AmqpChannel channel) {
        metricManager.decrementChannelCount();
        channel.close();
    }

    public void closeAllChannels() {
        for (AmqpChannel channel: channels.values()) {
            closeChannel(channel);
        }

        channels.clear();
    }

    /**
     * Returns the {@link Broker} for the amq connection.
     *
     * @return Broker
     */
    public Broker getBroker() {
        return broker;
    }

    /**
     * Attach relevant broker implementation to the amq connection.
     *
     * @param broker a broker instance
     */
    public void attachBroker(Broker broker) {
        this.broker = broker;
    }

    /**
     * Check whether the underline connection is writable. A connection can become unwritable when the outbound
     * buffer is full.
     *
     * @return true if the connection is writable, false otherwise
     */
    public boolean isWritable() {
        return nettyChannel.isWritable();
    }
}
