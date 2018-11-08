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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

/**
 * Test class to start broker node.
 * Node two is active, start node one
 */
@Test(groups = {"StartTestClass"})
public class StartTest {

    private int node;
    private Connection connection;

    /**
     * Create database connection
     */
    @Parameters({"db-username", "db-password"})
    @BeforeClass
    public void setup(String dbUsername, String dbPassword) throws SQLException {
        String mysqlUrl = "jdbc:mysql://localhost:3306/brokerdb";
        connection = DriverManager.getConnection(mysqlUrl, dbUsername, dbPassword);
    }

    @Parameters({"broker-1-port", "broker-1-hostname"})
    @Test(description = "Confirms node one is not started")
    public void testNodeOneAvailableBeforeStart(String portOne, String hostnameOne) {
        Assert.assertTrue(ClusterUtils.isPortAvailable(hostnameOne, Integer.parseInt(portOne)),
                "Broker node one is active");
    }

    @Parameters({"broker-2-port", "broker-2-hostname"})
    @Test(description = "Confirms node two is in active mode", dependsOnMethods = "testNodeOneAvailableBeforeStart")
    public void testNodeTwoAvailableBeforeStart(String portTwo, String hostnameTwo) {
        Assert.assertFalse(ClusterUtils.isPortAvailable(hostnameTwo, Integer.parseInt(portTwo)),
                "Broker node two is not active");
    }

    @Test(description = "Confirms only a single node is available before start",
            dependsOnMethods = "testNodeTwoAvailableBeforeStart")
    public void testDataBaseBeforeStart() throws SQLException {
        String query = "select count(*) from mb_node_heartbeat";
        Statement statement = connection.createStatement();
        ResultSet resultSets = statement.executeQuery(query);

        while (resultSets.next()) {
            node = resultSets.getInt("count(*)");
        }
        Assert.assertEquals(node, 1, "Both nodes are started");
        statement.close();
    }

    @Parameters({"broker-1-home"})
    @Test(description = "Confirms both nodes are started", dependsOnMethods = "testDataBaseBeforeStart")
    public void testDataBaseAfterStart(String brokerHome) throws IOException, SQLException, InterruptedException {
        ClusterUtils.startBrokerNode(brokerHome);
        String query = "select count(*) from mb_node_heartbeat";
        Statement statement = connection.createStatement();

        Awaitility.await().atMost(3000, TimeUnit.MILLISECONDS)
                .pollInterval(5, TimeUnit.MILLISECONDS)
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

    @Parameters({"broker-1-port", "broker-1-hostname"})
    @Test(description = "Confirms node one is in passive mode", dependsOnMethods = "testDataBaseAfterStart")
    public void testNodeOneAvailableAfterStart(String portOne, String hostnameOne) {
        Assert.assertTrue(ClusterUtils.isPortAvailable(hostnameOne, Integer.parseInt(portOne)),
                "Broker node one is active");
    }

    @Parameters({"broker-2-port", "broker-2-hostname"})
    @Test(description = "Confirms node two in active mode", dependsOnMethods = "testNodeOneAvailableAfterStart")
    public void checkNodeTwoAvailableAfterStart(String portTwo, String hostnameTwo) {
        Assert.assertFalse(ClusterUtils.isPortAvailable(hostnameTwo, Integer.parseInt(portTwo)),
                "Broker node two is not active");
    }
}


