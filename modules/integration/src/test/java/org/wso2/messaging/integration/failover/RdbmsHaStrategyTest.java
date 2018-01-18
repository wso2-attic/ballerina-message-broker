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

/**
 * Test class for the {@link org.wso2.broker.coordination.rdbms.RdbmsHaStrategy} implementation.
 */
public class RdbmsHaStrategyTest {

    private String hostnameOne;
    private String hostnameTwo;

    private String portOne;
    private String portTwo;

    private Node nodeOne;
    private Node nodeTwo;

    private Node activeNode;
    private Node passiveNode;

    private int heartbeatInterval = 5000;
    private int coordinatorEntryCreationWaitTime = 3000;

    @Parameters({ "broker-1-port", "broker-1-ssl-port", "broker-1-rest-port", "broker-1-hostname", "broker-2-port",
            "broker-2-ssl-port", "broker-2-rest-port", "broker-2-hostname", "admin-username", "admin-password"})
    @BeforeClass
    public void setup(String portOne, String sslPortOne, String restPortOne, String hostnameOne, String portTwo,
                      String sslPortTwo, String restPortTwo, String hostnameTwo, String adminUsername,
                      String adminPassword, ITestContext context) throws Exception {

        this.portOne = portOne;
        this.portTwo = portTwo;
        this.hostnameOne = hostnameOne;
        this.hostnameTwo = hostnameTwo;

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
        String hostname = null;
        String port = null;
        if (nodeOne.isActiveNode()) {
            activeNode = nodeOne;
            passiveNode = nodeTwo;
            hostname = hostnameOne;
            port = portOne;
        } else if (nodeTwo.isActiveNode()) {
            activeNode = nodeTwo;
            passiveNode = nodeOne;
            hostname = hostnameTwo;
            port = portTwo;
        } else {
            Assert.fail("No ACTIVE node!");
        }
        sendReceiveForNode("testSendReceiveForActiveNodeBeforeFailover", hostname, port);
    }

    @Test(dependsOnMethods = "testSendReceiveForActiveNodeBeforeFailover", expectedExceptions = JMSException.class)
    public void testSendReceiveForPassiveNodeBeforeFailover() throws Exception {
        String hostname = null;
        String port = null;
        if (nodeOne.equals(passiveNode)) {
            hostname = hostnameOne;
            port = portOne;
        } else if (nodeTwo.equals(passiveNode)) {
            hostname = hostnameTwo;
            port = portTwo;
        } else {
            Assert.fail("No PASSIVE node in the two node HA group!");
        }
        sendReceiveForNode("testSendReceiveForPassiveNodeBeforeFailover", hostname, port);
    }

    @Test(dependsOnMethods = "testSendReceiveForPassiveNodeBeforeFailover")
    public void testFailoverWithActiveNodeShutdown() throws Exception {
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

    @Test(dependsOnMethods = "testFailoverWithActiveNodeShutdown")
    public void testSendReceiveForActiveNodeAfterFailover() throws Exception {
        String hostname = null;
        String port = null;
        if (activeNode.equals(nodeOne)) {
            passiveNode = nodeTwo;
            hostname = hostnameOne;
            port = portOne;
        } else if (activeNode.equals(nodeTwo)) {
            passiveNode = nodeOne;
            hostname = hostnameTwo;
            port = portTwo;
        } else {
            Assert.fail("No ACTIVE node!");
        }
        sendReceiveForNode("testSendReceiveForActiveNodeAfterFailover", hostname, port);
    }

    @Test(dependsOnMethods = "testSendReceiveForActiveNodeAfterFailover", expectedExceptions = JMSException.class)
    public void testSendReceiveForPassiveNodeAfterFailover() throws Exception {
        String hostname;
        String port;
        if (passiveNode.equals(nodeOne)) {
            hostname = hostnameOne;
            port = portOne;
        } else {
            hostname = hostnameTwo;
            port = portTwo;
        }
        sendReceiveForNode("testSendReceiveForPassiveNodeAfterFailover", hostname, port);
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
