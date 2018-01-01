/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.broker.coordination.CoordinationException;
import org.wso2.broker.coordination.node.NodeDetail;
import org.wso2.broker.coordination.rdbms.CoordinationConfiguration;
import org.wso2.broker.coordination.rdbms.RdbmsCoordinationDaoImpl;
import org.wso2.broker.coordination.rdbms.RdbmsCoordinationStrategy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

/**
 * Test class for RDBMS coordination with Derby.
 */
public class RdbmsCoordinationStrategyTest {

    /**
     * Class logger.
     */
    private Logger logger = LoggerFactory.getLogger(RdbmsCoordinationStrategyTest.class);

    /**
     * Instance of {@link RdbmsCoordinationStrategy} being tested.
     */
    private RdbmsCoordinationStrategy rdbmsCoordinationStrategy;

    private String databaseUrl = "jdbc:derby:memory:mbDB";

    private String nodeOneId = "7a473f76-e22b-11e7-80c1-9a214cf093ae";
    private String nodeTwoId = "bc993ca8-e22b-11e7-80c1-9a214cf093ae";

    private static final String CREATE_MB_COORDINATOR_HEARTBEAT_TABLE = "CREATE TABLE MB_COORDINATOR_HEARTBEAT ("
            + "ANCHOR INT NOT NULL, "
            + "NODE_ID VARCHAR(512) NOT NULL, "
            + "LAST_HEARTBEAT BIGINT NOT NULL, "
            + "PRIMARY KEY (ANCHOR))";

    private static final String CREATE_MB_NODE_HEARTBEAT_TABLE = "CREATE TABLE MB_NODE_HEARTBEAT ("
            + "NODE_ID VARCHAR(512) NOT NULL, "
            + "LAST_HEARTBEAT BIGINT NOT NULL, "
            + "IS_NEW_NODE SMALLINT NOT NULL, "
            + "PRIMARY KEY (NODE_ID))";

    @BeforeClass
    public void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection(databaseUrl + ";create=true");
        Statement statement = connection.createStatement();
        statement.executeUpdate(CREATE_MB_COORDINATOR_HEARTBEAT_TABLE);
        statement.executeUpdate(CREATE_MB_NODE_HEARTBEAT_TABLE);
        connection.close();

        CoordinationConfiguration.RdbmsCoordinationConfiguration rdbmsCoordinationConfiguration =
                new CoordinationConfiguration.RdbmsCoordinationConfiguration();
        rdbmsCoordinationConfiguration.setNodeId(nodeOneId);
        rdbmsCoordinationConfiguration.setHeartbeatInterval(5000);
        rdbmsCoordinationConfiguration.setCoordinatorEntryCreationWaitTime(3000);

        HikariConfig hikariDatasourceConfig = new HikariConfig();
        hikariDatasourceConfig.setJdbcUrl(databaseUrl);
        hikariDatasourceConfig.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        hikariDatasourceConfig.setAutoCommit(false);
        DataSource datasource = new HikariDataSource(hikariDatasourceConfig);

        RdbmsCoordinationDaoImpl rdbmsCoordinationDaoImpl = new RdbmsCoordinationDaoImpl(datasource);
        rdbmsCoordinationStrategy =
                new RdbmsCoordinationStrategy(rdbmsCoordinationDaoImpl, rdbmsCoordinationConfiguration);
        rdbmsCoordinationStrategy.start();
    }

    @Test(description = "Test if coordinator state is maintained")
    public void testCoordinatorState() {
        Assert.assertTrue(rdbmsCoordinationStrategy.isCoordinator());
    }

    @Test(description = "Test retrieval of coordinator information")
    public void testCoordinatorInformationRetrieval() throws CoordinationException {
        Assert.assertEquals(rdbmsCoordinationStrategy.getNodeIdentifierOfCoordinator(), nodeOneId,
                "Incorrect coordinator node ID retrieved.");
    }

    @Test(description = "Test retrieval of node details")
    public void testRetrieveNodeDetailsWithMemberAddition() throws Exception {
        Connection connection = DriverManager.getConnection(databaseUrl);
        Statement statement = connection.createStatement();
        long secondNodeHeartbeat = System.currentTimeMillis();
        statement.executeUpdate("INSERT INTO MB_NODE_HEARTBEAT "
                + "VALUES ('bc993ca8-e22b-11e7-80c1-9a214cf093ae', " + secondNodeHeartbeat + ", 1)");
        connection.close();
        List<String> allNodeIds = rdbmsCoordinationStrategy.getAllNodeIdentifiers();
        if (!(allNodeIds.size() == 2 && allNodeIds.contains(nodeOneId) && allNodeIds.contains(nodeTwoId))) {
            Assert.fail("Incorrect IDs retrieved as Node Identifiers");
        }
        List<NodeDetail> allNodeDetails = rdbmsCoordinationStrategy.getAllNodeDetails();
        if (allNodeDetails.size() == 2) {
            for (NodeDetail nodeDetail : allNodeDetails) {
                if (nodeDetail.getNodeId().equals(nodeOneId)) {
                    Assert.assertTrue(nodeDetail.isCoordinator(), "Coordinator not identified as coordinator");
                } else if (nodeDetail.getNodeId().equals(nodeTwoId)) {
                    Assert.assertFalse(nodeDetail.isCoordinator(), "Candidate node identified as coordinator");
                } else {
                    Assert.fail("Details do not match an existing node");
                }
            }
        } else {
            Assert.fail("Details retrieved do not match number of nodes");
        }
    }

    @AfterClass
    public void tearDown() throws Exception {
        try {
            DriverManager.getConnection(databaseUrl + ";drop=true").close();
        } catch (SQLException e) {
            if (!e.getMessage().equals("Database 'memory:mbDB' dropped.")) {
                throw e;
            }
        }
    }

}
