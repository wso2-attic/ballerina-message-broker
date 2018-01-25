/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.messaging.integration.failover;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.wso2.broker.common.StartupContext;
import org.wso2.broker.coordination.BrokerHaConfiguration;
import org.wso2.broker.coordination.rdbms.RdbmsCoordinationConstants;
import org.wso2.messaging.integration.util.ClientHelper;
import org.wso2.messaging.integration.util.Node;
import org.wso2.messaging.integration.util.TestConfigProvider;

import java.util.HashMap;
import java.util.Map;
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
 * Test class for the {@link org.wso2.broker.coordination.rdbms.RdbmsHaStrategy} implementation.
 */
public class RdbmsHaStrategyTest {

    private Node nodeOne;
    private Node nodeTwo;

    private Node activeNode;
    private Node passiveNode;

    private int heartbeatInterval = 5000;
    private int coordinatorEntryCreationWaitTime = 3000;

    private static final String SYNC_TEST_QUEUE_NAME = "testSyncingOnFailover";
    private static final int SYNC_TEST_NUMBER_OF_MESSAGES = 100;

    @Parameters({ "broker-1-port", "broker-1-ssl-port", "broker-1-rest-port", "broker-1-hostname", "broker-2-port",
            "broker-2-ssl-port", "broker-2-rest-port", "broker-2-hostname", "admin-username", "admin-password"})
    @BeforeClass
    public void setup(String portOne, String sslPortOne, String restPortOne, String hostnameOne, String portTwo,
                      String sslPortTwo, String restPortTwo, String hostnameTwo, String adminUsername,
                      String adminPassword, ITestContext context) throws Exception {

        StartupContext startupContext = new StartupContext();
        TestConfigProvider configProvider = new TestConfigProvider();

        BrokerHaConfiguration haConfiguration = new BrokerHaConfiguration();
        haConfiguration.setEnabled(true);
        haConfiguration.setStrategy("org.wso2.broker.coordination.rdbms.RdbmsHaStrategy");
        Map<String, String> rdbmsCoordinationOptions = new HashMap<>();
        rdbmsCoordinationOptions.put(
                RdbmsCoordinationConstants.HEARTBEAT_INTERVAL, Integer.toString(heartbeatInterval));
        rdbmsCoordinationOptions.put(RdbmsCoordinationConstants.COORDINATOR_ENTRY_CREATION_WAIT_TIME,
                                     Integer.toString(coordinatorEntryCreationWaitTime));
        haConfiguration.setOptions(rdbmsCoordinationOptions);
        configProvider.registerConfigurationObject(BrokerHaConfiguration.NAMESPACE, haConfiguration);

        startupContext.registerService(BrokerHaConfiguration.class, haConfiguration);

        nodeOne = new Node(hostnameOne, portOne, sslPortOne, restPortOne, adminUsername, adminPassword,
                           startupContext, configProvider);
        nodeOne.startUp();

        nodeTwo = new Node(hostnameTwo, portTwo, sslPortTwo, restPortTwo, adminUsername, adminPassword,
                           startupContext, configProvider);
        nodeTwo.startUp();

        //Allow for initial coordinator entry creation
        Thread.sleep(heartbeatInterval + coordinatorEntryCreationWaitTime + 1000);
    }

    @Test
    public void testSingleActiveNode() throws Exception {
        Assert.assertNotEquals(nodeOne.isActiveNode(), nodeTwo.isActiveNode(), "Two nodes marked as active nodes");
    }

    @Test(dependsOnMethods = "testSingleActiveNode")
    public void testSendReceiveForActiveNodeBeforeFailover() throws Exception {
        if (nodeOne.isActiveNode()) {
            activeNode = nodeOne;
            passiveNode = nodeTwo;
        } else if (nodeTwo.isActiveNode()) {
            activeNode = nodeTwo;
            passiveNode = nodeOne;
        } else {
            Assert.fail("No ACTIVE node!");
        }
        sendReceiveForNode("testSendReceiveForActiveNodeBeforeFailover",
                           activeNode.getHostname(), activeNode.getPort());
    }

    @Test(dependsOnMethods = "testSendReceiveForActiveNodeBeforeFailover", expectedExceptions = JMSException.class)
    public void testSendReceiveForPassiveNodeBeforeFailover() throws Exception {
        sendReceiveForNode("testSendReceiveForPassiveNodeBeforeFailover",
                           passiveNode.getHostname(), passiveNode.getPort());
    }

    @Test(dependsOnMethods = "testSendReceiveForPassiveNodeBeforeFailover")
    public void testFailoverWithActiveNodeShutdown() throws Exception {
        sendMessagesForSyncTest();
        activeNode.shutdown();
        //Change to expected states
        if (nodeOne.equals(passiveNode)) {
            activeNode = nodeOne;
            passiveNode = nodeTwo;
        } else {
            activeNode = nodeTwo;
            passiveNode = nodeOne;
        }

        //Allow for failover
        Thread.sleep(3 * heartbeatInterval + coordinatorEntryCreationWaitTime + 1000);

        Assert.assertTrue(activeNode.isActiveNode());
    }

    private void sendMessagesForSyncTest() throws NamingException, JMSException {
        //Send messages for the sync test prior to initializing failover
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin",
                                          activeNode.getHostname(), activeNode.getPort())
                .withQueue(SYNC_TEST_QUEUE_NAME)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(SYNC_TEST_QUEUE_NAME);
        MessageProducer producer = producerSession.createProducer(queue);

        for (int i = 0; i < SYNC_TEST_NUMBER_OF_MESSAGES; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        producerSession.close();
        connection.close();
    }

    @Test(dependsOnMethods = "testFailoverWithActiveNodeShutdown")
    public void testSendReceiveForActiveNodeAfterFailover() throws Exception {
        sendReceiveForNode("testSendReceiveForActiveNodeAfterFailover",
                           activeNode.getHostname(), activeNode.getPort());
    }

    @Test(dependsOnMethods = "testSendReceiveForActiveNodeAfterFailover", expectedExceptions = JMSException.class)
    public void testSendReceiveForPassiveNodeAfterFailover() throws Exception {
        sendReceiveForNode("testSendReceiveForPassiveNodeAfterFailover",
                           passiveNode.getHostname(), passiveNode.getPort());
    }

    @Test(dependsOnMethods = "testSendReceiveForActiveNodeAfterFailover")
    public void testSyncingOnFailover() throws Exception {
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin",
                                          activeNode.getHostname(), activeNode.getPort())
                .withQueue(SYNC_TEST_QUEUE_NAME)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // Consume messages send to the original active node
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(SYNC_TEST_QUEUE_NAME);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        for (int i = 0; i < SYNC_TEST_NUMBER_OF_MESSAGES; i++) {
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        subscriberSession.close();
        connection.close();
    }

    private void sendReceiveForNode(String queueName, String hostname, String port) throws Exception {
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin", hostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 10;
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

    @AfterClass
    public void teardown(ITestContext context) throws Exception {
        nodeTwo.shutdown();
    }

}
