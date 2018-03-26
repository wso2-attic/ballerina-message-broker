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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.common;

import io.ballerina.messaging.broker.common.util.function.ThrowingConsumer;
import io.ballerina.messaging.broker.common.util.function.ThrowingFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import javax.sql.DataSource;

/**
 * Defines common base functionality required at persistence layer.
 */
public abstract class BaseDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDao.class);
    public static final Object DUMMY_RETURN_OBJECT = new Object();

    private final DataSource dataSource;

    public BaseDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    protected void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.error("Error closing connection.", e);
            }
        }
    }

    protected void close(Connection connection, Statement statement) {
        close(statement);
        close(connection);
    }

    public void close(Connection connection, Statement statement, ResultSet resultSet) {
        close(resultSet);
        close(connection, statement);
    }

    protected void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.error("Error closing statement. " + statement, e);
            }
        }
    }

    protected void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.error("Error closing result set.", e);
            }
        }
    }

    protected String getSQLFormattedIdList(int listLength) {
        StringBuilder paramList = new StringBuilder();
        paramList.append("?");

        for (int i = 1; i < listLength; i++) {
            paramList.append(",?");
        }
        return paramList.toString();
    }

    protected void rollback(Connection connection, String message) {
        try {
            if (Objects.nonNull(connection)) {
                connection.rollback();
            }
        } catch (SQLException e) {
            LOGGER.error("Error occurred while rolling back. Failed operation " + message, e);
        }
    }
    public <E extends Exception> void transaction(ThrowingConsumer<Connection, E> command) throws DaoException {

        Connection connection = null;
        try {
            connection = getConnection();
            command.accept(connection);
            connection.commit();
        } catch (Exception e) {
            String message = "transaction operation";
            rollback(connection, message);
            throw new DaoException("Error occurred while " + message, e);
        } finally {
            close(connection);
        }
    }

    public <R, E extends Exception> R transaction(ThrowingFunction<Connection, R,  E> command) throws DaoException {

        Connection connection = null;
        try {
            R response;
            connection = getConnection();
            response = command.apply(connection);
            connection.commit();
            return response;
        } catch (Exception e) {
            String message = "transaction operation";
            rollback(connection, message);
            throw new DaoException("Error occurred while " + message, e);
        } finally {
            close(connection);
        }
    }

    public <R, E extends Exception> R selectAndGetOperation(ThrowingFunction<Connection, R, E> command)
            throws DaoException {

        Connection connection = null;
        try {
            connection = getConnection();
            return command.apply(connection);
        } catch (Exception e) {
            String message = "select operation";
            rollback(connection, message);
            throw new DaoException("Error occurred while " + message, e);
        } finally {
            close(connection);
        }
    }

    public <E extends Exception> void selectOperation(ThrowingConsumer<Connection, E> command) throws DaoException {
        selectAndGetOperation((ThrowingFunction<Connection, Object, Exception>) connection -> {
            command.accept(connection);
            return DUMMY_RETURN_OBJECT;
        });
    }
}
