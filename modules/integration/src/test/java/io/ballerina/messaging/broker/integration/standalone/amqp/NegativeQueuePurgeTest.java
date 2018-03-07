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

package io.ballerina.messaging.broker.integration.standalone.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test unsuccessful queue purge operations. Client throws {@link IOException} on failure.
 */
public class NegativeQueuePurgeTest {

    private Connection amqpConnection;

    @Parameters({"broker-hostname", "broker-port", "admin-username", "admin-password"})
    @BeforeMethod
    public void setUp(String hostname, String port, String username, String password) throws Exception {
        amqpConnection = ClientHelper.getAmqpConnection(username, password, hostname, port);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        this.amqpConnection.close();
    }

    @Test(description = "Test queue purge with non existing queue", expectedExceptions = IOException.class)
    public void purgeNonExistingQueue() throws IOException {
        Channel channel = amqpConnection.createChannel();
        channel.queuePurge("NegativeQueuePurgeTestPurgeNonExistingQueue");
    }

    @Test(description = "Test queue purge on a queue with consumers", expectedExceptions = IOException.class)
    public void deleteUsedQueue() throws Exception {
        String queueName = "NegativeQueuePurgeTestDeleteUsedQueue";
        Channel channel = amqpConnection.createChannel();

        channel.queueDeclare(queueName, false, false, false, null);
        channel.basicConsume(queueName, new DefaultConsumer(channel));

        channel.queuePurge(queueName);
    }
}
