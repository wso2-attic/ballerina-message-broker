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

package io.ballerina.messaging.broker.integration.standalone.jms;

import io.ballerina.messaging.broker.integration.util.ClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Test class to validate message ordering in the queue.
 */
public class QueueMessagesOrderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueMessagesOrderTest.class);

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void test1782QueueMessagesOrderSingleConsumer(String port,
                                                         String adminUsername,
                                                         String adminPassword,
                                                         String brokerHostname) throws NamingException, JMSException {
        String queueName = "test1782QueueMessagesOrderSingleConsumer";
        List<String> consumerOneMessages = new ArrayList<>();

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send 1782 messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 1782;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage(String.valueOf(i)));
        }

        producerSession.close();

        // receive 1782 messages
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        // add messages to list
        for (int i = 0; i < numberOfMessages; i++) {
            TextMessage message = (TextMessage) consumer.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
            consumerOneMessages.add(message.getText());
        }

        subscriberSession.close();

        connection.close();

        // verify order is preserved
        boolean isOrderPreserved = true;
        for (int i = 0; i < numberOfMessages; i++) {
            if (!(i == Integer.parseInt(consumerOneMessages.get(i)))) {
                isOrderPreserved = false;
                break;
            }
        }

        Assert.assertTrue(isOrderPreserved, "Queue messages order not preserved for single consumer.");
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void test1221QueueMessagesOrderTwoSequentialConsumers(String port,
                                                                 String adminUsername,
                                                                 String adminPassword,
                                                                 String brokerHostname)
            throws NamingException, JMSException {
        String queueName = "test1221QueueMessagesOrderTwoSequentialConsumers";
        List<String> consumerOneMessages = new ArrayList<>();
        List<String> consumerTwoMessages = new ArrayList<>();

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        int numberOfMessages = 1221;

        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSessionOne = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Session subscriberSessionTwo = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumerOne = subscriberSessionOne.createConsumer(subscriberDestination);
        MessageConsumer consumerTwo = subscriberSessionTwo.createConsumer(subscriberDestination);

        // send 1221 messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage(String.valueOf(i)));
        }

        producerSession.close();

        int consumerOneMessageCount = 0;
        // receive messages from the consumer1
        for (int i = 0; i < numberOfMessages; i++) {
            TextMessage message = (TextMessage) consumerOne.receive(1000);
            if (Objects.isNull(message)) {
                break;
            }
            consumerOneMessages.add(message.getText());
            consumerOneMessageCount = consumerOneMessageCount + 1;
        }

        int consumerTwoMessageCount = 0;
        // receive messages from the consumer2
        for (int i = 0; i < numberOfMessages; i++) {
            TextMessage message = (TextMessage) consumerTwo.receive(1000);
            if (Objects.isNull(message)) {
                break;
            }
            consumerTwoMessages.add(message.getText());
            consumerTwoMessageCount = consumerTwoMessageCount + 1;
        }

        Assert.assertEquals(consumerOneMessageCount + consumerTwoMessageCount, numberOfMessages,
                "Consumer One received " + consumerOneMessageCount +
                        " and Consumer two received " + consumerTwoMessageCount + "." +
                        " Send messages count not matched with receive messages count.");

        subscriberSessionOne.close();
        subscriberSessionTwo.close();

        connection.close();

        // verify order is preserved
        boolean isConsumerOneOrderPreserved;
        boolean isConsumerTwoOrderPreserved;

        isConsumerOneOrderPreserved = checkStrictlyIncreasing
                (Arrays.stream(consumerOneMessages.toArray(new String[0])).mapToInt(Integer::parseInt).toArray());

        isConsumerTwoOrderPreserved = checkStrictlyIncreasing
                (Arrays.stream(consumerTwoMessages.toArray(new String[0])).mapToInt(Integer::parseInt).toArray());

        Assert.assertTrue(isConsumerOneOrderPreserved,
                "Queue messages order not preserved for sequential consumer one.");
        Assert.assertTrue(isConsumerTwoOrderPreserved,
                "Queue messages order not preserved for sequential consumer two.");
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void test1497QueueMessagesOrderTwoParallelConsumers(String port,
                                                               String adminUsername,
                                                               String adminPassword,
                                                               String brokerHostname)
            throws NamingException, JMSException, InterruptedException {
        String queueName = "test1497QueueMessagesOrderTwoParallelConsumers";
        List<String> consumerOneMessages = new ArrayList<>();
        List<String> consumerTwoMessages = new ArrayList<>();

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        int numberOfMessages = 1497;

        Session subscriberSessionOne = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Session subscriberSessionTwo = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumerOne = subscriberSessionOne.createConsumer(subscriberDestination);
        MessageConsumer consumerTwo = subscriberSessionTwo.createConsumer(subscriberDestination);

        // receive messages from the consumer1
        AtomicInteger consumerOneMessageCount = new AtomicInteger();
        Thread consumerOneThread = new Thread(() -> {
            try {
                for (int i = 0; i < numberOfMessages; i++) {

                    TextMessage message = (TextMessage) consumerOne.receive(5000);
                    if (Objects.isNull(message)) {
                        break;
                    }
                    consumerOneMessages.add(message.getText());
                    consumerOneMessageCount.incrementAndGet();
                }
                subscriberSessionOne.close();
            } catch (JMSException e) {
                LOGGER.error("Error occurred while receiving messages consumer one thread.", e);
            }
        });
        consumerOneThread.start();

        // receive messages from the consumer2
        AtomicInteger consumerTwoMessageCount = new AtomicInteger();
        Thread consumerTwoThread = new Thread(() -> {
            try {
                for (int i = 0; i < numberOfMessages; i++) {

                    TextMessage message = (TextMessage) consumerTwo.receive(5000);
                    if (Objects.isNull(message)) {
                        break;
                    }
                    consumerTwoMessages.add(message.getText());
                    consumerTwoMessageCount.incrementAndGet();
                }
                subscriberSessionTwo.close();
            } catch (JMSException e) {
                LOGGER.error("Error occurred while receiving messages consumer two thread.", e);
            }
        });
        consumerTwoThread.start();

        // send 1497 messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage(String.valueOf(i)));
        }

        producerSession.close();

        consumerOneThread.join();
        consumerTwoThread.join();

        Assert.assertEquals(consumerOneMessageCount.get() + consumerTwoMessageCount.get(), numberOfMessages,
                "Consumer One received " + consumerOneMessageCount.get() +
                        " and Consumer two received " + consumerTwoMessageCount.get() + "." +
                        " Send messages count not matched with receive messages count.");

        connection.close();

        // verify order is preserved
        boolean isConsumerOneOrderPreserved;
        boolean isConsumerTwoOrderPreserved;

        isConsumerOneOrderPreserved = checkStrictlyIncreasing
                (Arrays.stream(consumerOneMessages.toArray(new String[0])).mapToInt(Integer::parseInt).toArray());

        isConsumerTwoOrderPreserved = checkStrictlyIncreasing
                (Arrays.stream(consumerTwoMessages.toArray(new String[0])).mapToInt(Integer::parseInt).toArray());

        Assert.assertTrue(isConsumerOneOrderPreserved,
                "Queue messages order not preserved for concurrent consumer one.");
        Assert.assertTrue(isConsumerTwoOrderPreserved,
                "Queue messages order not preserved for concurrent consumer two.");
    }

    private boolean checkStrictlyIncreasing(int[] messageSequence) {
        boolean isIncrease = true;
        for (int i = 0; i < messageSequence.length - 1; i++) {
            if (!(messageSequence[i] < messageSequence[i + 1])) {
                isIncrease = false;
                break;
            }
        }
        return isIncrease;
    }
}
