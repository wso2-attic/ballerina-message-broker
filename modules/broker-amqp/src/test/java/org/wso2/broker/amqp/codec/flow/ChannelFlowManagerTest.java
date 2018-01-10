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

package org.wso2.broker.amqp.codec.flow;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.broker.amqp.codec.AmqpChannel;

import java.util.stream.IntStream;

public class ChannelFlowManagerTest {

    private ChannelFlowManager channelFlowManager;
    private ChannelHandlerContext ctx;
    private AmqpChannel channel;

    @BeforeMethod
    public void setUp() throws Exception {
        channel = Mockito.mock(AmqpChannel.class);
        ctx = Mockito.mock(ChannelHandlerContext.class);
        Mockito.when(ctx.channel()).thenReturn(Mockito.mock(Channel.class));
        channelFlowManager = new ChannelFlowManager(channel, 2, 10);
    }

    @Test
    public void testFlowNotDisabledWhenHighLevelNotExceeded() throws Exception {
        IntStream.rangeClosed(1, 10)
                .forEach(i -> channelFlowManager.notifyMessageAddition(ctx));
        Mockito.verify(channel, Mockito.never()).setFlow(false);
    }

    @Test
    public void testFlowDisabledWhenHighLevelExceeded() throws Exception {
        IntStream.rangeClosed(1, 11)
                 .forEach(i -> channelFlowManager.notifyMessageAddition(ctx));
        Mockito.verify(channel, Mockito.times(1)).setFlow(false);
    }

    @Test
    public void testFlowNotEnabledLowLevelIsNotMet() throws Exception {
        IntStream.rangeClosed(1, 11)
                 .forEach(i -> channelFlowManager.notifyMessageAddition(ctx));
        IntStream.rangeClosed(1, 4)
                 .forEach(i -> channelFlowManager.notifyMessageRemoval(ctx));
        Mockito.verify(channel, Mockito.never()).setFlow(true);
    }

    @Test
    public void testFlowNEnabledLowLevelIsMet() throws Exception {
        IntStream.rangeClosed(1, 11)
                 .forEach(i -> channelFlowManager.notifyMessageAddition(ctx));
        IntStream.rangeClosed(1, 10)
                 .forEach(i -> channelFlowManager.notifyMessageRemoval(ctx));
        Mockito.verify(channel, Mockito.times(1)).setFlow(true);
    }
}
