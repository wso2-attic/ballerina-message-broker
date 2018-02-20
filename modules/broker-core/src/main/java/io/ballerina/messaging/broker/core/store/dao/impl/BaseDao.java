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

package io.ballerina.messaging.broker.core.store.dao.impl;

import io.ballerina.messaging.broker.common.util.function.ThrowingConsumer;
import io.ballerina.messaging.broker.common.util.function.ThrowingFunction;
import io.ballerina.messaging.broker.core.BrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import javax.sql.DataSource;

/**
 * Implements methods to do boilerplate JDBC tasks.
 */
abstract class BaseDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDao.class);

    private final DataSource dataSource;

    BaseDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.error("Error closing connection.", e);
            }
        }
    }

    void close(Connection connection, Statement statement) {
        close(statement);
        close(connection);
    }

    void close(Connection connection, Statement statement, ResultSet resultSet) {
        close(resultSet);
        close(connection, statement);
    }

    void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.error("Error closing statement. " + statement, e);
            }
        }
    }

    void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.error("Error closing result set.", e);
            }
        }
    }

    void rollback(Connection connection, String message) {
        try {
            if (Objects.nonNull(connection)) {
                connection.rollback();
            }
        } catch (SQLException e) {
            LOGGER.error("Error occurred while rolling back. Failed operation " + message, e);
        }
    }

    void transaction(ThrowingConsumer<Connection, Exception> command,
                     String message) throws BrokerException {

        Connection connection = null;
        try {
            connection = getConnection();
            command.accept(connection);
            connection.commit();
        } catch (Exception e) {
            rollback(connection, message);
            throw new BrokerException("Error occurred while " + message, e);
        } finally {
            close(connection);
        }
    }

    <R> R selectOperation(ThrowingFunction<Connection, R, Exception> command,
                          String message) throws BrokerException {

        Connection connection = null;
        try {
            connection = getConnection();
            return command.apply(connection);
        } catch (Exception e) {
            rollback(connection, message);
            throw new BrokerException("Error occurred while " + message, e);
        } finally {
            close(connection);
        }
    }
}
