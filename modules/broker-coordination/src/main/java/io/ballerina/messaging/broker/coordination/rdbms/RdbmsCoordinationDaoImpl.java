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

package io.ballerina.messaging.broker.coordination.rdbms;

import io.ballerina.messaging.broker.coordination.CoordinationException;
import io.ballerina.messaging.broker.coordination.node.NodeHeartbeatData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

/**
 * Abstraction of the underlying database used with RDBMS coordination.
 */
public class RdbmsCoordinationDaoImpl {

    /**
     * Class Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RdbmsCoordinationDaoImpl.class);

    /**
     * Connection pooled SQL data source object.
     */
    private DataSource datasource;

    /**
     * Default constructor which uses the provided datasource.
     *
     * @param datasource the datasource to use
     */
    public RdbmsCoordinationDaoImpl(DataSource datasource) {
        this.datasource = datasource;
    }

    /**
     * Remove heartbeat entry for the given node. This is normally done when the coordinator detects that the node
     * has left.
     *
     * @param nodeId local node ID
     * @throws CoordinationException if an error occurs while removing the node heartbeat entry (mostly due to a DB
     * error)
     */
    public void removeNodeHeartbeat(String nodeId) throws CoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String task = RdbmsCoordinationConstants.TASK_REMOVE_NODE_HEARTBEAT;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(RdbmsCoordinationConstants.PS_DELETE_NODE_HEARTBEAT);
            preparedStatement.setString(1, nodeId);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            rollback(connection, task);
            throw new CoordinationException("Error occurred while " + task, e);
        } finally {
            close(preparedStatement, task);
            close(connection, task);
        }
    }

    /**
     * Get node heart beat status for all the existing nodes.
     *
     * @return list of node heartbeat data
     * @throws CoordinationException if an error is detected while calling the store (mostly due to a DB error)
     */
    public List<NodeHeartbeatData> getAllHeartBeatData() throws CoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String task = RdbmsCoordinationConstants.TASK_GET_ALL_HEARTBEAT;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(RdbmsCoordinationConstants.PS_GET_ALL_NODE_HEARTBEAT);
            resultSet = preparedStatement.executeQuery();
            ArrayList<NodeHeartbeatData> nodeDataList = new ArrayList<>();
            while (resultSet.next()) {
                String nodeId = resultSet.getString(1);
                long lastHeartbeat = resultSet.getLong(2);
                boolean isNewNode = convertIntToBoolean(resultSet.getInt(3));
                NodeHeartbeatData heartBeatData = new NodeHeartbeatData(nodeId, lastHeartbeat, isNewNode);
                nodeDataList.add(heartBeatData);
            }
            return nodeDataList;
        } catch (SQLException e) {
            throw new CoordinationException("Error occurred while " + task, e);
        } finally {
            close(resultSet, task);
            close(preparedStatement, task);
            close(connection, task);
        }
    }

    /**
     * Retrieve the current coordinator's node ID.
     *
     * @return node ID of the current coordinator
     * @throws CoordinationException if an error is detected while retrieving the node ID of the coordinator
     */
    public String getCoordinatorNodeId() throws CoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String task = RdbmsCoordinationConstants.TASK_GET_COORDINATOR_INFORMATION;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(RdbmsCoordinationConstants.PS_GET_COORDINATOR_NODE_ID);
            resultSet = preparedStatement.executeQuery();
            String coordinatorNodeId;
            if (resultSet.next()) {
                coordinatorNodeId = resultSet.getString(1);
                if (logger.isDebugEnabled()) {
                    logger.debug("Coordinator node ID: " + coordinatorNodeId);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No coordinator present in the database");
                }
                coordinatorNodeId = null;
            }
            return coordinatorNodeId;
        } catch (SQLException e) {
            throw new CoordinationException("Error occurred while " + task, e);
        } finally {
            close(resultSet, task);
            close(preparedStatement, task);
            close(connection, task);
        }
    }

    /**
     * Remove current coordinator entry from database.
     *
     * @throws CoordinationException if an error is detected while removing the coordinator entry.
     */
    public void removeCoordinator() throws CoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String task = RdbmsCoordinationConstants.TASK_REMOVE_COORDINATOR;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(RdbmsCoordinationConstants.PS_DELETE_COORDINATOR);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            rollback(connection, task);
            throw new CoordinationException("Error occurred while " + task, e);
        } finally {
            close(preparedStatement, task);
            close(connection, task);
        }
    }

    /**
     * Check if the coordinator is invalid using the heart beat value.
     *
     * @param age maximum relative age with respect to the current time in milliseconds.
     * @return true if timed out, false otherwise
     * @throws CoordinationException if an error occurs checking if the coordinator is valid.
     */
    public boolean checkIfCoordinatorValid(int age) throws CoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String task = RdbmsCoordinationConstants.TASK_GET_COORDINATOR_INFORMATION;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(RdbmsCoordinationConstants.PS_GET_COORDINATOR_HEARTBEAT);
            resultSet = preparedStatement.executeQuery();
            long currentTimeMillis = System.currentTimeMillis();
            boolean isCoordinator;
            if (resultSet.next()) {
                long coordinatorHeartbeat = resultSet.getLong(1);
                long heartbeatAge = currentTimeMillis - coordinatorHeartbeat;
                isCoordinator = heartbeatAge <= age;
                if (logger.isDebugEnabled()) {
                    logger.debug("isCoordinator: " + isCoordinator + ", heartbeatAge: " + age
                            + ", coordinatorHeartBeat: " + coordinatorHeartbeat
                            + ", currentTime: " + currentTimeMillis);
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No coordinator present in database");
                }
                isCoordinator = false;
            }
            return isCoordinator;
        } catch (SQLException e) {
            throw new CoordinationException("Error occurred while " + task, e);
        } finally {
            close(resultSet, task);
            close(preparedStatement, task);
            close(connection, task);
        }
    }

    /**
     * Method to update the node heartbeat value to the current time.
     *
     * @param nodeId local node ID
     * @return true if the update is successful, false otherwise
     * @throws CoordinationException if an error is detected while updating the node heartbeat.
     */
    public boolean updateNodeHeartbeat(String nodeId) throws CoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatementForNodeUpdate = null;
        String task = RdbmsCoordinationConstants.TASK_UPDATE_NODE_HEARTBEAT;
        try {
            connection = getConnection();
            preparedStatementForNodeUpdate = connection.prepareStatement(
                    RdbmsCoordinationConstants.PS_UPDATE_NODE_HEARTBEAT);
            preparedStatementForNodeUpdate.setLong(1, System.currentTimeMillis());
            preparedStatementForNodeUpdate.setString(2, nodeId);
            int updateCount = preparedStatementForNodeUpdate.executeUpdate();
            connection.commit();
            return updateCount != 0;
        } catch (SQLException e) {
            rollback(connection, task);
            throw new CoordinationException("Error occurred while " + task + ". Node ID: " + nodeId, e);
        } finally {
            close(preparedStatementForNodeUpdate, task);
            close(connection, task);
        }
    }

    /**
     * Method to create the node heartbeat entry.
     *
     * @param nodeId local node ID
     * @throws CoordinationException if an error is detected while creating the node heartbeat entry.
     */
    public void createNodeHeartbeatEntry(String nodeId) throws CoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String task = RdbmsCoordinationConstants.TASK_ADD_NODE_ROW;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(RdbmsCoordinationConstants.PS_INSERT_NODE_HEARTBEAT_ROW);
            preparedStatement.setString(1, nodeId);
            preparedStatement.setLong(2, System.currentTimeMillis());
            preparedStatement.executeUpdate();
            connection.commit();

        } catch (SQLException e) {
            rollback(connection, task);
            throw new CoordinationException("Error occurred while " + task + ". Node ID: " + nodeId, e);
        } finally {
            close(preparedStatement, task);
            close(connection, task);
        }
    }

    /**
     * Update coordinator heartbeat value to current time.
     *
     * @param nodeId local node ID
     * @return true if the update is successful, false otherwise
     * @throws CoordinationException if an error is detected while updating the coordinator heartbeat.
     */
    public boolean updateCoordinatorHeartbeat(String nodeId) throws CoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatementForCoordinatorUpdate = null;
        String task = RdbmsCoordinationConstants.TASK_UPDATE_COORDINATOR_HEARTBEAT;
        try {
            connection = getConnection();
            preparedStatementForCoordinatorUpdate = connection.prepareStatement(
                    RdbmsCoordinationConstants.PS_UPDATE_COORDINATOR_HEARTBEAT);
            preparedStatementForCoordinatorUpdate.setLong(1, System.currentTimeMillis());
            preparedStatementForCoordinatorUpdate.setString(2, nodeId);
            int updateCount = preparedStatementForCoordinatorUpdate.executeUpdate();
            connection.commit();
            return updateCount != 0;
        } catch (SQLException e) {
            rollback(connection, task);
            throw new CoordinationException("Error occurred while " + task + ". instance ID: " + nodeId, e);
        } finally {
            close(preparedStatementForCoordinatorUpdate, task);
            close(connection, task);
        }
    }

    /**
     * Method to indicate that the coordinator detected a node addition.
     *
     * @param nodeId local node ID
     * @throws CoordinationException if an error is detected while marking the node as not new.
     */
    public void markNodeAsNotNew(String nodeId) throws CoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String task = RdbmsCoordinationConstants.TASK_MARK_NODE_NOT_NEW;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(RdbmsCoordinationConstants.PS_MARK_NODE_NOT_NEW);
            preparedStatement.setString(1, nodeId);
            int updateCount = preparedStatement.executeUpdate();
            if (updateCount == 0) {
                logger.warn("No record was updated while marking node as not new");
            }
            connection.commit();
        } catch (SQLException e) {
            rollback(connection, task);
            throw new CoordinationException("Error occurred while " + task, e);
        } finally {
            close(preparedStatement, task);
            close(connection, task);
        }
    }

    /**
     * Method to create coordinator entry (i.e - make this node the coordinator).
     *
     * @param nodeId local node ID
     * @return true if creation is successful, false otherwise
     * @throws CoordinationException if an error is detected while creating the coordinator entry
     */
    public boolean createCoordinatorEntry(String nodeId) throws CoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String task = RdbmsCoordinationConstants.TASK_ADD_COORDINATOR_ROW;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(RdbmsCoordinationConstants.PS_INSERT_COORDINATOR_ROW);
            preparedStatement.setInt(1, RdbmsCoordinationConstants.COORDINATOR_ANCHOR);
            preparedStatement.setString(2, nodeId);
            preparedStatement.setLong(3, System.currentTimeMillis());
            int updateCount = preparedStatement.executeUpdate();
            connection.commit();
            return updateCount != 0;
        } catch (SQLException e) {
            String errorMessage = task + " instance ID: " + nodeId;
            rollback(connection, task);
            if (isIntegrityConstraintViolationException(e)) {
                return false;
            } else {
                throw new CoordinationException("Error occurred while " + errorMessage, e);
            }
        } finally {
            close(preparedStatement, task);
            close(connection, task);
        }
    }

    /**
     * Check if the given node is the coordinator.
     *
     * @param nodeId local node ID
     * @return true if the given node is the coordinator, false otherwise
     * @throws CoordinationException if an error is detected while checking if coordinator
     */
    public boolean checkIsCoordinator(String nodeId) throws CoordinationException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String task = RdbmsCoordinationConstants.TASK_CHECK_IF_COORDINATOR;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(
                    RdbmsCoordinationConstants.PS_GET_COORDINATOR_ROW_FOR_NODE_ID);
            preparedStatement.setString(1, nodeId);
            resultSet = preparedStatement.executeQuery();
            boolean isCoordinator;
            isCoordinator = resultSet.next();
            return isCoordinator;
        } catch (SQLException e) {
            String errorMessage = task + " instance id: " + nodeId;
            throw new CoordinationException("Error occurred while " + errorMessage, e);
        } finally {
            close(resultSet, task);
            close(preparedStatement, task);
            close(connection, task);
        }
    }

    /**
     * Create a connection using a thread pooled data source object and return the connection.
     *
     * @return a connection to the datasource specified
     * @throws SQLException if an error occurs with database access
     */
    protected Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

    /**
     * Close the result set resource.
     *
     * @param resultSet the ResultSet
     * @param task      the task that was done by the closed result set
     */
    protected void close(ResultSet resultSet, String task) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.error("Closing result set failed after " + task, e);
            }
        }
    }

    /**
     * Close the prepared statement resource.
     *
     * @param preparedStatement the PreparedStatement
     * @param task              the task that was done by the closed prepared statement
     */
    protected void close(PreparedStatement preparedStatement, String task) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                logger.error("Closing prepared statement failed after " + task, e);
            }
        }
    }

    /**
     * Closes the provided connection, on failure logs the error.
     *
     * @param connection the Connection
     * @param task       the task that was done before closing
     */
    protected void close(Connection connection, String task) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Failed to close connection after " + task, e);
        }
    }

    /**
     * On database update failure try to rollback.
     *
     * @param connection the database connection
     * @param task       the task that was being done
     */
    protected void rollback(Connection connection, String task) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                logger.warn("Rollback failed on " + task, e);
            }
        }
    }

    /**
     * Convert Integer values to boolean. 0 is considered as boolean false, and all other values as true.
     *
     * @param value integer value
     * @return false if value is equal to 0, true otherwise
     */
    private boolean convertIntToBoolean(int value) {
        return value != 0;
    }

    /**
     * Private method to check if the SQL exception is an integrity constraint violation.
     *
     * @param sqlException the SQL exception thrown
     * @return true if an integrity constraint violation
     */
    private boolean isIntegrityConstraintViolationException(SQLException sqlException) {
        //Check by class for MySQL, else by state.
        return (SQLIntegrityConstraintViolationException.class.isInstance(sqlException)
                        || isIntegrityViolationSQLState(sqlException));
    }

    /**
     * Helper method to check if the state of the SQLException is that of an integrity violation exception.
     *
     * @param sqlException the SQL exception thrown
     * @return true if the state matches an integrity violation exception state
     */
    private boolean isIntegrityViolationSQLState(SQLException sqlException) {
        String sqlState = extractSqlState(sqlException);
        String sqlStateClassCode = determineSqlStateClassCode(sqlState);
        //TODO: read state class codes from config
        ArrayList<String> dataIntegrityViolationSQLStateClassCodes =
                new ArrayList<>(Arrays.asList("23", "27", "44"));
        return (dataIntegrityViolationSQLStateClassCodes.contains(sqlStateClassCode));
    }

    /**
     * Helper method to extract state of SQL exception.
     *
     * @param sqlException the SQL exception thrown
     * @return the SQL state of the exception
     */
    private String extractSqlState(SQLException sqlException) {
        String sqlState = sqlException.getSQLState();
        SQLException nextException = sqlException.getNextException();
        while (sqlState == null && nextException != null) {
            sqlState = nextException.getSQLState();
            nextException = nextException.getNextException();
        }
        return sqlState;
    }

    /**
     * Helper method to determine the class code of the SQL state.
     *
     * @param sqlState the SQL state of the exception
     * @return the class code
     */
    private String determineSqlStateClassCode(String sqlState) {
        if (sqlState == null || sqlState.length() < 2) {
            return sqlState;
        }
        return sqlState.substring(0, 2);
    }

}
