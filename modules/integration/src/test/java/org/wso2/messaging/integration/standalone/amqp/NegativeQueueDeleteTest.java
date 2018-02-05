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

package org.wso2.messaging.integration.standalone.amqp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.wso2.messaging.integration.util.ClientHelper;

import java.io.IOException;

/**
 * Test unsuccessful queue delete operations. Client throws {@link IOException} on failure.
 */
public class NegativeQueueDeleteTest {

    private Connection amqpConnection;

    private String queueWithConsumers = "QueueWithConsumers";

    private String queueWithMessages = "QueueWithMessages";

    @Parameters({"broker-hostname", "broker-port", "admin-username", "admin-password"})
    @BeforeClass
    public void setUp(String hostname, String port, String username, String password) throws Exception {
        amqpConnection = ClientHelper.getAmqpConnection(username, password, hostname, port);
        Channel channel = amqpConnection.createChannel();

        channel.queueDeclare(queueWithConsumers, false, false, false, null);
        channel.queueDeclare(queueWithMessages, false, false, false, null);

        channel.basicConsume(queueWithConsumers, new DefaultConsumer(channel));
        channel.basicPublish("<<default>>", queueWithMessages,
                             new AMQP.BasicProperties(), "Test Message".getBytes());
    }

    @Test(description = "Test queue delete with ifUnused parameter set", expectedExceptions = IOException.class)
    public void testDeleteQueueWithConsumers() throws Exception {
        Channel channel = amqpConnection.createChannel();
        channel.queueDelete(queueWithConsumers, true, false);
    }

    @Test(description = "Test queue delete with isEmpty parameter set", expectedExceptions = IOException.class)
    public void testDeleteQueueWithMessages() throws Exception {
        Channel channel = amqpConnection.createChannel();
        channel.queueDelete(queueWithMessages, false, true);
    }

    @AfterClass
    public void tearDown() throws Exception {
        Channel channel = amqpConnection.createChannel();
        channel.queueDelete(queueWithConsumers);
        channel.queueDelete(queueWithMessages);

        amqpConnection.close();
    }
}
