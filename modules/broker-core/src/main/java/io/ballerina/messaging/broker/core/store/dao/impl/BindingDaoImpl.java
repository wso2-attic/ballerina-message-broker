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
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.core.Binding;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.store.dao.BindingDao;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Implements functionality required to manipulate bindings in the storage.
 */
class BindingDaoImpl extends BaseDao implements BindingDao {

    BindingDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void persist(String exchangeName, Binding binding) throws BrokerException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_INSERT_BINDING);
            statement.setString(1, exchangeName);
            statement.setString(2, binding.getQueue().getName());
            statement.setString(3, binding.getBindingPattern());
            FieldTable arguments = binding.getArguments();
            byte[] bytes = new byte[(int) arguments.getSize()];
            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            byteBuf.resetWriterIndex();
            arguments.write(byteBuf);
            statement.setBytes(4, bytes);

            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            String message = "Error occurred while storing binding " + binding;
            rollback(connection, message);
            throw new BrokerException(message, e);
        } finally {
            close(connection, statement);
        }
    }

    @Override
    public void delete(String queueName, String routingKey, String exchangeName) throws BrokerException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_DELETE_BINDING);
            statement.setString(1, exchangeName);
            statement.setString(2, queueName);
            statement.setString(3, routingKey);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            String message = "Error occurred while deleting the binding for queue " + queueName + " routing key "
                    + routingKey;
            rollback(connection, message);
            throw new BrokerException(message, e);
        } finally {
            close(connection, statement);
        }
    }

    @Override
    public void retrieveBindingsForExchange(String exchangeName,
                                            BindingCollector bindingCollector) throws BrokerException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_SELECT_BINDINGS_FOR_EXCHANGE);
            statement.setString(1, exchangeName);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String queueName = resultSet.getString(1);
                String routingKey = resultSet.getString(2);
                byte[] arguments = resultSet.getBytes(3);
                FieldTable fieldTable = FieldTable.parse(Unpooled.wrappedBuffer(arguments));
                bindingCollector.addBinding(queueName, routingKey, fieldTable);
            }

        } catch (Exception e) {
            String message = "Error occurred while retrieving bindings";
            rollback(connection, message);
            throw new BrokerException(message, e);
        } finally {
            close(connection, statement, resultSet);
        }
    }
}
