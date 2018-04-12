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
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JmsPropertyTypeTest {

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testBooleanProperty(String port,
                                    String adminUsername,
                                    String adminPassword,
                                    String brokerHostname) throws NamingException, JMSException {
        String queueName = "testBooleanProperty";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);
        String booleanPropertyName1 = "BooleanProperty1";
        String booleanPropertyName2 = "BooleanProperty2";
        TextMessage textMessage = producerSession.createTextMessage("Test message");
        textMessage.setBooleanProperty(booleanPropertyName1, true);
        textMessage.setBooleanProperty(booleanPropertyName2, false);
        producer.send(textMessage);
        producerSession.close();

        // receive messages
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        TextMessage message = (TextMessage) consumer.receive(1000);
        Assert.assertNotNull(message, "Message was not received");
        boolean receivedBooleanProperty1 = message.getBooleanProperty(booleanPropertyName1);
        boolean receivedBooleanProperty2 = message.getBooleanProperty(booleanPropertyName2);
        Assert.assertEquals(true, receivedBooleanProperty1, "Boolean property not matched.");
        Assert.assertEquals(false, receivedBooleanProperty2, "Boolean property not matched.");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testByteProperty(String port,
                                 String adminUsername,
                                 String adminPassword,
                                 String brokerHostname) throws NamingException, JMSException {
        String queueName = "testByteProperty";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);
        String bytePropertyName = "ByteProperty";
        byte byteProperty = 10;
        TextMessage textMessage = producerSession.createTextMessage("Test message");
        textMessage.setByteProperty(bytePropertyName, byteProperty);
        producer.send(textMessage);
        producerSession.close();

        // receive messages
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        TextMessage message = (TextMessage) consumer.receive(1000);
        Assert.assertNotNull(message, "Message was not received");
        byte receivedByteProperty = message.getByteProperty(bytePropertyName);
        Assert.assertEquals(byteProperty, receivedByteProperty, "Byte property not matched.");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testShortProperty(String port,
                                 String adminUsername,
                                 String adminPassword,
                                 String brokerHostname) throws NamingException, JMSException {
        String queueName = "testShortProperty";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);
        String shortPropertyName = "ShortProperty";
        short shortProperty = 10;
        TextMessage textMessage = producerSession.createTextMessage("Test message");
        textMessage.setShortProperty(shortPropertyName, shortProperty);
        producer.send(textMessage);
        producerSession.close();

        // receive messages
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        TextMessage message = (TextMessage) consumer.receive(1000);
        Assert.assertNotNull(message, "Message was not received");
        short receivedShortProperty = message.getShortProperty(shortPropertyName);
        Assert.assertEquals(shortProperty, receivedShortProperty, "Short property not matched.");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testIntProperty(String port,
                                  String adminUsername,
                                  String adminPassword,
                                  String brokerHostname) throws NamingException, JMSException {
        String queueName = "testIntProperty";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);
        String intPropertyName = "IntProperty";
        int intProperty = 10;
        TextMessage textMessage = producerSession.createTextMessage("Test message");
        textMessage.setIntProperty(intPropertyName, intProperty);
        producer.send(textMessage);
        producerSession.close();

        // receive messages
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        TextMessage message = (TextMessage) consumer.receive(1000);
        Assert.assertNotNull(message, "Message was not received");
        int receivedIntProperty = message.getIntProperty(intPropertyName);
        Assert.assertEquals(intProperty, receivedIntProperty, "Int property not matched.");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testLongProperty(String port,
                                String adminUsername,
                                String adminPassword,
                                String brokerHostname) throws NamingException, JMSException {
        String queueName = "testLongProperty";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);
        String longPropertyName = "LongProperty";
        long longProperty = 10L;
        TextMessage textMessage = producerSession.createTextMessage("Test message");
        textMessage.setLongProperty(longPropertyName, longProperty);
        producer.send(textMessage);
        producerSession.close();

        // receive messages
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        TextMessage message = (TextMessage) consumer.receive(1000);
        Assert.assertNotNull(message, "Message was not received");
        long receivedLongProperty = message.getLongProperty(longPropertyName);
        Assert.assertEquals(longProperty, receivedLongProperty, "Long property not matched.");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testFloatProperty(String port,
                                String adminUsername,
                                String adminPassword,
                                String brokerHostname) throws NamingException, JMSException {
        String queueName = "testFloatProperty";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);
        String floatPropertyName = "FloatProperty";
        float floatProperty = 10.0f;
        TextMessage textMessage = producerSession.createTextMessage("Test message");
        textMessage.setFloatProperty(floatPropertyName, floatProperty);
        producer.send(textMessage);
        producerSession.close();

        // receive messages
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        TextMessage message = (TextMessage) consumer.receive(1000);
        Assert.assertNotNull(message, "Message was not received");
        float receivedFloatProperty = message.getFloatProperty(floatPropertyName);
        Assert.assertEquals(floatProperty, receivedFloatProperty, "Float property not matched.");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testDoubleProperty(String port,
                                  String adminUsername,
                                  String adminPassword,
                                  String brokerHostname) throws NamingException, JMSException {
        String queueName = "testDoubleProperty";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);
        String doublePropertyName = "DoubleProperty";
        double doubleProperty = 10.0d;
        TextMessage textMessage = producerSession.createTextMessage("Test message");
        textMessage.setDoubleProperty(doublePropertyName, doubleProperty);
        producer.send(textMessage);
        producerSession.close();

        // receive messages
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        TextMessage message = (TextMessage) consumer.receive(1000);
        Assert.assertNotNull(message, "Message was not received");
        double receivedDoubleProperty = message.getDoubleProperty(doublePropertyName);
        Assert.assertEquals(doubleProperty, receivedDoubleProperty, "Double property not matched.");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testStringProperty(String port,
                                   String adminUsername,
                                   String adminPassword,
                                   String brokerHostname) throws NamingException, JMSException {
        String queueName = "testStringProperty";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);
        String stringPropertyName = "StringProperty";
        String stringProperty = "!@#$%^QWERTYqwerty123456";
        TextMessage textMessage = producerSession.createTextMessage("Test message");
        textMessage.setStringProperty(stringPropertyName, stringProperty);
        producer.send(textMessage);
        producerSession.close();

        // receive messages
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        TextMessage message = (TextMessage) consumer.receive(1000);
        Assert.assertNotNull(message, "Message was not received");
        String receivedStringProperty = message.getStringProperty(stringPropertyName);
        Assert.assertEquals(stringProperty, receivedStringProperty, "String property not matched.");

        subscriberSession.close();
        connection.close();
    }

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testObjectProperty(String port,
                                   String adminUsername,
                                   String adminPassword,
                                   String brokerHostname) throws NamingException, JMSException {
        String queueName = "testObjectProperty";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // send messages
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);
        String objectPropertyName = "ObjectProperty";
        String objectProperty = "123456qwertyQWERTY!@#$%^";
        TextMessage textMessage = producerSession.createTextMessage("Test message");
        textMessage.setObjectProperty(objectPropertyName, objectProperty);
        producer.send(textMessage);
        producerSession.close();

        // receive messages
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        TextMessage message = (TextMessage) consumer.receive(1000);
        Assert.assertNotNull(message, "Message was not received");
        String receivedObjectProperty = (String) message.getObjectProperty(objectPropertyName);
        Assert.assertEquals(objectProperty, receivedObjectProperty, "Object property not matched.");

        subscriberSession.close();
        connection.close();
    }
}
