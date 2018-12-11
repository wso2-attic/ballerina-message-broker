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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Test class to start broker node.
 * Node two is active, start node one
 */
@Test(groups = {"StartTestClass"}, dependsOnGroups = "ShutdownTestClass")
public class StartTest {

    private int node;
    private Connection connectiondb;
    private static final String queueName = "testQueue";
    private static final Logger LOGGER = LoggerFactory.getLogger(KillTest.class);
    private File file = new File(getClass().getResource("/docker-compose.yml").getFile());

    @Parameters({"db-username", "db-password", "admin-username", "admin-password", "broker-1-hostname", "broker-1-port",
            "broker-2-hostname", "broker-2-port"})
    @BeforeClass
    public void setUp(String dbUsername, String dbPassword, String username, String password, String hostnameOne,
                      String portOne, String hostnameTwo, String portTwo) throws SQLException, IOException {
        Runtime.getRuntime().exec("docker-compose -f " + file.getPath() + " up");
        Awaitility.await().atMost(240, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> isConnectionAvailable(username, password, hostnameOne, portOne));

        String mysqlUrl = "jdbc:mysql://localhost:3307/brokerdb";
        connectiondb = DriverManager.getConnection(mysqlUrl, dbUsername, dbPassword);
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

        Runtime.getRuntime().exec("docker stop brokernode1");
        Awaitility.await().atMost(180, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> isConnectionAvailable(username, password, hostnameTwo, portTwo));

    }

    @Parameters({"admin-username", "admin-password", "broker-1-hostname", "broker-1-port"})
    @Test(description = "Confirms node one is not started")
    public void testNodeOneAvailableBeforeStart(String username, String password, String hostnameOne, String portOne) {
        Assert.assertFalse(isConnectionAvailable(username, password, hostnameOne, portOne));
    }

    @Parameters({"admin-username", "admin-password", "broker-2-hostname", "broker-2-port"})
    @Test(description = "Confirms node two is in active mode", dependsOnMethods = "testNodeOneAvailableBeforeStart")
    public void testNodeTwoAvailableBeforeStart(String username, String password, String hostnameTwo, String portTwo) {
        Assert.assertTrue(isConnectionAvailable(username, password, hostnameTwo, portTwo));
    }

    @Test(description = "Confirms only a single node is available before start",
            dependsOnMethods = "testNodeTwoAvailableBeforeStart")
    public void testDataBaseBeforeStart() throws SQLException {
        String query = "select count(*) from MB_NODE_HEARTBEAT";
        Statement statement = connectiondb.createStatement();
        ResultSet resultSets = statement.executeQuery(query);

        while (resultSets.next()) {
            node = resultSets.getInt("count(*)");
        }
        Assert.assertEquals(node, 1, "Both nodes are started");
        statement.close();
    }

    @Parameters({"broker-1-home"})
    @Test(description = "Confirms both nodes are started", dependsOnMethods = "testDataBaseBeforeStart")
    public void testDataBaseAfterStart() throws IOException, SQLException {
        ClusterUtils.startBrokerNode("brokernode1");
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
        Assert.assertEquals(node, 2, "Both nodes are not started");
        statement.close();
    }

    @Parameters({"admin-username", "admin-password", "broker-1-hostname", "broker-1-port"})
    @Test(description = "Confirms node one is in passive mode", dependsOnMethods = "testDataBaseAfterStart")
    public void testNodeOneAvailableAfterStart(String username, String password, String hostnameOne, String portOne) {
        Awaitility.await().atMost(60, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> !isConnectionAvailable(username, password, hostnameOne, portOne));
        Assert.assertFalse(isConnectionAvailable(username, password, hostnameOne, portOne));
    }

    @Parameters({"admin-username", "admin-password", "broker-2-hostname", "broker-2-port"})
    @Test(description = "Confirms node two in active mode", dependsOnMethods = "testNodeOneAvailableAfterStart")
    public void checkNodeTwoAvailableAfterStart(String username, String password, String hostnameTwo, String portTwo) {
        Assert.assertTrue(isConnectionAvailable(username, password, hostnameTwo, portTwo));
    }

    @Parameters({"admin-username", "admin-password", "broker-1-hostname", "broker-1-port", "broker-2-hostname",
            "broker-2-port", "db-hostname", "db-port"})
    @AfterClass
    public void tearDown(String username, String password, String hostnameOne, String portOne, String hostnameTwo,
                         String portTwo, String dbHostname, String dbPort) throws IOException, InterruptedException {
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
            InitialContext ctx = ClientHelper.getInitialContextBuilder(userName, password, hostname, port)
                    .withQueue(queueName).build();
            ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup(ClientHelper.CONNECTION_FACTORY);
            javax.jms.Connection connection = connectionFactory.createConnection();
            connection.start();
            LOGGER.info("Connection available...");
            return true;
        } catch (NamingException | JMSException e) {
            LOGGER.info("Connection not available...", e);
            return false;
        }
    }
}
