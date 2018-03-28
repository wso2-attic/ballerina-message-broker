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

import io.ballerina.messaging.broker.common.BaseDao;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Exchange;
import io.ballerina.messaging.broker.core.store.dao.ExchangeDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

/**
 * Implements functionality required to manipulate exchanges in the storage.
 */
class ExchangeDaoImpl extends BaseDao implements ExchangeDao {

    ExchangeDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void persist(Exchange exchange) throws BrokerException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_INSERT_EXCHANGE);
            statement.setString(1, exchange.getName());
            statement.setString(2, exchange.getType().toString());
            statement.executeUpdate();
            connection.commit();

        } catch (SQLException e) {
            String message = "Error occurred while storing exchange " + exchange;
            rollback(connection, message);
            throw new BrokerException(message, e);
        } finally {
            close(connection, statement);
        }
    }

    @Override
    public void delete(Exchange exchange) throws BrokerException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_DELETE_EXCHANGE);
            statement.setString(1, exchange.getName());
            statement.setString(2, exchange.getType().toString());
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            String message = "Error occurred while deleting exchange " + exchange;
            rollback(connection, message);
            throw new BrokerException("Error occurred while deleting exchange " + exchange, e);
        } finally {
            close(connection, statement);
        }
    }

    @Override
    public void retrieveAll(ExchangeCollector exchangeCollector) throws BrokerException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(RDBMSConstants.SELECT_ALL_EXCHANGES);
            while (resultSet.next()) {
                String name = resultSet.getString(1);
                String typeString = resultSet.getString(2);

                exchangeCollector.addExchange(name, typeString);
            }
        } catch (SQLException e) {
            String message = "Error occurred while retrieving exchanges";
            rollback(connection, message);
            throw new BrokerException(message, e);
        } finally {
            close(connection, statement, resultSet);
        }
    }

}
