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

package io.ballerina.messaging.broker.amqp.consumer;

import io.ballerina.messaging.broker.amqp.codec.AmqConstant;
import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.netty.channel.ChannelHandlerContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class AmqpConsumerTest {

    @Test
    public void testGetTransportProperties() throws Exception {
        AmqpChannel amqpChannel = Mockito.mock(AmqpChannel.class);
        ChannelHandlerContext context = Mockito.mock(ChannelHandlerContext.class);
        Broker broker = Mockito.mock(Broker.class);
        Mockito.when(amqpChannel.getChannelId()).thenReturn(5);
        Mockito.when(amqpChannel.getConnectionId()).thenReturn(7);
        AmqpConsumer consumer = new AmqpConsumer(context, broker, amqpChannel, "queue",
                                                 new ShortString(0, new byte[0]), true);
        Properties transportProperties = consumer.getTransportProperties();
        Assert.assertEquals(transportProperties.get(AmqConstant.TRANSPORT_PROPERTY_CHANNEL_ID), 5,
                                                    "Incorrect channel id set");
        Assert.assertEquals(transportProperties.get(AmqConstant.TRANSPORT_PROPERTY_CONNECTION_ID), 7,
                                                    "Incorrect connection id set");
    }

}
