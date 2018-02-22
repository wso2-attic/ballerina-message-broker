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
package io.ballerina.messaging.broker.auth.user;

import io.ballerina.messaging.broker.auth.user.config.UserConfig;
import io.ballerina.messaging.broker.auth.user.config.UsersFile;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Class for testing user file get and set operations
 */
public class UsersFileTest {

    private UsersFile usersFile;

    @Test(description = "Testing add users")
    public void testSetUsers() throws Exception {
        usersFile = new UsersFile();
        UserConfig user1 = new UserConfig();
        user1.setUsername("test1");
        user1.setPassword("testPassword1");
        user1.setRoles(Arrays.asList("role1", "role2"));

        UserConfig user2 = new UserConfig();
        user2.setUsername("test2");
        user2.setPassword("testPassword2");
        user2.setRoles(Arrays.asList("role1", "role3", "role4"));

        usersFile.setUserConfigs(Arrays.asList(user1, user2));
    }

    @Test(priority = 1,
          description = "Testing get users")
    public void testGetUsers() throws Exception {
        List<UserConfig> userList = usersFile.getUserConfigs();
        Assert.assertEquals(userList.size(), 2, "User count incorrect");
        UserConfig user1 = userList.get(0);
        UserConfig user2 = userList.get(1);
        Assert.assertEquals(user1.getUsername(), "test1", "Test1 username is incorrect");
        Assert.assertEquals(user1.getPassword(), "testPassword1", "Test1 password is incorrect");
        Assert.assertEquals(user1.getRoles().size(), 2, "Test1 role list count is incorrect");
        Assert.assertEquals(user2.getUsername(), "test2", "Test2 username is incorrect");
        Assert.assertEquals(user2.getPassword(), "testPassword2", "Test2 password is incorrect");
        Assert.assertEquals(user2.getRoles().size(), 3, "Test2 role list count is incorrect");

    }
}
