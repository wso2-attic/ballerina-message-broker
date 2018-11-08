/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.messaging.broker.integration.cluster;

import io.ballerina.messaging.broker.integration.util.ClusterUtils;
import org.awaitility.Awaitility;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Test class for shutdown broker node
 * Node one is active and node two is passive, shutdown node one
 * */
@Test(groups = {"ShutdownTestClass"})
public class ShutdownTest {

    private static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String QUEUE_NAME_PREFIX = "queue.";
    private static final String CF_NAME = "qpidConnectionfactory";
    private static final String CARBON_CLIENT_ID = "carbon";
    private static final String CARBON_VIRTUAL_HOST_NAME = "carbon";
    private static final String queueName = "testQueue";
    private static InitialContext ctx;
    private static ConnectionFactory connFactory;

    @Parameters({"admin-username", "admin-password", "broker-1-hostname", "broker-1-port", "broker-2-hostname",
            "broker-2-port"})
    @BeforeClass
    public void setUp(String username, String password, String hostnameOne, String portOne,
                      String hostnameTwo, String portTwo) throws NamingException, JMSException {

        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, QPID_ICF);
        properties.put(CF_NAME_PREFIX + CF_NAME, getTCPConnectionURL(username, password, hostnameOne, portOne,
                hostnameTwo, portTwo));
        properties.put(QUEUE_NAME_PREFIX + queueName, queueName);
        ctx = new InitialContext(properties);

        connFactory = (ConnectionFactory) ctx.lookup(CF_NAME);
        Connection queueConnectionOne = connFactory.createConnection();
        queueConnectionOne.start();

        Session producerSession = queueConnectionOne.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 10;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        producerSession.close();
        queueConnectionOne.close();
    }

    @Parameters({"broker-1-port", "broker-1-hostname"})
    @Test(description = "Confirms node one is in active mode")
    public void testNodeOneAvailableBeforeShutdown(String portOne, String hostnameOne) {
        Assert.assertFalse(ClusterUtils.isPortAvailable(hostnameOne, Integer.parseInt(portOne)),
                "Broker node one is not active");
    }

    @Parameters({"broker-2-port", "broker-2-hostname"})
    @Test(description = "Confirms node two in passive mode", dependsOnMethods = "testNodeOneAvailableBeforeShutdown")
    public void testNodeTwoAvailableBeforeShutdown(String portTwo, String hostnameTwo) {
        Assert.assertTrue(ClusterUtils.isPortAvailable(hostnameTwo, Integer.parseInt(portTwo)),
                "Broker node two is not passive");
    }
    @Parameters({"broker-1-port", "broker-1-hostname"})
    @Test(description = "Confirms node one is shutdown", dependsOnMethods = "testNodeTwoAvailableBeforeShutdown")
    public void testShutdownNodeOne(String portOne, String hostnameOne) throws IOException, InterruptedException {
        ClusterUtils.shutdownBrokerNode(portOne);
        Awaitility.await().atMost(3000, TimeUnit.MILLISECONDS)
                .pollInterval(5, TimeUnit.MILLISECONDS)
                .until(() -> ClusterUtils.isPortAvailable(hostnameOne, Integer.parseInt(portOne)));
        Assert.assertTrue(ClusterUtils.isPortAvailable(hostnameOne, Integer.parseInt(portOne)),
                "Broker node one is not killed");
        Assert.assertTrue(ClusterUtils.isPortAvailable(hostnameOne, Integer.parseInt(portOne)),
                "Broker node one is not shutdown");
    }

    @Parameters({"broker-2-port", "broker-2-hostname"})
    @Test(description = "Confirms node two is active", dependsOnMethods = "testShutdownNodeOne")
    public void testNodeTwoActive(String portTwo, String hostnameTwo) throws NamingException, JMSException {
        Awaitility.await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> !ClusterUtils.isPortAvailable(hostnameTwo, Integer.parseInt(portTwo)));
        Assert.assertFalse(ClusterUtils.isPortAvailable(hostnameTwo, Integer.parseInt(portTwo)),
                "Broker node two is not active");
    }

    @Test(description = "Confirms message is received from active node", dependsOnMethods = "testNodeTwoActive")
    public void testReceiveMessage() throws JMSException, NamingException {
        Connection queueConnectionTwo = connFactory.createConnection();
        queueConnectionTwo.start();
        Session subscriberSession = queueConnectionTwo.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) ctx.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        for (int i = 0; i < 10; i++) {
            Message message = consumer.receive();
            Assert.assertNotNull(String.valueOf(message), "Message #" + i + " was not received");
        }
        subscriberSession.close();
        queueConnectionTwo.close();
    }

    /**
     * Method for get the TCP connection url
     *
     * @param password    admin password
     * @param hostnameOne hostname of broker node one
     * @param portOne     port of broker node one
     * @param hostnameTwo hostname of broker node two
     * @param portTwo     port of broker node two
     */
    private static String getTCPConnectionURL(String username, String password, String hostnameOne, String portOne,
                                              String hostnameTwo, String portTwo) {
        return new StringBuffer()
                .append("amqp://").append(username).append(":").append(password)
                .append("@").append(CARBON_CLIENT_ID)
                .append("/").append(CARBON_VIRTUAL_HOST_NAME)
                .append("?failover='roundrobin'&cyclecount='2'&brokerlist='tcp://")
                .append(hostnameOne).append(":").append(portOne)
                .append("?retries='5'&connectdelay='50';tcp://")
                .append(hostnameTwo).append(":").append(portTwo)
                .append("?retries='5'&connectdelay='50''")
                .toString();
    }
}
