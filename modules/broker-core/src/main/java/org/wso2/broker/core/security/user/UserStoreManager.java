/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.broker.core.security.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages the users and authentication
 */
public class UserStoreManager {

    private static final Logger log = LoggerFactory.getLogger(UserStoreManager.class);

    /**
     * Store the list of users
     */
    private static Map<String, User> users = new ConcurrentHashMap<>();

    public Map<String, User> getUsers() {
        return users;
    }

    public static void addUser(User user) {
        if (user != null && user.getUsername() != null) {
            users.put(user.getUsername(), user);
        } else {
            log.error("User or username can not be null");
        }
    }

    public static boolean authenticate(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword() != null && user.getPassword().equals(password);
    }
}
