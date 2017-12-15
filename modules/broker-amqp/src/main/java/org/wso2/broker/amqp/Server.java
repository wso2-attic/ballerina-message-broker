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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.amqp;

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
import org.wso2.broker.amqp.codec.AmqpConnectionHandler;
import org.wso2.broker.amqp.codec.AmqpDecoder;
import org.wso2.broker.amqp.codec.AmqpEncoder;
import org.wso2.broker.amqp.codec.AmqpMessageWriter;
import org.wso2.broker.amqp.codec.BlockingTaskHandler;
import org.wso2.broker.core.Broker;

/**
 * AMQP Server implementation.
 */
public class Server {

    /**
     * Number of threads used for blocking tasks like I/O operations. Putting a higher number of threads here is OK
     * since these are I/O bound threads.
     * TODO This should be read from
     */
    private static final int BLOCKING_TASK_EXECUTOR_THREADS = 32;
    private final int port;

    private final Broker broker;
    private final String hostname;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private EventExecutorGroup ioExecutors;
    private Channel serverChannel;

    public Server(Broker broker, AmqpServerConfiguration configuration) {
        this.hostname = configuration.getNonSecure().getHostName();
        this.port = Integer.parseInt(configuration.getNonSecure().getPort());
        this.broker = broker;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ioExecutors = new DefaultEventExecutorGroup(BLOCKING_TASK_EXECUTOR_THREADS);
    }

    /**
     * Start the AMQP server.
     *
     * @throws InterruptedException throws Exception when binding to port
     */
    public void run() throws InterruptedException {
        try {
            ChannelFuture f = bindToSocket();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            shutdownExecutors();
        }
    }

    private void shutdownExecutors() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        ioExecutors.shutdownGracefully();
    }

    private ChannelFuture bindToSocket() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new SocketChannelInitializer(ioExecutors))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Bind and start to accept incoming connections.
        return b.bind(hostname, port).sync();
    }

    public void start() throws InterruptedException {
        ChannelFuture channelFuture = bindToSocket();
        serverChannel = channelFuture.channel();
    }

    public void stop() throws InterruptedException {
        try {
            serverChannel.close().sync();
        } finally {
            shutdownExecutors();
        }
    }

    private class SocketChannelInitializer extends ChannelInitializer<SocketChannel> {

        private final EventExecutorGroup ioExecutors;

        public SocketChannelInitializer(EventExecutorGroup ioExecutors) {
            this.ioExecutors = ioExecutors;
        }

        protected void initChannel(SocketChannel socketChannel) {
            socketChannel.pipeline()
                         .addLast(new AmqpDecoder())
                         .addLast(new AmqpEncoder())
                         .addLast(new AmqpConnectionHandler(broker))
                         .addLast(new AmqpMessageWriter())
                         .addLast(ioExecutors, new BlockingTaskHandler());
        }
    }


}
