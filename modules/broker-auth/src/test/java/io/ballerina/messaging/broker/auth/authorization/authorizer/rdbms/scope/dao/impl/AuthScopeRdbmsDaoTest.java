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
package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope.dao.impl;

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.DbUtil;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope.AuthScope;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope.dao.AuthScopeDao;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

/**
 * Test class for @{@link AuthScopeRdbmsDao}.
 */
public class AuthScopeRdbmsDaoTest {

    private AuthScopeDao authScopeDao;

    private DataSource dataSource;

    @BeforeClass
    public void beforeTest() throws AuthException, SQLException {
        dataSource = DbUtil.getDataSource();
        authScopeDao = new AuthScopeRdbmsDao(dataSource);
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('exchanges:test')");
        statement.execute("INSERT INTO MB_AUTH_SCOPE (SCOPE_NAME) VALUES ('queues:test')");
        statement.execute(" INSERT INTO MB_AUTH_SCOPE_MAPPING (SCOPE_ID, USER_GROUP_ID) SELECT SCOPE_ID,"
                                  + "'admin' FROM MB_AUTH_SCOPE WHERE SCOPE_NAME = 'exchanges:test'");
        statement.execute(" INSERT INTO MB_AUTH_SCOPE_MAPPING (SCOPE_ID, USER_GROUP_ID) SELECT SCOPE_ID,"
                                  + "'admin' FROM MB_AUTH_SCOPE WHERE SCOPE_NAME = 'queues:test'");
        connection.commit();
        statement.close();
        connection.close();
    }

    @AfterClass
    public void cleanup() throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("DELETE FROM MB_AUTH_SCOPE");
        connection.commit();
        statement.close();
        connection.close();
    }

    @Test
    public void testRead() throws AuthServerException {
        AuthScope authScope = authScopeDao.read("exchanges:test");
        Assert.assertEquals(authScope.getScopeName(), "exchanges:test");
        Assert.assertEquals(authScope.getUserGroups().size(), 1);
    }

    @Test
    public void testReadAll() throws AuthServerException {
        List<AuthScope> authScopes = authScopeDao.readAll();
        Assert.assertEquals(authScopes.size(), 13);
    }

    @Test
    public void testUpdate() throws AuthServerException {
        AuthScope authScope = authScopeDao.read("queues:test");
        Assert.assertEquals(authScope.getScopeName(), "queues:test");
        Assert.assertEquals(authScope.getUserGroups().size(), 1);
        authScopeDao.update("queues:test", Arrays.asList("developer", "manager"));
        authScope = authScopeDao.read("queues:test");
        Assert.assertEquals(authScope.getScopeName(), "queues:test");
        Assert.assertEquals(authScope.getUserGroups().size(), 2);
    }
}
