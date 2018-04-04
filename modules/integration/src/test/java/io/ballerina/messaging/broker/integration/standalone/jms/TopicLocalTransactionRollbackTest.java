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
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Test cases written to verify topic subscriber and publisher behavior in local transaction rollback operation
 */
public class TopicLocalTransactionRollbackTest {

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testSubscriberRollbackTransaction(String port,
                                                  String adminUsername,
                                                  String adminPassword,
                                                  String brokerHostname) throws NamingException, JMSException {
        String topicName = "testSubscriberRollbackTransaction";
        int numberOfMessages = 100;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(topicName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        // initialize subscriber
        TopicSession subscriberSession = connection.createTopicSession(true, Session.SESSION_TRANSACTED);
        Topic subscriberDestination = (Topic) initialContext.lookup(topicName);
        TopicSubscriber subscriber = subscriberSession.createSubscriber(subscriberDestination);

        // publish 100 messages
        TopicSession producerSession = connection.createTopicSession(true, Session.SESSION_TRANSACTED);
        TopicPublisher producer = producerSession.createPublisher(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.publish(producerSession.createTextMessage("Test message " + i));
        }
        // commit all publish messages
        producerSession.commit();
        producerSession.close();

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = subscriber.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        // rollback all subscribe messages
        subscriberSession.rollback();

        int numberOfMessagesAfterRollback = 0;
        for (int i = 0; i < numberOfMessages; i++) {
            Message message = subscriber.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received after rollback");
            numberOfMessagesAfterRollback++;
        }
        Assert.assertEquals(numberOfMessages, numberOfMessagesAfterRollback, "Only "
                + numberOfMessagesAfterRollback + " messages received after rollback but expect "
                + numberOfMessages + " messages");

        subscriberSession.commit();
        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testPublisherRollbackTransaction(String port,
                                                 String adminUsername,
                                                 String adminPassword,
                                                 String brokerHostname) throws NamingException, JMSException {
        String topicName = "testPublisherRollbackTransaction";
        int numberOfMessages = 100;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(topicName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        // initialize subscriber
        TopicSession subscriberSession = connection.createTopicSession(true, Session.SESSION_TRANSACTED);
        Topic subscriberDestination = (Topic) initialContext.lookup(topicName);
        TopicSubscriber subscriber = subscriberSession.createSubscriber(subscriberDestination);

        // publish 100 messages
        TopicSession producerSession = connection.createTopicSession(true, Session.SESSION_TRANSACTED);
        TopicPublisher producer = producerSession.createPublisher(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.publish(producerSession.createTextMessage("Test message " + i));
        }
        // rollback all publish messages
        producerSession.rollback();

        // Consume published messages
        Message message = subscriber.receive(1000);
        Assert.assertNull(message, "Messages should not receive upon publisher rollback");

        producerSession.close();
        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testTwoSubscribersRollbackOnePublisherCommitTransaction(String port,
                                                                        String adminUsername,
                                                                        String adminPassword,
                                                                        String brokerHostname)
            throws NamingException, JMSException {
        String topicName = "testTwoSubscribersRollbackOnePublisherCommitTransaction";
        int numberOfMessages = 100;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(topicName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        // initialize subscriber1 and subscriber2
        Topic subscriberDestination = (Topic) initialContext.lookup(topicName);
        TopicSession subscriberSession1 = connection.createTopicSession(true, Session.SESSION_TRANSACTED);
        TopicSubscriber subscriber1 = subscriberSession1.createSubscriber(subscriberDestination);

        TopicSession subscriberSession2 = connection.createTopicSession(true, Session.SESSION_TRANSACTED);
        TopicSubscriber subscriber2 = subscriberSession2.createSubscriber(subscriberDestination);

        // publish 100 messages
        TopicSession producerSession = connection.createTopicSession(true, Session.SESSION_TRANSACTED);
        TopicPublisher producer = producerSession.createPublisher(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.publish(producerSession.createTextMessage("Test message " + i));
        }
        // commit all publish messages
        producerSession.commit();
        producerSession.close();

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = subscriber1.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        // subscriber1 commit all subscribe messages
        subscriberSession1.rollback();

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = subscriber2.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        // subscriber2 rollback all subscribe messages
        subscriberSession2.rollback();

        // subscriber1 consumer all messages again after rollback
        int numberOfMessagesAfterRollbackSubscriber1 = 0;
        for (int i = 0; i < numberOfMessages; i++) {
            Message message = subscriber1.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received after rollback");
            numberOfMessagesAfterRollbackSubscriber1++;
        }
        Assert.assertEquals(numberOfMessages, numberOfMessagesAfterRollbackSubscriber1, "Only "
                + numberOfMessagesAfterRollbackSubscriber1 + " messages received after rollback but expect "
                + numberOfMessages + " messages");

        subscriberSession1.commit();

        // subscriber2 consumer all messages again after rollback
        int numberOfMessagesAfterRollbackSubscriber2 = 0;
        for (int i = 0; i < numberOfMessages; i++) {
            Message message = subscriber2.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received after rollback");
            numberOfMessagesAfterRollbackSubscriber2++;
        }
        Assert.assertEquals(numberOfMessages, numberOfMessagesAfterRollbackSubscriber2, "Only "
                + numberOfMessagesAfterRollbackSubscriber2 + " messages received after rollback but expect "
                + numberOfMessages + " messages");

        subscriberSession2.commit();

        // subscriber1 check remain messages after commit
        Message subscriber1RemainingMessage = subscriber1.receive(1000);
        Assert.assertNull(subscriber1RemainingMessage, "Messages should not receive after commit");

        // subscriber2 check remain messages after commit
        Message subscriber2RemainingMessage = subscriber2.receive(1000);
        Assert.assertNull(subscriber2RemainingMessage, "Messages should not receive after commit");

        subscriberSession1.close();
        subscriberSession2.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test(expectedExceptions = javax.jms.IllegalStateException.class,
            expectedExceptionsMessageRegExp = ".*Session is not transacted")
    public void testRollbackOnNonTransactionTopicSession(String port,
                                                         String adminUsername,
                                                         String adminPassword,
                                                         String brokerHostname) throws NamingException, JMSException {
        String topicName = "testRollbackOnNonTransactionTopicSession";
        int numberOfMessages = 100;

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

        // publish 100 messages
        TopicSession producerSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicPublisher producer = producerSession.createPublisher(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.publish(producerSession.createTextMessage("Test message " + i));
        }
        // commit all publish messages
        producerSession.rollback();
        producerSession.close();

        Message message = subscriber.receive(1000);
        Assert.assertNull(message, "Messages should not receive message after calling rollback on "
                + "non transaction channel");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testPublisherCloseBeforeRollbackTransaction(String port,
                                                            String adminUsername,
                                                            String adminPassword,
                                                            String brokerHostname)
            throws NamingException, JMSException {
        String topicName = "testPublisherCloseBeforeRollbackTransaction";
        int numberOfMessages = 100;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(topicName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        // initialize subscriber
        TopicSession subscriberSession = connection.createTopicSession(true, Session.SESSION_TRANSACTED);
        Topic subscriberDestination = (Topic) initialContext.lookup(topicName);
        TopicSubscriber subscriber = subscriberSession.createSubscriber(subscriberDestination);

        // publish 100 messages
        TopicSession producerSession = connection.createTopicSession(true, Session.SESSION_TRANSACTED);
        TopicPublisher producer = producerSession.createPublisher(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.publish(producerSession.createTextMessage("Test message " + i));
        }

        // close publisher before rollback
        producer.close();

        // rollback all publish messages
        producerSession.rollback();

        Message message = subscriber.receive(1000);
        Assert.assertNull(message, "Messages should not receive upon publisher rollback");

        producerSession.close();
        subscriberSession.close();
        connection.close();
    }
}
