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

package io.ballerina.messaging.broker.integration.failover;

import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.coordination.BrokerHaConfiguration;
import io.ballerina.messaging.broker.coordination.rdbms.RdbmsCoordinationConstants;
import io.ballerina.messaging.broker.coordination.rdbms.RdbmsHaStrategy;
import io.ballerina.messaging.broker.integration.util.BrokerNode;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import io.ballerina.messaging.broker.integration.util.TestConfigProvider;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

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
 * Test class for the {@link RdbmsHaStrategy} implementation.
 *
 * This class introduces tests for fail-over by simulating a fail-over scenario, based on the RDBMS coordination
 * strategy.
 *
 * Two MB instances (nodes) are started up on different ports. Fail-over is simulated by pausing the coordination
 * strategy for the active node, which prevents the active node from updating the DB entry, resulting in the originally
 * passive node becoming the active node, and the originally active node being marked as the passive node.
 */
public class RdbmsHaStrategyTest {

    private BrokerNode brokerNodeOne;
    private BrokerNode brokerNodeTwo;

    private BrokerNode activeBrokerNode;
    private BrokerNode passiveBrokerNode;

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
        haConfiguration.setStrategy(RdbmsHaStrategy.class.getCanonicalName());
        Map<String, String> rdbmsCoordinationOptions = new HashMap<>();
        rdbmsCoordinationOptions.put(
                RdbmsCoordinationConstants.HEARTBEAT_INTERVAL, Integer.toString(heartbeatInterval));
        rdbmsCoordinationOptions.put(RdbmsCoordinationConstants.COORDINATOR_ENTRY_CREATION_WAIT_TIME,
                                     Integer.toString(coordinatorEntryCreationWaitTime));
        haConfiguration.setOptions(rdbmsCoordinationOptions);
        configProvider.registerConfigurationObject(BrokerHaConfiguration.NAMESPACE, haConfiguration);

        startupContext.registerService(BrokerHaConfiguration.class, haConfiguration);

        brokerNodeOne = new BrokerNode(hostnameOne, portOne, sslPortOne, restPortOne, adminUsername, adminPassword,
                                       startupContext, configProvider);
        brokerNodeOne.startUp();

        brokerNodeTwo = new BrokerNode(hostnameTwo, portTwo, sslPortTwo, restPortTwo, adminUsername, adminPassword,
                                       startupContext, configProvider);
        brokerNodeTwo.startUp();

        //Allow for initial coordinator entry creation
        Thread.sleep(heartbeatInterval + coordinatorEntryCreationWaitTime + 1000);
    }

    @Test(description = "Confirm election of a single active node, and marking of the remaining node as passive")
    public void testSingleActiveNode() throws Exception {
        Assert.assertNotEquals(brokerNodeOne.isActiveNode(), brokerNodeTwo.isActiveNode(),
                               "Two nodes marked as active nodes");
    }

    @Test(dependsOnMethods = "testSingleActiveNode",
          description = "Confirm expected behaviour with the active node before fail-over")
    public void testSendReceiveForActiveNodeBeforeFailover() throws Exception {
        if (brokerNodeOne.isActiveNode()) {
            activeBrokerNode = brokerNodeOne;
            passiveBrokerNode = brokerNodeTwo;
        } else if (brokerNodeTwo.isActiveNode()) {
            activeBrokerNode = brokerNodeTwo;
            passiveBrokerNode = brokerNodeOne;
        } else {
            Assert.fail("No ACTIVE node!");
        }
        sendReceiveForNode("testSendReceiveForActiveNodeBeforeFailover",
                           activeBrokerNode.getHostname(), activeBrokerNode.getPort());
    }

    @Test(dependsOnMethods = "testSendReceiveForActiveNodeBeforeFailover",
          expectedExceptions = JMSException.class,
          description = "Confirm expected behaviour with the passive node before fail-over")
    public void testSendReceiveForPassiveNodeBeforeFailover() throws Exception {
        sendReceiveForNode("testSendReceiveForPassiveNodeBeforeFailover",
                           passiveBrokerNode.getHostname(), passiveBrokerNode.getPort());
    }

    @Test(dependsOnMethods = "testSendReceiveForPassiveNodeBeforeFailover",
          description = "Confirm failing over, pausing the coordination strategy")
    public void testFailoverWithActiveNodePause() throws Exception {
        sendMessagesForSyncTest();
        activeBrokerNode.pause();
        //Change to expected states
        if (brokerNodeOne.equals(passiveBrokerNode)) {
            activeBrokerNode = brokerNodeOne;
            passiveBrokerNode = brokerNodeTwo;
        } else {
            activeBrokerNode = brokerNodeTwo;
            passiveBrokerNode = brokerNodeOne;
        }

        //Allow for failover
        Thread.sleep(3 * heartbeatInterval + coordinatorEntryCreationWaitTime + 1000);

        Assert.assertTrue(activeBrokerNode.isActiveNode());
        passiveBrokerNode.resume(); //resume the previously active node (now passive), which was paused
    }

    private void sendMessagesForSyncTest() throws NamingException, JMSException {
        //Send messages for the sync test prior to initializing failover
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin",
                                          activeBrokerNode.getHostname(), activeBrokerNode.getPort())
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

    @Test(dependsOnMethods = "testFailoverWithActiveNodePause",
          description = "Confirm expected behaviour with the active node after fail-over")
    public void testSendReceiveForActiveNodeAfterFailover() throws Exception {
        sendReceiveForNode("testSendReceiveForActiveNodeAfterFailover",
                           activeBrokerNode.getHostname(), activeBrokerNode.getPort());
    }

    @Test(dependsOnMethods = "testSendReceiveForActiveNodeAfterFailover",
          expectedExceptions = JMSException.class,
          description = "Confirm expected behaviour with the passive node after fail-over")
    public void testSendReceiveForPassiveNodeAfterFailover() throws Exception {
        sendReceiveForNode("testSendReceiveForPassiveNodeAfterFailover",
                           passiveBrokerNode.getHostname(), passiveBrokerNode.getPort());
    }

    @Test(dependsOnMethods = "testSendReceiveForActiveNodeAfterFailover",
          description = "Confirm expected behaviour syncing data from the database by the active node, after fail-over")
    public void testSyncingOnFailover() throws Exception {
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin",
                                          activeBrokerNode.getHostname(), activeBrokerNode.getPort())
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
        brokerNodeOne.shutdown();
        brokerNodeTwo.shutdown();
    }

}
