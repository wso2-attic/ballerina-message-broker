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

import io.ballerina.messaging.broker.integration.util.ClientHelper;
import io.ballerina.messaging.broker.integration.util.ClusterUtils;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Test class to restart broker node
 * Node one is active and node two is passive, restart node one
 */
@Test(groups = {"RestartTestClass"}, dependsOnGroups = "StartTestClass")
public class RestartTest {

    private int node;
    private static final String queueName = "testQueue";
    private Connection connection;
    private InitialContext ctx;
    private static final Logger LOGGER = LoggerFactory.getLogger(KillTest.class);
    private File file = new File(getClass().getResource("/docker-compose.yml").getFile());

    @Parameters({"admin-username", "admin-password", "broker-1-hostname", "broker-1-port", "db-username",
            "db-password"})
    @BeforeClass
    public void setUp(String username, String password, String hostnameOne, String portOne, String dbUsername,
                      String dbPassword) throws JMSException, IOException, SQLException {
        Runtime.getRuntime().exec("docker-compose -f " + file.getPath() + " up");
        Awaitility.await().atMost(240, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> isConnectionAvailable(username, password, hostnameOne, portOne));

        String mysqlUrl = "jdbc:mysql://localhost:3307/brokerdb";
        java.sql.Connection connectiondb = DriverManager.getConnection(mysqlUrl, dbUsername, dbPassword);
        String query = "select count(*) from MB_NODE_HEARTBEAT";
        Statement statement = connectiondb.createStatement();

        Awaitility.await().atMost(60, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> {
                    ResultSet resultSets = statement.executeQuery(query);
                    while (resultSets.next()) {
                        node = resultSets.getInt("count(*)");
                    }
                    return node == 2;
                });
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 10;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        producerSession.close();
        connection.close();
    }

    @Parameters({"admin-username", "admin-password", "broker-1-hostname", "broker-1-port"})
    @Test(description = "Confirms node one is in active mode")
    public void testNodeOneAvailableBeforeRestart(String username, String password, String hostnameOne,
                                                  String portOne) {
        Assert.assertTrue(isConnectionAvailable(username, password, hostnameOne, portOne));
    }

    @Parameters({"admin-username", "admin-password", "broker-2-hostname", "broker-2-port"})
    @Test(description = "Confirms node two in passive mode", dependsOnMethods = "testNodeOneAvailableBeforeRestart")
    public void testNodeTwoAvailableBeforeRestart(String username, String password, String hostnameTwo,
                                                  String portTwo) {
        Assert.assertFalse(isConnectionAvailable(username, password, hostnameTwo, portTwo));
    }

    @Parameters({"admin-username", "admin-password", "broker-2-port", "broker-2-hostname"})
    @Test(description = "Confirms node one is restarted", dependsOnMethods = "testNodeTwoAvailableBeforeRestart")
    public void testRestartBrokerNode(String username, String password, String portTwo, String hostnameTwo)
            throws IOException {
        ClusterUtils.restartBrokerNode("brokernode1", username, password, hostnameTwo, portTwo);
    }

    @Parameters({"admin-username", "admin-password", "broker-2-port", "broker-2-hostname"})
    @Test(description = "Confirms node two is active", dependsOnMethods = "testRestartBrokerNode")
    public void testNodeTwoAvailableAfterRestart(String username, String password, String portTwo, String hostnameTwo) {
        Assert.assertTrue(isConnectionAvailable(username, password, hostnameTwo, portTwo));
    }

    //    @Parameters({"admin-username", "admin-password", "broker-2-port", "broker-2-hostname"})
    @Test(description = "Confirms message is received from active node",
            dependsOnMethods = "testNodeTwoAvailableAfterRestart")
    public void testReceiveMessage() throws JMSException, NamingException {
        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) ctx.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        for (int i = 0; i < 10; i++) {
            Message message = consumer.receive();
            Assert.assertNotNull(String.valueOf(message), "Message #" + i + " was not received");
        }
        subscriberSession.close();
        connection.close();
    }

    @Parameters({"admin-username", "admin-password", "broker-1-hostname", "broker-1-port", "broker-2-hostname",
            "broker-2-port", "db-hostname", "db-port"})
    @AfterClass
    public void tearDown(String username, String password, String hostnameOne, String portOne, String hostnameTwo,
                         String portTwo, String dbHostname, String dbPort) throws IOException {
        Runtime.getRuntime().exec("docker stop brokernode1 && docker rm $_");
        Runtime.getRuntime().exec("docker-compose -f " + file.getPath() + " down");
        Awaitility.await().atMost(60, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> ClusterUtils.isPortAvailable(hostnameOne, Integer.parseInt(portOne)));
        Awaitility.await().atMost(60, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> ClusterUtils.isPortAvailable(hostnameTwo, Integer.parseInt(portTwo)));
        Awaitility.await().atMost(60, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> ClusterUtils.isPortAvailable(dbHostname, Integer.parseInt(dbPort)));
    }

    public boolean isConnectionAvailable(String userName, String password, String hostname, String port) {
        try {
            ctx = ClientHelper.getInitialContextBuilder(userName, password, hostname, port)
                    .withQueue(queueName).build();
            ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup(ClientHelper.CONNECTION_FACTORY);
            connection = connectionFactory.createConnection();
            connection.start();
            LOGGER.info("Connection available...");
            return true;
        } catch (NamingException | JMSException e) {
            LOGGER.info("Connection not available...", e);
            return false;
        }
    }
}
