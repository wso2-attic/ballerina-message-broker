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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test successful queue delete operations.
 */
public class PositiveQueueDeleteTest {

    private Connection connection;

    private Channel channel;

    @Parameters({"broker-hostname", "broker-port", "admin-username", "admin-password"})
    @BeforeMethod
    public void setUp(String hostname, String port, String username, String password) throws Exception {
        connection = ClientHelper.getAmqpConnection(username, password, hostname, port);
        channel = connection.createChannel();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        channel.close();
        connection.close();
    }

    @Test (dataProvider = "queueNames")
    public void testQueueDelete(String queueName) throws Exception {
        channel.queueDeclare(queueName, false, false, false, null);
        channel.basicPublish("<<default>>", queueName, new AMQP.BasicProperties(), "testMessage".getBytes());

        AMQP.Queue.DeleteOk deleteOk = channel.queueDelete(queueName);
        Assert.assertEquals(deleteOk.getMessageCount(), 1);
    }

    @Test (dataProvider = "queueNames", description = "Test multiple delete calls to the same queue. It should not"
            + "throw an exception")
    public void testDeleteNonExistingQueue(String queueName) throws Exception {
        AMQP.Queue.DeleteOk deleteOk = channel.queueDelete(queueName);
        Assert.assertEquals(deleteOk.getMessageCount(), 0);

        deleteOk = channel.queueDelete(queueName);
        Assert.assertEquals(deleteOk.getMessageCount(), 0);
    }

    @Test (dataProvider = "queueNames")
    public void testDeleteUsedQueue(String queueName) throws Exception {
        channel.queueDeclare(queueName, false, false, false, null);
        channel.basicConsume(queueName, new DefaultConsumer(channel));

        AMQP.Queue.DeleteOk deleteOk = channel.queueDelete(queueName, false, true);
        Assert.assertEquals(deleteOk.getMessageCount(), 0);
    }

    @Test (dataProvider = "queueNames")
    public void testDeleteNonEmptyQueue(String queueName) throws Exception {
        channel.queueDeclare(queueName, false, false, false, null);
        channel.basicPublish("<<default>>", queueName, new AMQP.BasicProperties(), "testMessage".getBytes());

        AMQP.Queue.DeleteOk deleteOk = channel.queueDelete(queueName, true, false);
        Assert.assertEquals(deleteOk.getMessageCount(), 1);
    }

    @DataProvider(name = "queueNames")
    public static Object[] queueNames() {
        return new Object[] {
                "Queue1",
                "MyQueue",
        };
    }
}
