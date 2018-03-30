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
 * This class written to verify the behavior when client create and close connection per message.
 */
public class ConnectionCreateClosePerMessageTest {

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testCreateAndCloseConnectionPerQueueMessage(String port,
                                                            String adminUsername,
                                                            String adminPassword,
                                                            String brokerHostname)
            throws NamingException, JMSException {
        String queueName = "testCreateAndCloseConnectionPerQueueMessage";
        int numberOfMessages = 100;

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        // publish 100 messages
        for (int i = 0; i < numberOfMessages; i++) {
            Connection connection = getConnection(initialContextForQueue);
            Session producerSession = getSession(connection);
            Queue queue = getQueue(queueName, producerSession);
            MessageProducer producer = getMessageProducer(producerSession, queue);
            producer.send(producerSession.createTextMessage("Test message " + i));
            connection.close();
        }

        // Consume published messages
        int consumeMessageCount = 0;
        for (int i = 0; i < numberOfMessages; i++) {
            Connection connection = getConnection(initialContextForQueue);
            Session subscriberSession = getSession(connection);
            MessageConsumer consumer = getMessageConsumer(queueName, initialContextForQueue, subscriberSession);
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
            consumeMessageCount = consumeMessageCount + 1;
            connection.close();
        }

        Assert.assertEquals(numberOfMessages, consumeMessageCount, "Send message count " +  numberOfMessages +
                " and receive messages count " + consumeMessageCount);

    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testCreateAndCloseConnectionPerDurableTopicMessage(String port,
                                                                  String adminUsername,
                                                                  String adminPassword,
                                                                  String brokerHostname)
            throws NamingException, JMSException {
        String topicName = "testCreateAndCloseConnectionPerDurableTopicMessage";
        int numberOfMessages = 100;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(topicName)
                .build();

        // initialize subscriber to create durable topic and close immediately
        TopicConnection tmpConnection = getTopicConnection(initialContext);
        TopicSession tmpSession = getTopicSession(tmpConnection);
        Topic subscriberDestination = getTopic(topicName, initialContext);
        TopicSubscriber tmpSubscriber = getTopicSubscriber(tmpSession, subscriberDestination);
        TextMessage tmpMessage = (TextMessage) tmpSubscriber.receive(1000);
        Assert.assertNull(tmpMessage, "Message was received");
        tmpSession.close();
        tmpConnection.close();

        // publish 100 messages
        for (int i = 0; i < numberOfMessages; i++) {
            TopicConnection connection = getTopicConnection(initialContext);
            TopicSession producerSession = getTopicSession(connection);
            TopicPublisher producer = getTopicPublisher(subscriberDestination, producerSession);
            producer.publish(producerSession.createTextMessage(String.valueOf(i)));
            producerSession.close();
            connection.close();
        }

        // receive 100 messages
        int consumeMessageCount = 0;
        for (int i = 0; i < numberOfMessages; i++) {
            TopicConnection connection = getTopicConnection(initialContext);
            TopicSession subscriberSession = getTopicSession(connection);
            TopicSubscriber subscriber = getTopicSubscriber(subscriberSession, subscriberDestination);
            Message message = subscriber.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
            consumeMessageCount = consumeMessageCount + 1;
            subscriberSession.close();
            connection.close();
        }

        Assert.assertEquals(numberOfMessages, consumeMessageCount, "Send message count " +  numberOfMessages +
                " and receive messages count " + consumeMessageCount);

    }

    private TopicPublisher getTopicPublisher(Topic subscriberDestination, TopicSession producerSession)
            throws JMSException {
        return producerSession.createPublisher(subscriberDestination);
    }

    private TopicSubscriber getTopicSubscriber(TopicSession subscriberSession, Topic subscriberDestination)
            throws JMSException {
        return subscriberSession.createDurableSubscriber(subscriberDestination, "100_1");
    }

    private Topic getTopic(String topicName, InitialContext initialContext) throws NamingException {
        return (Topic) initialContext.lookup(topicName);
    }

    private TopicSession getTopicSession(TopicConnection connection) throws JMSException {
        return connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    private TopicConnection getTopicConnection(InitialContext initialContext) throws NamingException, JMSException {
        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();
        return connection;
    }

    private MessageConsumer getMessageConsumer(String queueName, InitialContext initialContextForQueue,
                                               Session subscriberSession) throws NamingException, JMSException {
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        return subscriberSession.createConsumer(subscriberDestination);
    }

    private Queue getQueue(String queueName, Session producerSession) throws JMSException {
        return producerSession.createQueue(queueName);
    }

    private Session getSession(Connection connection) throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    private MessageProducer getMessageProducer(Session producerSession, Queue queue) throws JMSException {
        return producerSession.createProducer(queue);
    }

    private Connection getConnection(InitialContext initialContextForQueue) throws NamingException, JMSException {
        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }
}
