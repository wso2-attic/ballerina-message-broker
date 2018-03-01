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
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JMSSelectorTest {

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testPositiveJMSSelectorConsumerProducer(String port,
                                                        String adminUsername,
                                                        String adminPassword,
                                                        String brokerHostname) throws NamingException, JMSException {
        String queueName = "testPositiveJMSSelectorConsumerProducer";
        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(queueName)
                .build();


        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        TopicSession subscriberSession = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        Topic topic = (Topic) initialContext.lookup(queueName);

        // Subscribe with a selector
        String propertyName = "MyProperty";
        String propertyValue = "propertyValue";
        String jmsPropertySelector = propertyName + " = '" + propertyValue + "'";
        TopicSubscriber consumer = subscriberSession.createSubscriber(topic, jmsPropertySelector, false);

        // publish messages with property
        TopicSession producerSession = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        TopicPublisher producer = producerSession.createPublisher(topic);

        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            TextMessage textMessage = producerSession.createTextMessage("Test message " + i);
            textMessage.setStringProperty(propertyName, propertyValue);
            producer.send(textMessage);
        }

        // consume messages
        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }

        producerSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testNegativeJMSSelectorConsumerProducer(String port,
                                                        String adminUsername,
                                                        String adminPassword,
                                                        String brokerHostname) throws NamingException, JMSException {
        String queueName = "testNegativeJMSSelectorConsumerProducer";
        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(queueName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        TopicSession subscriberSession = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        Topic topic = (Topic) initialContext.lookup(queueName);

        // Subscribe with a selector
        String propertyName = "MyProperty";
        String propertyValue = "propertyValue";
        String jmsPropertySelector = propertyName + " = '" + propertyValue + "'";
        TopicSubscriber consumer = subscriberSession.createSubscriber(topic, jmsPropertySelector, false);

        // publish messages with property
        TopicSession producerSession = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        TopicPublisher producer = producerSession.createPublisher(topic);

        // Send messages with a different property value
        int numberOfMessages = 100;
        for (int i = 0; i < numberOfMessages; i++) {
            TextMessage textMessage = producerSession.createTextMessage("Test message " + i);
            textMessage.setStringProperty(propertyName, propertyValue + "-1");
            producer.send(textMessage);
        }

        // consume messages
        Message message = consumer.receive(100);
        Assert.assertNull(message, "Message received. Shouldn't receive any messages.");

        producerSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testMultipleSelectorConsumersForTopic(String port,
                                                      String adminUsername,
                                                      String adminPassword,
                                                      String brokerHostname) throws NamingException, JMSException {
        String queueName = "testMultipleSelectorConsumersForTopic";
        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(queueName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        TopicSession subscriberSession = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        Topic topic = (Topic) initialContext.lookup(queueName);

        // Subscribe with selectors.
        TopicSubscriber consumer1 = subscriberSession.createSubscriber(topic, "Age = 10", false);
        TopicSubscriber consumer2 = subscriberSession.createSubscriber(topic, "Age = 15", false);
        TopicSubscriber consumer3 = subscriberSession.createSubscriber(topic, "Name = 'John'", false);

        // publish messages with property.
        TopicSession producerSession = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        TopicPublisher producer = producerSession.createPublisher(topic);

        String consumer1Message = "Age 10 group";
        String consumer2Message = "Age 15 group";
        String consumer3Message = "Name John group";
        TextMessage textMessage = producerSession.createTextMessage(consumer1Message);
        textMessage.setIntProperty("Age", 10);
        producer.send(textMessage);

        textMessage = producerSession.createTextMessage(consumer2Message);
        textMessage.setIntProperty("Age", 15);
        producer.send(textMessage);

        textMessage = producerSession.createTextMessage(consumer3Message);
        textMessage.setStringProperty("Name", "John");
        producer.send(textMessage);


        // Receive and test messages.
        Message message = consumer1.receive(1000);
        Assert.assertNotNull(message, "Message not received.");
        textMessage = (TextMessage) message;
        Assert.assertEquals(textMessage.getText(), consumer1Message, "Incorrect message received.");

        message = consumer2.receive(1000);
        Assert.assertNotNull(message, "Message not received.");
        textMessage = (TextMessage) message;
        Assert.assertEquals(textMessage.getText(), consumer2Message, "Incorrect message received.");

        message = consumer3.receive(1000);
        Assert.assertNotNull(message, "Message not received.");
        textMessage = (TextMessage) message;
        Assert.assertEquals(textMessage.getText(), consumer3Message, "Incorrect message received.");

        producer.close();
        consumer1.close();
        consumer2.close();
        consumer3.close();

        connection.close();

    }
}
