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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * Test cases written to verify queue consumer and producer behavior in local transaction commit operation
 */
public class QueueLocalTransactionCommitTest {

    @BeforeClass
    public void setup() {
        System.setProperty("STRICT_AMQP", "true");
    }

    @AfterClass
    public void destroy() {
        System.clearProperty("STRICT_AMQP");
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testConsumerProducerCommitTransaction(String port,
                                                      String adminUsername,
                                                      String adminPassword,
                                                      String brokerHostname) throws NamingException, JMSException {
        String queueName = "testConsumerProducerCommitTransaction";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send 100 messages
        Session producerSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        // commit all sent messages
        producerSession.commit();
        producerSession.close();

        // consume messages
        Session subscriberSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        // commit all received messages
        subscriberSession.commit();
        Message message = consumer.receive(1000);
        Assert.assertNull(message, "Messages should not receive after commit");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testTwoConsumersOneProducerCommitTransaction(String port,
                                                             String adminUsername,
                                                             String adminPassword,
                                                             String brokerHostname)
            throws NamingException, JMSException {
        String queueName = "testTwoConsumersOneProducerCommitTransaction";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        int numberOfMessages = 100;

        // create consumer1 and consumer2
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession1 = connection.createSession(true, Session.SESSION_TRANSACTED);
        MessageConsumer consumer1 = subscriberSession1.createConsumer(subscriberDestination);

        Session subscriberSession2 = connection.createSession(true, Session.SESSION_TRANSACTED);
        MessageConsumer consumer2 = subscriberSession2.createConsumer(subscriberDestination);

        // send 100 messages
        Session producerSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        // commit all sent messages
        producerSession.commit();
        producerSession.close();

        for (int i = 0; i < (numberOfMessages / 2); i++) {
            Message message = consumer1.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        // consumer1 commit all received messages
        subscriberSession1.commit();

        for (int i = 0; i < (numberOfMessages / 2); i++) {
            Message message = consumer2.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        // consumer2 commit all received messages
        subscriberSession2.commit();

        // consumer1 check remain messages after commit
        Message consumer1RemainingMessage = consumer1.receive(1000);
        Assert.assertNull(consumer1RemainingMessage, "Messages should not receive by consumer1 after commit");

        // consumer2 check remain messages after commit
        Message consumer2RemainingMessage = consumer2.receive(1000);
        Assert.assertNull(consumer2RemainingMessage, "Messages should not receive by consumer2 after commit");

        subscriberSession1.close();
        subscriberSession2.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testOneConsumerCommitOneConsumerRollbackOneProducerCommitTransaction(String port,
                                                                                     String adminUsername,
                                                                                     String adminPassword,
                                                                                     String brokerHostname)
            throws NamingException, JMSException {
        String queueName = "testOneConsumerCommitOneConsumerRollbackOneProducerCommitTransaction";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        int numberOfMessages = 100;

        // create consumer1 and consumer2
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession1 = connection.createSession(true, Session.SESSION_TRANSACTED);
        MessageConsumer consumer1 = subscriberSession1.createConsumer(subscriberDestination);

        Session subscriberSession2 = connection.createSession(true, Session.SESSION_TRANSACTED);
        MessageConsumer consumer2 = subscriberSession2.createConsumer(subscriberDestination);

        // send 100 messages
        Session producerSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        // commit all sent messages
        producerSession.commit();
        producerSession.close();

        for (int i = 0; i < (numberOfMessages / 2); i++) {
            Message message = consumer1.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        // consumer1 commit all received messages and close
        subscriberSession1.commit();
        consumer1.close();

        for (int i = 0; i < (numberOfMessages / 2); i++) {
            Message message = consumer2.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        // consumer2 rollback all received messages
        subscriberSession2.rollback();

        // consumer2 receive all messages again after rollback
        int numberOfMessagesAfterRollback = 0;
        for (int i = 0; i < (numberOfMessages / 2); i++) {
            Message message = consumer2.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received after rollback");
            numberOfMessagesAfterRollback++;
        }
        Assert.assertEquals((numberOfMessages / 2), numberOfMessagesAfterRollback, "Only "
                + numberOfMessagesAfterRollback + " messages received after rollback but expect "
                + (numberOfMessages / 2) + " messages");

        subscriberSession2.commit();

        // consumer2 check remain messages after commit
        Message consumer2RemainingMessage = consumer2.receive(1000);
        Assert.assertNull(consumer2RemainingMessage, "Messages should not receive by consumer2 after commit");

        subscriberSession1.close();
        subscriberSession2.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testProducerNotCommitTransaction(String port,
                                                 String adminUsername,
                                                 String adminPassword,
                                                 String brokerHostname) throws NamingException, JMSException {
        String queueName = "testProducerNotCommitTransaction";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send 100 messages
        Session producerSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }

        // consume messages
        Session subscriberSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        Message messageBeforeCommit = consumer.receive(1000);
        Assert.assertNull(messageBeforeCommit, "Messages should not receive because publisher not committed");

        // commit all sent messages
        producerSession.commit();
        producerSession.close();

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }

        // commit all received messages
        subscriberSession.commit();

        // check messages receive after commit
        Message message = consumer.receive(1000);
        Assert.assertNull(message, "Messages should not receive after commit");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test(expectedExceptions = javax.jms.IllegalStateException.class,
            expectedExceptionsMessageRegExp = ".*Session is not transacted")
    public void testCommitOnNonTransactionSession(String port,
                                                  String adminUsername,
                                                  String adminPassword,
                                                  String brokerHostname) throws NamingException, JMSException {
        String queueName = "testCommitOnNonTransactionSession";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send 100 messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        // commit all sent messages on non transactional session
        producerSession.commit();
        producerSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testConsumerCloseBeforeCommitTransaction(String port,
                                                         String adminUsername,
                                                         String adminPassword,
                                                         String brokerHostname) throws NamingException, JMSException {
        String queueName = "testConsumerCloseBeforeCommitTransaction";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send 100 messages
        Session producerSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        // commit all sent messages
        producerSession.commit();
        producerSession.close();

        // consume messages
        Session subscriberSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer1 = subscriberSession.createConsumer(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer1.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        // close consumer before commit
        consumer1.close();

        // commit all received messages
        subscriberSession.commit();

        // create another consumer and check there are no messages after commit
        MessageConsumer consumer2 = subscriberSession.createConsumer(subscriberDestination);
        Message message = consumer2.receive(1000);
        Assert.assertNull(message, "Messages should not receive after commit");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testProducerCloseBeforeCommitTransaction(String port,
                                                         String adminUsername,
                                                         String adminPassword,
                                                         String brokerHostname) throws NamingException, JMSException {
        String queueName = "testPublisherCloseBeforeCommitTransaction";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send 100 messages
        Session producerSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        // close publisher before commit
        producer.close();

        // commit all sent messages
        producerSession.commit();
        producerSession.close();

        // consume messages
        Session subscriberSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        // commit all received messages
        subscriberSession.commit();

        Message message = consumer.receive(1000);
        Assert.assertNull(message, "Messages should not receive after commit");

        subscriberSession.close();
        connection.close();
    }

}
