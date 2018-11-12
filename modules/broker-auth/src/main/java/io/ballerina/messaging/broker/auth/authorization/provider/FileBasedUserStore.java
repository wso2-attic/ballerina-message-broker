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
package io.ballerina.messaging.broker.auth.authorization.provider;

import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.auth.user.FileBasedUserLoader;
import io.ballerina.messaging.broker.auth.user.dto.User;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements @{@link UserStore} to connect to file based user store.
 */
public class FileBasedUserStore implements UserStore {

    private static Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public void initialize(StartupContext startupContext, Map<String, String> properties) throws Exception {

        users.putAll(FileBasedUserLoader.loadUsers());
    }

    @Override
    public boolean isUserExists(String username) {
        return Objects.nonNull(users.get(username));
    }

    /**
     * Retrieve the set of users for given username.
     *
     * @param userName user name
     * @return List of roles
     */
    @Override
    public Set<String> getUserGroupsList(String userName) {
        User user = users.get(userName);
        if (user != null) {
            return user.getRoles();
        }
        return Collections.emptySet();
    }
}
