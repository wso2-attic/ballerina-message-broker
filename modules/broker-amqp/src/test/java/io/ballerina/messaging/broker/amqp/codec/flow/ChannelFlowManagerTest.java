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
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

public class ChannelFlowManagerTest {

    private ChannelFlowManager channelFlowManager;
    private ChannelHandlerContext ctx;
    private ArgumentCaptor<ChannelFlow> argumentCaptor;

    @BeforeMethod
    public void setUp() throws Exception {
        AmqpChannel channel = Mockito.mock(AmqpChannel.class);
        ctx = Mockito.mock(ChannelHandlerContext.class);
        Mockito.when(ctx.channel()).thenReturn(Mockito.mock(Channel.class));
        channelFlowManager = new ChannelFlowManager(channel, 2, 10);

        argumentCaptor = ArgumentCaptor.forClass(ChannelFlow.class);
    }

    @Test
    public void testFlowNotDisabledWhenHighLevelNotExceeded() throws Exception {
        IntStream.rangeClosed(1, 10)
                .forEach(i -> channelFlowManager.notifyMessageAddition(ctx));

        Mockito.verify(ctx, Mockito.never()).writeAndFlush(argumentCaptor.capture());
    }

    @Test
    public void testFlowDisabledWhenHighLevelExceeded() throws Exception {
        IntStream.rangeClosed(1, 11)
                 .forEach(i -> channelFlowManager.notifyMessageAddition(ctx));
        Mockito.verify(ctx, Mockito.times(1)).writeAndFlush(argumentCaptor.capture());
    }

    @Test
    public void testFlowNotEnabledLowLevelIsNotMet() throws Exception {
        IntStream.rangeClosed(1, 11)
                 .forEach(i -> channelFlowManager.notifyMessageAddition(ctx));
        IntStream.rangeClosed(1, 4)
                 .forEach(i -> channelFlowManager.notifyMessageRemoval(ctx));
        // 1 time since flow should have been enabled
        Mockito.verify(ctx, Mockito.times(1)).writeAndFlush(argumentCaptor.capture());
    }

    @Test()
    public void testFlowNEnabledLowLevelIsMet() throws Exception {
        IntStream.rangeClosed(1, 11)
                 .forEach(i -> channelFlowManager.notifyMessageAddition(ctx));
        IntStream.rangeClosed(1, 10)
                 .forEach(i -> channelFlowManager.notifyMessageRemoval(ctx));
        // 2 times since flow is disabled and enables
        Mockito.verify(ctx, Mockito.times(2)).writeAndFlush(argumentCaptor.capture());
    }
}
