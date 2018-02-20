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

import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.DbUtil;
import io.ballerina.messaging.broker.core.Exchange;
import io.ballerina.messaging.broker.core.ExchangeRegistry;
import io.ballerina.messaging.broker.core.store.dao.BindingDao;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;

public class ExchangeDaoImplTest {

    private ExchangeDaoImpl exchangeDao;

    private BindingDao bindingDao;

    private DataSource dataSource;

    @BeforeTest
    public void beforeTest() {
        dataSource = DbUtil.getDataSource();
        exchangeDao = new ExchangeDaoImpl(dataSource);
        bindingDao = new BindingDaoImpl(dataSource);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Object[][] exchanges = exchangeData();
        Connection connection = dataSource.getConnection();
        PreparedStatement prepareStatement = connection.prepareStatement("DELETE FROM MB_EXCHANGE WHERE "
                                                                                  + "EXCHANGE_NAME=?");
        for (Object[] exchange : exchanges) {
            String exchangeName = (String) exchange[0];
            prepareStatement.setString(1, exchangeName);
            prepareStatement.addBatch();
        }

        prepareStatement.executeBatch();
        connection.commit();
        connection.close();
    }

    @Test(dataProvider = "exchangeData", description = "Test exchange persistence")
    public void testPersistAndDelete(String name, String type) throws Exception {
        Exchange exchange = ExchangeRegistry.ExchangeFactory.newInstance(name, Exchange.Type.from(type), bindingDao);
        exchangeDao.persist(exchange);

        Connection connection = dataSource.getConnection();
        String selectSql = "SELECT * FROM MB_EXCHANGE WHERE EXCHANGE_NAME=? AND EXCHANGE_TYPE=?";
        PreparedStatement statement = connection.prepareStatement(selectSql);

        statement.setString(1, name);
        statement.setString(2, type);

        ResultSet resultSet = statement.executeQuery();
        Assert.assertTrue(resultSet.next(), "Exchange should be persisted");

        resultSet.close();
        statement.close();

        exchangeDao.delete(exchange);
        statement = connection.prepareStatement(selectSql);
        statement.setString(1, name);
        statement.setString(2, type);
        resultSet = statement.executeQuery();
        Assert.assertFalse(resultSet.next(), "Exchange should be deleted from database");
        resultSet.close();
        statement.close();
        connection.close();
    }

    @Test(dataProvider = "exchangeData", description = "Test duplicate exchange persistence"
            , expectedExceptions = BrokerException.class)
    public void testDuplicatePersistence(String name, String type) throws Exception {
        Exchange exchange = ExchangeRegistry.ExchangeFactory.newInstance(name, Exchange.Type.from(type), bindingDao);
        exchangeDao.persist(exchange);
        // Try to add an already existing exchange
        exchangeDao.persist(exchange);
    }

    @DataProvider(name = "exchangeData")
    public Object[][] exchangeData() {
        return new Object[][] {
                { "myExchange", "topic" },
                { "testExchange", "direct" },
        };
    }
}
