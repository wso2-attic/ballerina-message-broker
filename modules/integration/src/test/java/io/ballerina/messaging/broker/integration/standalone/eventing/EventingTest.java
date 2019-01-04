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

package io.ballerina.messaging.broker.integration.standalone.eventing;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EventingTest {

    private Connection connection;
    private Channel channel;
    private static final Logger LOGGER = LoggerFactory.getLogger(EventingTest.class);

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

    @Test(dataProvider = "example queues")
    public void testQueueCreatedEvent(String queueName, boolean durable, boolean autoDelete) throws Exception {

        channel.queueDeclare("queue.added", false, false, false, null);
        channel.queueBind("queue.added", "x-event", "queue.created");
        channel.queueDeclare(queueName, durable, false, autoDelete, null);

        AMQP.Queue.DeclareOk dok = channel.queueDeclare("queue.created", false, false, false, null);
        Assert.assertFalse(dok.getQueue().isEmpty());
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

                Map<String, Object> headers = properties.getHeaders();
                Assert.assertEquals(String.valueOf(headers.get("queueName")), queueName);
                Assert.assertEquals(String.valueOf(headers.get("durable")), String.valueOf(durable));
                Assert.assertEquals(String.valueOf(headers.get("autoDelete")), String.valueOf(autoDelete));
            }
        };
        Assert.assertNotNull(channel.basicConsume("queue.added", false, consumer));
        channel.queueDelete(queueName);
    }

    @Test(dataProvider = "example queues")
    public void testQueueDeletedEvent(String queueName, boolean durable, boolean autoDelete) throws IOException {

        channel.queueDeclare("queue.deleted", false, false, false, null);
        channel.queueBind("queue.deleted", "x-event", "queue.deleted");
        channel.queueDeclare(queueName, durable, false, autoDelete, null);
        channel.queueDelete(queueName);
        AMQP.Queue.DeclareOk dok = channel.queueDeclare("queue.deleted", false, false, false, null);
        Assert.assertFalse(dok.getQueue().isEmpty());
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

                Map<String, Object> headers = properties.getHeaders();
                Assert.assertEquals(String.valueOf(headers.get("queueName")), queueName);
                Assert.assertEquals(String.valueOf(headers.get("durable")), String.valueOf(durable));
                Assert.assertEquals(String.valueOf(headers.get("autoDelete")), String.valueOf(autoDelete));
            }
        };
        Assert.assertNotNull(channel.basicConsume("queue.deleted", false, consumer));
    }

    @Test(dataProvider = "example exchanges")
    public void testExchangeCreatedEvent(String exchangeName, boolean durable, String type) throws IOException {

        channel.queueDeclare("exchange.created", false, false, false, null);
        channel.queueBind("exchange.created", "x-event", "exchange.created");
        channel.exchangeDeclare(exchangeName, type, durable);
        AMQP.Queue.DeclareOk dok = channel.queueDeclare("exchange.created", false, false, false, null);
        Assert.assertFalse(dok.getQueue().isEmpty());
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

                Map<String, Object> headers = properties.getHeaders();
                Assert.assertEquals(String.valueOf(headers.get("exchangeName")), exchangeName);
                Assert.assertEquals(String.valueOf(headers.get("durable")), String.valueOf(durable));
                Assert.assertEquals(String.valueOf(headers.get("type")), type);
            }
        };
        Assert.assertNotNull(channel.basicConsume("exchange.created", false, consumer));
        channel.exchangeDelete(exchangeName);
    }

    @Test(dataProvider = "example exchanges")
    public void testExchangeDeletedEvent(String exchangeName, boolean durable, String type) throws IOException {

        channel.queueDeclare("exchange.deleted", false, false, false, null);
        channel.queueBind("exchange.deleted", "x-event", "exchange.deleted");
        channel.exchangeDeclare(exchangeName, type, durable);
        channel.exchangeDelete(exchangeName);
        AMQP.Queue.DeclareOk dok = channel.queueDeclare("exchange.deleted", false, false, false, null);
        Assert.assertFalse(dok.getQueue().isEmpty());
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

                Map<String, Object> headers = properties.getHeaders();
                Assert.assertEquals(String.valueOf(headers.get("exchangeName")), exchangeName);
                Assert.assertEquals(String.valueOf(headers.get("durable")), String.valueOf(durable));
                Assert.assertEquals(String.valueOf(headers.get("type")), type);
            }
        };
        Assert.assertNotNull(channel.basicConsume("exchange.deleted", false, consumer));
    }

    @Test(dataProvider = "queueNames")
    public void testBindingCreatedEvent(String queueName) throws IOException {

        channel.queueDeclare("binding.created", false, false, false, null);
        channel.queueBind("binding.created", "x-event", "binding.created");
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, "<<default>>", "testPattern");
        AMQP.Queue.DeclareOk dok = channel.queueDeclare("binding.created", false, false, false, null);
        Assert.assertFalse(dok.getQueue().isEmpty());
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

                Map<String, Object> headers = properties.getHeaders();
                Assert.assertEquals(String.valueOf(headers.get("bindingQueue")), queueName);
                Assert.assertEquals(String.valueOf(headers.get("bindingPattern")), "testPattern");
            }
        };
        Assert.assertNotNull(channel.basicConsume("binding.created", false, consumer));
    }

    @Test(dataProvider = "queueNames")
    public void testBindingDeletedEvent(String queueName) throws IOException {

        channel.queueDeclare("binding.deleted", false, false, false, null);
        channel.queueBind("binding.deleted", "x-event", "binding.deleted");
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, "<<default>>", "testPattern");
        channel.queueUnbind(queueName, "<<default>>", "testPattern");
        AMQP.Queue.DeclareOk dok = channel.queueDeclare("binding.deleted", false, false, false, null);
        Assert.assertFalse(dok.getQueue().isEmpty());
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

                Map<String, Object> headers = properties.getHeaders();
                Assert.assertEquals(String.valueOf(headers.get("bindingQueue")), queueName);
                Assert.assertEquals(String.valueOf(headers.get("bindingPattern")), "testPattern");
            }
        };
        Assert.assertNotNull(channel.basicConsume("binding.deleted", false, consumer));
    }

    @Test(dataProvider = "queueNames")
    public void testConsumerAddedEvent(String queueName) throws Exception {

        channel.queueDeclare("consumer.added", false, false, false, null);
        channel.queueBind("consumer.added", "x-event", "consumer.added");
        channel.queueDeclare(queueName, false, false, false, null);

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

                Map<String, Object> headers = properties.getHeaders();
                Assert.assertEquals(String.valueOf(headers.get("queueName")), queueName);
            }
        };
        DefaultConsumer testConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

            }
        };
        channel.basicConsume(queueName, false, testConsumer);
        AMQP.Queue.DeclareOk dok = channel.queueDeclare("consumer.added", false, false, false, null);
        Assert.assertFalse(dok.getQueue().isEmpty());
        Assert.assertNotNull(channel.basicConsume("consumer.added", false, consumer));
    }

    @Test(dataProvider = "queueNames")
    public void testConsumerDeletedEvent(String queueName) throws Exception {

        channel.queueDeclare("consumer.removed", false, false, false, null);
        channel.queueBind("consumer.removed", "x-event", "consumer.removed");
        channel.queueDeclare(queueName, false, false, false, null);

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

                Map<String, Object> headers = properties.getHeaders();
                Assert.assertEquals(String.valueOf(headers.get("queueName")), queueName);
            }
        };
        DefaultConsumer testConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

            }
        };
        String consumerTag = channel.basicConsume(queueName, false, testConsumer);
        channel.basicCancel(consumerTag);
        AMQP.Queue.DeclareOk dok = channel.queueDeclare("consumer.removed", false, false, false, null);
        Assert.assertFalse(dok.getQueue().isEmpty());
        Assert.assertNotNull(channel.basicConsume("consumer.removed", false, consumer));
    }

    @Test(dataProvider = "example ques with limits")
    public void testQueueLimitReachedEvent(String queueName, Object limit)
            throws Exception {

        channel.queueDeclare("queue.publishLimitReached." + queueName + "." + limit, false, false,
                false, null);
        channel.queueBind("queue.publishLimitReached." + queueName + "." + limit, "x-event",
                "queue.publishLimitReached." + queueName + "." + limit);
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-queue-limits", limit);
        channel.queueDeclare("TestQueue", false, false, false, arguments);
        channel.queueBind("TestQueue", "<<default>>", queueName);

        int numberOfMessages = 10;
        boolean type = limit instanceof Integer;
        LOGGER.info(String.valueOf(type));
        if (type) {
            numberOfMessages = (Integer) limit;
        }
        for (int i = 0; i < numberOfMessages; i++) {
            channel.basicPublish("<<default>>", queueName, new AMQP.BasicProperties(), "test".getBytes());
        }

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) {

                Map<String, Object> headers = properties.getHeaders();
                Assert.assertEquals(String.valueOf(headers.get("queueName")), queueName);
                Assert.assertEquals(String.valueOf(headers.get("durable")), String.valueOf(false));
                Assert.assertEquals(String.valueOf(headers.get("autoDelete")), String.valueOf(false));
                Assert.assertEquals(String.valueOf(headers.get("messageCount")), String.valueOf(limit));
            }
        };

        if (type) {
            AMQP.Queue.DeclareOk dok = channel.queueDeclare("queue.publishLimitReached." + queueName + "." + limit,
                    false, false, false, null);
            Assert.assertFalse(dok.getQueue().isEmpty());
            Assert.assertNotNull(channel.basicConsume("queue.publishLimitReached." + queueName + "." + limit, false,
                    consumer));
        } else {
            AMQP.Queue.DeclareOk dok = channel.queueDeclare("TestQueue", false, false, false, arguments);
            Assert.assertEquals(dok.getMessageCount(), 0);
        }
        channel.queueDelete(queueName);
    }

    @DataProvider(name = "queueNames")
    public static Object[] queueNames() {

        return new Object[]{
                "Queue1",
                "MyQueue",
        };
    }

    @DataProvider
    public static Object[][] queueMessageLimits() {

        return new Object[][]{
                {"q1", "3,2"},
                {"q2", "6,3"}
        };
    }

    @DataProvider(name = "example queues")
    public Object[][] queueExamples() {

        return new Object[][]{
                {"q1", true, true},
                {"q2", true, false},
                {"q3", false, true},
                {"q4", false, true}
        };
    }

    @DataProvider(name = "example ques with limits")
    public Object[][] queueLimits() {

        return new Object[][]{
                {"q1", 3},
                {"q2", 7},
                {"q3", "sa"},
                {"q4", "3"}
        };
    }

    @DataProvider(name = "example exchanges")
    public Object[][] exchangeExamples() {

        return new Object[][]{
                {"e1", true, "topic"},
                {"e2", true, "direct"},
                {"e3", false, "topic"},
                {"e4", false, "direct"}
        };
    }

}
