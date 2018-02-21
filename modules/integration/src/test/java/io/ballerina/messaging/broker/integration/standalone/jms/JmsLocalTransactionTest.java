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
 * Class for testing local transactions commit and rollback operations in JMS
 */
public class JmsLocalTransactionTest {

    @Parameters({"broker-port"})
    @Test
    public void testConsumerProducerCommitTransaction(String port) throws NamingException, JMSException {
        String queueName = "testCommitConsumerAndPublisherTransaction";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin", "localhost", port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // publish 100 messages
        Session producerSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        //commit all sent messages
        producerSession.commit();
        producerSession.close();

        // Consume published messages
        Session subscriberSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        //commit all received messages
        subscriberSession.commit();
        Message message = consumer.receive(5000);
        Assert.assertNull(message, "Messages should not receive after commit all");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port"})
    @Test
    public void testConsumerRollbackTransaction(String port) throws NamingException, JMSException {
        String queueName = "testConsumerRollbackTransaction";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin", "localhost", port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // publish 100 messages
        Session producerSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        //commit all sent messages
        producerSession.commit();
        producerSession.close();

        // Consume published messages
        Session subscriberSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        //rollback all received messages
        subscriberSession.rollback();

        int numberOfMessagesAfterRollback = 0;
        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received after rollback");
            numberOfMessagesAfterRollback++;
        }
        Assert.assertEquals(numberOfMessages, numberOfMessagesAfterRollback, "Only "
                + numberOfMessagesAfterRollback + " messages received" + " after rollback but expect "
                + numberOfMessages + " messages");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port"})
    @Test
    public void testProducerRollbackTransaction(String port) throws NamingException, JMSException {
        String queueName = "testProducerRollbackTransaction";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin", "localhost", port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // publish 100 messages
        Session producerSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        //commit all sent messages
        producerSession.rollback();
        producerSession.close();

        // Consume published messages
        Session subscriberSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        Message message = consumer.receive(5000);
        Assert.assertNull(message, "Messages should not receive upon publisher rollback");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port"})
    @Test
    public void testProducerNotCommitTransaction(String port) throws NamingException, JMSException {
        String queueName = "testProducerNotCommitTransaction";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin", "localhost", port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // publish 100 messages
        Session producerSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }

        // Consume published messages
        Session subscriberSession = connection.createSession(true, Session.SESSION_TRANSACTED);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        Message messageBeforeCommit = consumer.receive(5000);
        Assert.assertNull(messageBeforeCommit, "Messages should not receive because publisher not committed");

        //commit all sent messages
        producerSession.commit();
        producerSession.close();

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }

        //commit all received messages
        subscriberSession.commit();
        Message message = consumer.receive(5000);
        Assert.assertNull(message, "Messages should not receive after commit all");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port"})
    @Test(expectedExceptions = javax.jms.IllegalStateException.class,
            expectedExceptionsMessageRegExp = ".*Session is not transacted")
    public void testCommitOnNonTransactionSession(String port) throws NamingException, JMSException {
        String queueName = "testCommitOnNonTransactionalChannel";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin", "localhost", port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // publish 100 messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        //commit all sent messages
        producerSession.commit();
        producerSession.close();
        connection.close();
    }

    @Parameters({"broker-port"})
    @Test(expectedExceptions = javax.jms.IllegalStateException.class,
            expectedExceptionsMessageRegExp = ".*Session is not transacted")
    public void testRollbackOnNonTransactionSession(String port) throws NamingException, JMSException {
        String queueName = "testRollbackOnNonTransactionSession";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin", "localhost", port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // publish 100 messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        //commit all sent messages
        producerSession.rollback();
        producerSession.close();
        connection.close();
    }
}
