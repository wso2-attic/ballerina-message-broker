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

import io.ballerina.messaging.broker.amqp.AmqpConnectionManager;
import io.ballerina.messaging.broker.amqp.codec.AmqConstant;
import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.amqp.codec.AmqpChannelFactory;
import io.ballerina.messaging.broker.amqp.codec.AmqpChannelView;
import io.ballerina.messaging.broker.amqp.codec.AmqpChannelWrapper;
import io.ballerina.messaging.broker.amqp.codec.BlockingTask;
import io.ballerina.messaging.broker.amqp.codec.ConnectionException;
import io.ballerina.messaging.broker.amqp.codec.frames.AmqpBadMessage;
import io.ballerina.messaging.broker.amqp.codec.frames.ChannelClose;
import io.ballerina.messaging.broker.amqp.codec.frames.ConnectionClose;
import io.ballerina.messaging.broker.amqp.codec.frames.ConnectionStart;
import io.ballerina.messaging.broker.amqp.codec.frames.GeneralFrame;
import io.ballerina.messaging.broker.amqp.codec.frames.ProtocolInitFrame;
import io.ballerina.messaging.broker.amqp.metrics.AmqpMetricManager;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Netty handler for handling an AMQP connection.
 */
public class AmqpConnectionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpConnectionHandler.class);
    private final Map<Integer, AmqpChannel> channels = new HashMap<>();

    /**
     * Holds the views of AMQP channel with disabled operations on them.
     * <p>
     * A {@link LinkedHashMap} is used since the channels need to be sorted according to their channel Id.
     */
    private final Map<Integer, AmqpChannelView> channelViews = new LinkedHashMap<>();

    private Broker broker;
    private final AmqpMetricManager metricManager;
    private ChannelHandlerContext ctx;

    /**
     * Connection identifier which uniquely defines a connection.
     */
    private final int id;

    /**
     * Generates a unique identifier for each connection handler.
     */
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    /**
     * Stores the time at which the connection was established.
     */
    private final long connectedTime;

    /**
     * Stores the remote address of the AMQP client in the form of /ip:port that is connected to the broker.
     */
    private String remoteAddress;

    /**
     * The underlying connection manager that is responsible for managing all AMQP connections through the REST API.
     */
    private final AmqpConnectionManager connectionManager;

    /**
     * Used to create AMQP channel Objects.
     */
    private AmqpChannelFactory amqpChannelFactory;

    /**
     * Underline netty connection.
     */
    private Channel nettyChannel;

    public AmqpConnectionHandler(AmqpMetricManager metricManager, AmqpChannelFactory amqpChannelFactory,
                                 AmqpConnectionManager amqpConnectionManager) {
        this.metricManager = metricManager;
        this.amqpChannelFactory = amqpChannelFactory;
        this.connectionManager = amqpConnectionManager;
        metricManager.incrementConnectionCount();
        this.connectedTime = System.currentTimeMillis();
        this.id = ID_GENERATOR.incrementAndGet();
    }

    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        nettyChannel = ctx.channel();
        this.ctx = ctx;
        nettyChannel.closeFuture().addListener(future -> ctx.fireChannelRead((BlockingTask) this::onConnectionClose));
        remoteAddress = ctx.channel().remoteAddress().toString();
        ctx.fireChannelRead((BlockingTask) () -> connectionManager.addConnectionHandler(this));
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
        connectionManager.removeConnectionHandler(this);
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
        channel = amqpChannelFactory.createChannel(broker, channelId, this);
        channels.put(channelId, channel);
        channelViews.put(channelId, new AmqpChannelWrapper(channel));
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

    /**
     * Returns the remote address of the connection that is established.
     *
     * @return the remote address in the format of /ip:port
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Returns the number of AMQP channels that are created using the underlying connection.
     *
     * @return the number of AMQP channels registered
     */
    public int getChannelCount() {
        return channels.size();
    }

    /**
     * Returns the time at which the connection was registered.
     *
     * @return the timestamp at which the connection was created in the form of milliseconds
     */
    public long getConnectedTime() {
        return connectedTime;
    }

    /**
     * Returns the connection identifier.
     *
     * @return an integer defining the connection handler
     */
    public int getId() {
        return id;
    }

    /**
     * Closes the AMQP connection with a client according to the parameters set.
     *
     * @param reason reason to close connection
     * @param force  if set to true the connection will be closed by the broker without negotiating with the AMQP client
     * @param used   if set to true, the connection will be closed regardless of the number of active channels
     *               registered
     * @return int representing the number of channels registered on the connection
     */
    public int closeConnection(String reason, boolean force, boolean used) throws ValidationException {
        int numberOfChannels = channels.size();
        if (!used && numberOfChannels > 0) {
            throw new ValidationException("Cannot close connection. " + numberOfChannels + " active channels exist "
                                          + "and used parameter is not set.");
        }
        if (force) {
            forceCloseConnection(reason);
        } else {
            closeConnection(reason);
        }
        return numberOfChannels;
    }

    /**
     * Sends a connection close frame to the client.
     *
     * @param reason reason to close connection
     */
    private void closeConnection(String reason) {
        LOGGER.info("Closing connection {}. Reason: {}", getId(), reason);
        ctx.writeAndFlush(new ConnectionClose(AmqConstant.CONNECTION_FORCED,
                                              ShortString.parseString("Broker forced close connection. " + reason),
                                              0, 0));
    }

    /**
     * Closes the underlying connection with the client.
     *
     * @param reason reason to force close connection
     */
    private void forceCloseConnection(String reason) {
        LOGGER.info("Force closing connection {}. Reason: {}", getId(), reason);
        ChannelFuture close = ctx.close();
        close.addListener(future -> {
            if (future.isSuccess()) {
                LOGGER.info("Connection {} forcefully closed successfully.", getId());
            } else {
                LOGGER.error("Error occurred while closing connection {}", getId(), future.cause());
            }
        });
    }

    /**
     * Sends a channel close frame to the client.
     *
     * @param channelId the identifier of the channel
     * @param used      if set to true, the channel will be closed regardless of the number of active consumers
     *                  registered
     * @param reason    reason to disconnection channel
     */
    public void closeChannel(int channelId, boolean used, String reason) throws ValidationException {
        int numberOfConsumers = channelViews.get(channelId).getConsumerCount();
        if (!used && numberOfConsumers > 0) {
            throw new ValidationException("Cannot close channel. " + numberOfConsumers + " active consumers exist "
                                          + "and used parameter is not set.");
        }
        LOGGER.info("Closing channel {} of connection {}. Reason: {}", channelId, getId(), reason);
        ctx.writeAndFlush(new ChannelClose(channelId, AmqConstant.CHANNEL_CLOSED,
                                           ShortString.parseString("Broker forced close channel. " + reason),
                                           0, 0));
    }

    /**
     * Retrieves a view of AMQP channels registered with disabled operations on channels.
     *
     * @return {@link AmqpChannelView} representing the view of actual AMQP channels
     */
    public Collection<AmqpChannelView> getChannelViews() {
        return channelViews.values();
    }
}
