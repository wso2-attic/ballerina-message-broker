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

import io.ballerina.messaging.broker.coordination.CoordinationException;
import io.ballerina.messaging.broker.coordination.rdbms.RdbmsCoordinationDaoImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Class to test the error scenarios of the coordination DAO used with RDBMS coordination, by mocking the error
 * scenarios.
 */
public class RdbmsCoordinationDaoImplErrorTest {

    @Mock
    private DataSource mockDatasource;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    /**
     * The node ID used used in the test.
     */
    private String nodeID = "7a473f76-e22b-11e7-80c1-9a214cf093ae";

    /**
     * An instance of the {@link RdbmsCoordinationDaoImpl} whose error scenarios are being tested.
     */
    private RdbmsCoordinationDaoImpl rdbmsCoordinationDaoImpl;

    @BeforeMethod
    public void init() throws SQLException {
        MockitoAnnotations.initMocks(this);
        rdbmsCoordinationDaoImpl = new RdbmsCoordinationDaoImpl(mockDatasource);
        when(mockDatasource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test removing node heartbeat with SQL exception")
    public void testRemoveNodeHeartbeatWithException() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        rdbmsCoordinationDaoImpl.removeNodeHeartbeat(nodeID);
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test retrieving all heartbeat data with SQL exception")
    public void testGetAllHeartBeatDataWithException() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        rdbmsCoordinationDaoImpl.getAllHeartBeatData();
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test retrieving coordinator node ID with SQL exception")
    public void testGetCoordinatorNodeIdWithException() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        rdbmsCoordinationDaoImpl.getCoordinatorNodeId();
    }

    @Test(description = "Test retrieving coordinator node ID when the coordinator node ID is not available (null)")
    public void testNullCoordinatorNodeId() throws Exception {
        when(mockResultSet.next()).thenReturn(Boolean.FALSE);
        rdbmsCoordinationDaoImpl.getCoordinatorNodeId();
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test removing the coordinator with SQL exception")
    public void testRemoveCoordinatorWithException() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        rdbmsCoordinationDaoImpl.removeCoordinator();
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test checking if the coordinator is valid with SQL exception")
    public void testCheckIfCoordinatorValidWithException() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        rdbmsCoordinationDaoImpl.checkIfCoordinatorValid(1000);
    }

    @Test(description = "Test checking if coordinator is valid when there is no coordinator")
    public void testCheckIfCoordinatorValidWhenNoCoordinator() throws Exception {
        when(mockResultSet.next()).thenReturn(Boolean.FALSE);
        rdbmsCoordinationDaoImpl.checkIfCoordinatorValid(1000);
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test updating node heartbeat with SQL exception")
    public void testUpdateNodeHeartbeatWithException() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        rdbmsCoordinationDaoImpl.updateNodeHeartbeat(nodeID);
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test creating node heartbeat entry with SQL exception")
    public void testCreateNodeHeartbeatEntryWithException() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        rdbmsCoordinationDaoImpl.createNodeHeartbeatEntry(nodeID);
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test updating coordinator heartbeat with SQL exception")
    public void testUpdateCoordinatorHeartbeatWithException() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        rdbmsCoordinationDaoImpl.updateCoordinatorHeartbeat(nodeID);
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test marking node as not new with SQL exception")
    public void testMarkNodeAsNotNewWithException() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        rdbmsCoordinationDaoImpl.markNodeAsNotNew(nodeID);
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test creating coordinator entry with SQL exception")
    public void testCreateCoordinatorEntryWithException() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        rdbmsCoordinationDaoImpl.createCoordinatorEntry(nodeID);
    }

    @Test(description = "Test creating coordinator entry when an entry already exists")
    public void testCreateCoordinatorEntryWhenEntryExists() throws Exception {
        doThrow(new SQLIntegrityConstraintViolationException()).when(mockConnection).prepareStatement(anyString());
        Assert.assertFalse(rdbmsCoordinationDaoImpl.createCoordinatorEntry(nodeID));
    }

    @Test(expectedExceptions = CoordinationException.class,
            description = "Test checking if coordinator with SQL exception")
    public void testCheckIsCoordinatorWithException() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        rdbmsCoordinationDaoImpl.checkIsCoordinator(nodeID);
    }

    @Test(expectedExceptions = CoordinationException.class, description = "Test exception with roll back")
    public void testExceptionWithRollback() throws Exception {
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        doThrow(new SQLException()).when(mockConnection).rollback();
        rdbmsCoordinationDaoImpl.updateCoordinatorHeartbeat(nodeID);
    }

}
