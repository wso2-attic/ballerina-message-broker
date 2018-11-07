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
package io.ballerina.messaging.broker.auth.authentication.jaas;

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.AuthInitException;
import io.ballerina.messaging.broker.auth.authentication.AuthResult;
import io.ballerina.messaging.broker.auth.user.FileBasedUserLoader;
import io.ballerina.messaging.broker.auth.user.dto.User;
import org.wso2.carbon.config.ConfigurationException;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User registry for the UserStoreLoginModule.
 */
public class FileBasedUserRegistry {

    private static Map<String, User> users = new ConcurrentHashMap<>();

    public FileBasedUserRegistry() throws AuthInitException {
        try {
            users.putAll(FileBasedUserLoader.loadUsers());
        } catch (ConfigurationException e) {
            throw new AuthInitException("Error initializing FileBasedUserRegistry", e);
        }
    }

    public AuthResult authenticate(String username, char... credentials) throws AuthException {
        if (Objects.isNull(username)) {
            throw new AuthException("Username cannot be null.");
        }
        User user = users.get(username);
        if (Objects.isNull(user)) {
            throw new AuthException("User not found for the given username.");
        }
        if (!Arrays.equals(credentials, user.getPassword())) {
            throw new AuthException("Password did not match with the configured user");
        }
        return new AuthResult(true, username);
    }
}
