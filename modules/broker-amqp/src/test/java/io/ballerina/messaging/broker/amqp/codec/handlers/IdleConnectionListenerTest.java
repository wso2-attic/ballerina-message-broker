/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.times;

public class IdleConnectionListenerTest {

    @Test
    public void testIdleConnectionListener() {

        ChannelHandlerContext context = Mockito.mock(ChannelHandlerContext.class);
        Channel channel = Mockito.mock(Channel.class);
        IdleConnectionListener idleConnectionListener = new IdleConnectionListener();
        Mockito.when(context.channel()).thenReturn(channel);
        Mockito.when(context.channel().isWritable()).thenReturn(false);
        Mockito.when(context.channel().close()).thenReturn(null);
        for (int i = 0; i < 3; i++) {
            idleConnectionListener.userEventTriggered(context, null);
        }
        Mockito.verify(context.channel(), times(1)).close();
    }
}
