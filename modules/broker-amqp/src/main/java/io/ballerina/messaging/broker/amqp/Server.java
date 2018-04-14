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

package io.ballerina.messaging.broker.amqp;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.ballerina.messaging.broker.amqp.codec.auth.AuthenticationStrategy;
import io.ballerina.messaging.broker.amqp.codec.auth.AuthenticationStrategyFactory;
import io.ballerina.messaging.broker.amqp.codec.frames.AmqMethodRegistryFactory;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpDecoder;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpEncoder;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpMessageWriter;
import io.ballerina.messaging.broker.amqp.codec.handlers.BlockingTaskHandler;
import io.ballerina.messaging.broker.amqp.metrics.AmqpMetricManager;
import io.ballerina.messaging.broker.amqp.metrics.DefaultAmqpMetricManager;
import io.ballerina.messaging.broker.amqp.metrics.NullAmqpMetricManager;
import io.ballerina.messaging.broker.auth.AuthManager;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.coordination.BasicHaListener;
import io.ballerina.messaging.broker.coordination.HaListener;
import io.ballerina.messaging.broker.coordination.HaStrategy;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerFactory;
import io.ballerina.messaging.broker.core.DefaultBrokerFactory;
import io.ballerina.messaging.broker.core.SecureBrokerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.MetricService;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;

/**
 * AMQP Server implementation.
 */
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    /**
     * Number of threads used for blocking tasks like I/O operations. Putting a higher number of threads here is OK
     * since these are I/O bound threads.
     * TODO This should be read from
     */
    private static final int BLOCKING_TASK_EXECUTOR_THREADS = 32;

    private final BrokerFactory brokerFactory;
    private final AmqpServerConfiguration configuration;
    private final AmqpMetricManager metricManager;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventExecutorGroup ioExecutors;
    private Channel plainServerChannel;
    private Channel sslServerChannel;

    /**
     * The {@link HaStrategy} for which the HA listener is registered.
     */
    private HaStrategy haStrategy;

    private ServerHelper serverHelper;

    private AmqMethodRegistryFactory amqMethodRegistryFactory;

    public Server(StartupContext startupContext) throws Exception {
        MetricService metrics = startupContext.getService(MetricService.class);
        if (Objects.nonNull(metrics)) {
            metricManager = new DefaultAmqpMetricManager(metrics);
        } else {
            metricManager = new NullAmqpMetricManager();
        }

        BrokerConfigProvider configProvider = startupContext.getService(BrokerConfigProvider.class);
        this.configuration = configProvider
                .getConfigurationObject(AmqpServerConfiguration.NAMESPACE, AmqpServerConfiguration.class);
        Broker broker = startupContext.getService(Broker.class);

        if (broker == null) {
            throw new RuntimeException("Could not find the broker class to initialize AMQP server");
        }

        AuthManager authManager = startupContext.getService(AuthManager.class);
        if (null != authManager && authManager.isAuthenticationEnabled() && authManager.isAuthorizationEnabled()) {
            brokerFactory = new SecureBrokerFactory(startupContext);
        } else {
            brokerFactory = new DefaultBrokerFactory(startupContext);
        }

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ThreadFactory blockingTaskThreadFactory = new ThreadFactoryBuilder().setNameFormat("NettyBlockingTaskThread-%d")
                                                                            .build();
        ioExecutors = new DefaultEventExecutorGroup(BLOCKING_TASK_EXECUTOR_THREADS, blockingTaskThreadFactory);
        haStrategy = startupContext.getService(HaStrategy.class);
        if (haStrategy == null) {
            serverHelper = new ServerHelper();
        } else {
            LOGGER.info("AMQP Transport is in PASSIVE mode"); //starts up in passive mode
            serverHelper = new HaEnabledServerHelper();
        }

        AuthenticationStrategy authenticationStrategy
                = AuthenticationStrategyFactory.getStrategy(startupContext.getService(AuthManager.class),
                                                                  brokerFactory);
        amqMethodRegistryFactory = new AmqMethodRegistryFactory(authenticationStrategy);
    }

    private void shutdownExecutors() {
        LOGGER.info("Shutting down Netty Executors for AMQP transport");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        ioExecutors.shutdownGracefully();
    }


    public void start() throws InterruptedException, CertificateException, UnrecoverableKeyException,
                               NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        serverHelper.start();
    }

    public void awaitServerClose() throws InterruptedException {
        if (plainServerChannel != null) {
            plainServerChannel.closeFuture().sync();
        }

        if (sslServerChannel != null) {
            sslServerChannel.closeFuture().sync();
        }
    }

    public void stop() {
        try {
            closeChannels();
        } finally {
            shutdownExecutors();
        }
    }

    public void shutdown() {
        serverHelper.shutdown();
    }

    /**
     * Method to close the channels.
     */
    private void closeChannels() {
        if (plainServerChannel != null) {
            plainServerChannel.close();
        }
        if (sslServerChannel != null) {
            sslServerChannel.close();
        }
    }

    private class SocketChannelInitializer extends ChannelInitializer<SocketChannel> {

        private final EventExecutorGroup ioExecutors;

        SocketChannelInitializer(EventExecutorGroup ioExecutors) {
            this.ioExecutors = ioExecutors;
        }

        protected void initChannel(SocketChannel socketChannel) {
            socketChannel.pipeline()
                         .addLast(new AmqpDecoder(amqMethodRegistryFactory.newInstance()))
                         .addLast(new AmqpEncoder())
                         .addLast(new AmqpConnectionHandler(configuration, metricManager))
                         .addLast(ioExecutors, new AmqpMessageWriter())
                         .addLast(ioExecutors, new BlockingTaskHandler());
        }
    }

    private class SslSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

        private final EventExecutorGroup ioExecutors;
        private final SslHandlerFactory sslHandlerFactory;

        SslSocketChannelInitializer(EventExecutorGroup ioExecutors, SslHandlerFactory sslHandlerFactory) {
            this.ioExecutors = ioExecutors;
            this.sslHandlerFactory = sslHandlerFactory;
        }

        protected void initChannel(SocketChannel socketChannel) {
            socketChannel.pipeline()
                         .addLast(sslHandlerFactory.create())
                         .addLast(new AmqpDecoder(amqMethodRegistryFactory.newInstance()))
                         .addLast(new AmqpEncoder())
                         .addLast(new AmqpConnectionHandler(configuration, metricManager))
                         .addLast(ioExecutors, new AmqpMessageWriter())
                         .addLast(ioExecutors, new BlockingTaskHandler());
        }
    }

    private class ServerHelper {

        public void start() throws InterruptedException, CertificateException, UnrecoverableKeyException,
                NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
                IOException {
            ChannelFuture channelFuture = bindToPlainSocket();
            plainServerChannel = channelFuture.channel();

            if (configuration.getSsl().isEnabled()) {
                ChannelFuture sslSocketFuture = bindToSslSocket();
                sslServerChannel = sslSocketFuture.channel();
            }
        }

        private ChannelFuture bindToPlainSocket() throws InterruptedException {
            String hostname = configuration.getHostName();
            int port = Integer.parseInt(configuration.getPlain().getPort());

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new SocketChannelInitializer(ioExecutors))
             .option(ChannelOption.SO_BACKLOG, 128)
             .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture future = b.bind(hostname, port).sync();
            LOGGER.info("Listening AMQP on {}:{}", hostname, port);
            return future;
        }

        private ChannelFuture bindToSslSocket()
                throws InterruptedException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException,
                       KeyStoreException, KeyManagementException, IOException {
            String hostname = configuration.getHostName();
            int port = Integer.parseInt(configuration.getSsl().getPort());

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new SslSocketChannelInitializer(ioExecutors, new SslHandlerFactory(configuration)))
             .option(ChannelOption.SO_BACKLOG, 128)
             .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            ChannelFuture future = b.bind(hostname, port).sync();
            LOGGER.info("Listening AMQP/{} on {}:{}", configuration.getSsl().getProtocol(), hostname, port);
            return future;
        }


        public void shutdown() {
            stop();
        }

    }

    private class HaEnabledServerHelper extends ServerHelper implements HaListener {

        private BasicHaListener basicHaListener;

        HaEnabledServerHelper() {
            basicHaListener = new BasicHaListener(this);
            haStrategy.registerListener(basicHaListener, 2);
        }

        @Override
        public synchronized void start() throws InterruptedException, CertificateException, UnrecoverableKeyException,
                                   NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
                                   IOException {
            basicHaListener.setStartCalled(); //to allow starting when the node becomes active when HA is enabled
            if (!basicHaListener.isActive()) {
                return;
            }
            super.start();
        }

        @Override
        public void shutdown() {
            haStrategy.unregisterListener(basicHaListener);
            super.shutdown();
        }

        /**
         * {@inheritDoc}
         */
        public void activate() {
            startOnBecomingActive();
            LOGGER.info("AMQP Transport mode changed from PASSIVE to ACTIVE");
        }

        /**
         * {@inheritDoc}
         */
        public void deactivate() {
            LOGGER.info("AMQP Transport mode changed from ACTIVE to PASSIVE");
            closeChannels();
        }

        /**
         * Method to start the transport, only if has been called, prior to becoming the active node.
         */
        private synchronized void startOnBecomingActive() {
            if (basicHaListener.isStartCalled()) {
                try {
                    start();
                } catch (InterruptedException | CertificateException | UnrecoverableKeyException
                        | NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
                    LOGGER.error("Error while starting the AMQP Transport ", e);
                }
            }
        }

    }

}
