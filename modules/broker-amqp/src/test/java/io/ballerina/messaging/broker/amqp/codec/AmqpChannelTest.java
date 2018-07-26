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

package io.ballerina.messaging.broker.amqp.codec;

import io.ballerina.messaging.broker.amqp.AmqpServerConfiguration;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.amqp.metrics.AmqpMetricManager;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.transaction.DistributedTransaction;
import io.ballerina.messaging.broker.core.transaction.LocalTransaction;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AmqpChannelTest {

    AmqpChannel amqpChannel;

    @BeforeMethod
    public void setUp() {
        AmqpServerConfiguration configuration = Mockito.mock(AmqpServerConfiguration.class);

        Broker broker = Mockito.mock(Broker.class);
        LocalTransaction localTransaction = Mockito.mock(LocalTransaction.class);
        Mockito.when(broker.newLocalTransaction()).thenReturn(localTransaction);
        DistributedTransaction distributedTransaction = Mockito.mock(DistributedTransaction.class);
        Mockito.when(broker.newDistributedTransaction()).thenReturn(distributedTransaction);

        int channelId = 1;
        AmqpMetricManager metricManager = Mockito.mock(AmqpMetricManager.class);
        AmqpConnectionHandler connection = Mockito.mock(AmqpConnectionHandler.class);

        AmqpServerConfiguration.FlowDetails flowDetails = Mockito.mock(AmqpServerConfiguration.FlowDetails.class);
        Mockito.when(flowDetails.getHighLimit()).thenReturn(1000);
        Mockito.when(flowDetails.getLowLimit()).thenReturn(50);
        Mockito.when(configuration.getChannelFlow()).thenReturn(flowDetails);

        amqpChannel = new AmqpChannel(configuration, broker, channelId, metricManager, connection);

    }

    @Test
    public void testGetTransactionType() throws Exception {

        Assert.assertEquals(amqpChannel.getTransactionType(), "AutoCommit");
        amqpChannel.setLocalTransactional();
        Assert.assertEquals(amqpChannel.getTransactionType(), "LocalTransaction");
        amqpChannel.setDistributedTransactional();
        Assert.assertEquals(amqpChannel.getTransactionType(), "DistributedTransaction");
    }

}
