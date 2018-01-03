/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.messaging.integration;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.wso2.messaging.integration.util.ClientHelper;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;

public class QueueConsumerTest {

    @Parameters({ "broker-port"})
    @Test
    public void testConsumerProducerWithAutoAck(String port) throws Exception {
        String queueName = "testConsumerProducerWithAutoAck";
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
        producerSession.close();

        // Consume published messages
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }

        connection.close();
    }

    @Parameters({ "broker-port"})
    @Test
    public void testConsumerProducerWithClientAck(String port) throws Exception {
        String queueName = "testConsumerProducerWithClientAck";
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
        producerSession.close();

        // Consume published messages
        Session subscriberSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
            message.acknowledge();
        }

        Message message = consumer.receive(5000);
        Assert.assertNull(message, "Messages should not receive after acknowledging all");

        connection.close();
    }

    @Parameters({ "broker-port", "admin-username", "admin-password", "broker-hostname" })
    @Test
    public void testConsumerWithBasicReject(String port,
                                            String adminUsername,
                                            String adminPassword,
                                            String brokerHostname) throws Exception {
        System.setProperty("AndesAckWaitTimeOut", "5000");
        String queueName = "testConsumerWithBasicReject";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // publish message
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        producer.send(producerSession.createTextMessage("Test message for reject test"));
        producerSession.close();

        // Consume published messages
        Session subscriberSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        Message message = consumer.receive(5000);
        Assert.assertNotNull(message, "Message was not received");

        message = consumer.receive(10000);
        Assert.assertNotNull(message, "Requeued Message was not received");
        Assert.assertTrue(message.getJMSRedelivered(), "Redelivered flag was not set");
        message.acknowledge();

        connection.close();
    }

    @Parameters({ "broker-port", "admin-username", "admin-password", "broker-hostname" })
    @Test
    public void testConsumerWithBasicRecover(String port,
                                            String adminUsername,
                                            String adminPassword,
                                            String brokerHostname) throws Exception {
        String queueName = "testConsumerWithBasicRecover";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // publish message
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        producer.send(producerSession.createTextMessage("First message for reject test"));
        producer.send(producerSession.createTextMessage("Second message for reject test"));
        producerSession.close();

        // Consume published messages
        Session subscriberSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        Message message = consumer.receive(5000);
        Assert.assertNotNull(message, "Message was not received");

        subscriberSession.recover();

        message = consumer.receive(5000);
        Assert.assertNotNull(message, "Requeued first Message was not received");
        Assert.assertTrue(message.getJMSRedelivered(), "Redelivered flag was not set in first message");
        message.acknowledge();

        message = consumer.receive(5000);
        Assert.assertNotNull(message, "Requeued second Message was not received");
        Assert.assertTrue(message.getJMSRedelivered(), "Redelivered flag was not set in second");
        message.acknowledge();

        connection.close();
    }
}
