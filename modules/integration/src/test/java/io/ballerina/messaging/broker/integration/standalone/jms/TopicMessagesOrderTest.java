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
import java.util.List;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Test class to validate message ordering in the topic.
 */
public class TopicMessagesOrderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicMessagesOrderTest.class);

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void test1338TopicMessagesOrderSingleSubscriber(String port,
                                                           String adminUsername,
                                                           String adminPassword,
                                                           String brokerHostname) throws NamingException, JMSException {
        String topicName = "test1338TopicMessagesOrderSingleSubscriber";
        List<String> subscriberOneMessages = new ArrayList<>();
        int numberOfMessages = 1338;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(topicName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        // Initialize subscriber
        TopicSession subscriberSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic subscriberDestination = (Topic) initialContext.lookup(topicName);
        TopicSubscriber subscriber = subscriberSession.createSubscriber(subscriberDestination);

        // publish 1338 messages
        TopicSession producerSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicPublisher producer = producerSession.createPublisher(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.publish(producerSession.createTextMessage(String.valueOf(i)));
        }

        producerSession.close();

        for (int i = 0; i < numberOfMessages; i++) {
            TextMessage message = (TextMessage) subscriber.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
            subscriberOneMessages.add(message.getText());
        }

        subscriberSession.close();

        connection.close();

        // verify order is preserved
        boolean isOrderPreserved = true;
        for (int i = 0; i < numberOfMessages; i++) {
            if (!(i == Integer.parseInt(subscriberOneMessages.get(i)))) {
                isOrderPreserved = false;
                break;
            }
        }

        Assert.assertTrue(isOrderPreserved, "Topic messages order not preserved for single subscriber.");
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void test1644TopicMessagesOrderTwoSequentialSubscribers(String port,
                                                                   String adminUsername,
                                                                   String adminPassword,
                                                                   String brokerHostname)
            throws NamingException, JMSException {
        String topicName = "test1644TopicMessagesOrderTwoSequentialSubscribers";
        List<String> subscriberOneMessages = new ArrayList<>();
        List<String> subscriberTwoMessages = new ArrayList<>();
        int numberOfMessages = 1644;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(topicName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        // Initialize subscriber
        TopicSession subscriberSessionOne = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicSession subscriberSessionTwo = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic subscriberDestination = (Topic) initialContext.lookup(topicName);
        TopicSubscriber subscriberOne = subscriberSessionOne.createSubscriber(subscriberDestination);
        TopicSubscriber subscriberTwo = subscriberSessionTwo.createSubscriber(subscriberDestination);

        // publish 1644 messages
        TopicSession producerSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicPublisher producer = producerSession.createPublisher(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.publish(producerSession.createTextMessage(String.valueOf(i)));
        }

        producerSession.close();

        for (int i = 0; i < numberOfMessages; i++) {
            TextMessage message = (TextMessage) subscriberOne.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
            subscriberOneMessages.add(message.getText());
        }

        for (int i = 0; i < numberOfMessages; i++) {
            TextMessage message = (TextMessage) subscriberTwo.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
            subscriberTwoMessages.add(message.getText());
        }

        subscriberSessionOne.close();
        subscriberSessionTwo.close();

        connection.close();

        // verify order is preserved
        boolean isSubscriberOneOrderPreserved = true;
        boolean isSubscriberTwoOrderPreserved = true;

        for (int i = 0; i < numberOfMessages; i++) {
            if (!(i == Integer.parseInt(subscriberOneMessages.get(i)))) {
                isSubscriberOneOrderPreserved = false;
                break;
            }
        }

        for (int i = 0; i < numberOfMessages; i++) {
            if (!(i == Integer.parseInt(subscriberTwoMessages.get(i)))) {
                isSubscriberTwoOrderPreserved = false;
                break;
            }
        }

        Assert.assertTrue(isSubscriberOneOrderPreserved,
                "Topic messages order not preserved for sequential subscriber one.");
        Assert.assertTrue(isSubscriberTwoOrderPreserved,
                "Topic messages order not preserved for sequential subscriber two.");
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void test1161TopicMessagesOrderTwoParallelSubscribers(String port,
                                                                 String adminUsername,
                                                                 String adminPassword,
                                                                 String brokerHostname)
            throws NamingException, JMSException, InterruptedException {
        String topicName = "test1161TopicMessagesOrderTwoParallelSubscribers";
        List<String> subscriberOneMessages = new ArrayList<>();
        List<String> subscriberTwoMessages = new ArrayList<>();
        int numberOfMessages = 1161;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(topicName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        // Initialize subscriber
        TopicSession subscriberSessionOne = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicSession subscriberSessionTwo = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic subscriberDestination = (Topic) initialContext.lookup(topicName);
        TopicSubscriber subscriberOne = subscriberSessionOne.createSubscriber(subscriberDestination);
        TopicSubscriber subscriberTwo = subscriberSessionTwo.createSubscriber(subscriberDestination);

        Thread subscriberOneThread = new Thread(() -> {
            try {
                for (int i = 0; i < numberOfMessages; i++) {
                    TextMessage message = (TextMessage) subscriberOne.receive(1000);
                    Assert.assertNotNull(message, "Message #" + i + " was not received");
                    subscriberOneMessages.add(message.getText());
                }
                subscriberSessionOne.close();
            } catch (JMSException e) {
                LOGGER.error("Error occurred while receiving messages consumer one thread.", e);
            }
        });
        subscriberOneThread.start();

        Thread subscriberTwoThread = new Thread(() -> {
            try {
                for (int i = 0; i < numberOfMessages; i++) {
                    TextMessage message = (TextMessage) subscriberTwo.receive(1000);
                    Assert.assertNotNull(message, "Message #" + i + " was not received");
                    subscriberTwoMessages.add(message.getText());
                }
                subscriberSessionTwo.close();
            } catch (JMSException e) {
                LOGGER.error("Error occurred while receiving messages consumer one thread.", e);
            }
        });
        subscriberTwoThread.start();

        // publish 1161 messages
        TopicSession producerSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicPublisher producer = producerSession.createPublisher(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.publish(producerSession.createTextMessage(String.valueOf(i)));
        }

        producerSession.close();

        subscriberOneThread.join();
        subscriberTwoThread.join();

        connection.close();

        // verify order is preserved
        boolean isSubscriberOneOrderPreserved = true;
        boolean isSubscriberTwoOrderPreserved = true;

        for (int i = 0; i < numberOfMessages; i++) {
            if (!(i == Integer.parseInt(subscriberOneMessages.get(i)))) {
                isSubscriberOneOrderPreserved = false;
                break;
            }
        }

        for (int i = 0; i < numberOfMessages; i++) {
            if (!(i == Integer.parseInt(subscriberTwoMessages.get(i)))) {
                isSubscriberTwoOrderPreserved = false;
                break;
            }
        }

        Assert.assertTrue(isSubscriberOneOrderPreserved,
                "Topic messages order not preserved for parallel subscriber one.");
        Assert.assertTrue(isSubscriberTwoOrderPreserved,
                "Topic messages order not preserved for parallel subscriber two.");
    }
}
