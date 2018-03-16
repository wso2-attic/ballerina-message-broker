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
package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.impl;

import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.DbUtil;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.AuthResourceDao;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceType;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

/**
 * Test class for @{@link AuthResourceRdbmsDao}.
 */
public class AuthResourceRdbmsDaoTest {

    private DataSource dataSource;

    private AuthResourceDao authResourceDao;

    @BeforeClass
    public void beforeTest() throws AuthServerException {
        dataSource = DbUtil.getDataSource();
        authResourceDao = new AuthResourceRdbmsDao(dataSource);
        setup();
    }

    @AfterClass
    public void cleanup() throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("DELETE FROM MB_AUTH_RESOURCE");
        connection.commit();
        statement.close();
        connection.close();
    }

    public void setup() throws AuthServerException {
        AuthResource queue = new AuthResource(ResourceType.QUEUE.toString(), "queue1", true, "customer");
        authResourceDao.persist(queue);

        Map<String, Set<String>> actionsUserGroupsMap = new HashMap<>();
        actionsUserGroupsMap.put("bind", new HashSet<>(Arrays.asList("architect", "developer")));
        AuthResource exchange = new AuthResource(ResourceType.EXCHANGE.toString(), "exchange1", true, "manager",
                                                 actionsUserGroupsMap);

        actionsUserGroupsMap = new HashMap<>();
        actionsUserGroupsMap.put("bind", new HashSet<>(Collections.singletonList("architect")));
        AuthResource exchange2 = new AuthResource(ResourceType.EXCHANGE.toString(), "exchange2", true, "manager",
                                                  actionsUserGroupsMap);
        authResourceDao.persist(exchange);
        authResourceDao.persist(exchange2);
    }

    @Test(expectedExceptions = AuthServerException.class)
    public void testDuplicate() throws AuthServerException {
        AuthResource queue = new AuthResource(ResourceType.QUEUE.toString(), "queue1", true, "customer");
        authResourceDao.persist(queue);
    }

    @Test
    public void testRead() throws AuthServerException {
        AuthResource queue = authResourceDao.read(ResourceType.QUEUE.toString(), "queue1");
        Assert.assertEquals(queue.getOwner(), "customer");
        Assert.assertEquals(queue.getActionsUserGroupsMap().size(), 0);
        AuthResource exchange = authResourceDao.read(ResourceType.EXCHANGE.toString(), "exchange1");
        Assert.assertEquals(exchange.getOwner(), "manager");
        AuthResource exchange2 = authResourceDao.read(ResourceType.EXCHANGE.toString(), "exchange2");
        Assert.assertEquals(exchange.getOwner(), "manager");
        Assert.assertEquals(exchange2.getOwner(), "manager");
        Assert.assertEquals(exchange.getActionsUserGroupsMap().get("bind").size(), 2);
        Assert.assertEquals(exchange2.getActionsUserGroupsMap().get("bind").size(), 1);
    }

    @Test
    public void testReadAll() throws AuthServerException {

        List<AuthResource> customer = authResourceDao.readAll(ResourceType.QUEUE.toString(), "customer");
        List<AuthResource> customerExchange = authResourceDao.readAll(ResourceType.EXCHANGE.toString(), "customer");
        List<AuthResource> manager = authResourceDao.readAll(ResourceType.EXCHANGE.toString(), "manager");
        List<AuthResource> developer = authResourceDao.readAll(ResourceType.EXCHANGE.toString(), "developer");
        Assert.assertEquals(customer.size(), 1);
        Assert.assertEquals(customerExchange.size(), 0);
        Assert.assertEquals(manager.size(), 2);
        Assert.assertEquals(developer.size(), 0);

    }

    @Test
    public void testReadAllByGroups() throws AuthServerException {
        List<AuthResource> exchanges = authResourceDao.readAll(ResourceType.EXCHANGE.toString(), "bind",
                                                               "customer", Collections.singletonList("developer"));
        List<AuthResource> queues = authResourceDao.readAll(ResourceType.QUEUE.toString(), "bind",
                                                            "customer", Arrays.asList("architect", "designer"));
        Assert.assertEquals(exchanges.size(), 1);
        Assert.assertEquals(queues.size(), 1);
    }

    @Test
    public void testIsExists() throws AuthServerException {
        boolean queue1 = authResourceDao.isExists(ResourceType.QUEUE.toString(), "queue1");
        boolean queue11 = authResourceDao.isExists(ResourceType.QUEUE.toString(), "queue11");
        Assert.assertEquals(queue1, true);
        Assert.assertEquals(queue11, false);
    }

    @Test(dependsOnMethods = {"testIsExists", "testReadAllByGroups", "testReadAll", "testRead", "testDuplicate"})
    public void testUpdate() throws AuthServerException {
        Map<String, Set<String>> actionsUserGroupsMap = new HashMap<>();
        actionsUserGroupsMap.put("publish", new HashSet<>(Arrays.asList("architect", "customer", "manager")));
        AuthResource exchange = new AuthResource(ResourceType.EXCHANGE.toString(), "exchange1", true,
                                                 "developer", actionsUserGroupsMap);
        authResourceDao.update(exchange);

        List<AuthResource> manager = authResourceDao.readAll(ResourceType.EXCHANGE.toString(), "manager");
        List<AuthResource> developer = authResourceDao.readAll(ResourceType.EXCHANGE.toString(), "developer");
        Assert.assertEquals(manager.size(), 1);
        Assert.assertEquals(developer.size(), 1);
        AuthResource exchange1 = authResourceDao.read(ResourceType.EXCHANGE.toString(), "exchange1");
        Assert.assertEquals(exchange1.getOwner(), "developer");
        List<AuthResource> exchanges = authResourceDao.readAll(ResourceType.EXCHANGE.toString(), "publish",
                                                               "manager", Collections.singletonList("manager"));
        Assert.assertEquals(exchanges.size(), 2);
    }

    @Test(dependsOnMethods = "testUpdate")
    public void testDelete() throws AuthServerException {

        authResourceDao.delete(ResourceType.EXCHANGE.toString(), "exchange1");
        authResourceDao.delete(ResourceType.EXCHANGE.toString(), "exchange1");
        List<AuthResource> manager = authResourceDao.readAll(ResourceType.EXCHANGE.toString(), "manager");
        List<AuthResource> developer = authResourceDao.readAll(ResourceType.EXCHANGE.toString(), "developer");
        Assert.assertEquals(manager.size(), 1);
        Assert.assertEquals(developer.size(), 0);
        boolean exchange1 = authResourceDao.isExists(ResourceType.EXCHANGE.toString(), "exchange1");
        boolean exchange2 = authResourceDao.isExists(ResourceType.EXCHANGE.toString(), "exchange2");
        Assert.assertEquals(exchange1, false);
        Assert.assertEquals(exchange2, true);

    }
}
