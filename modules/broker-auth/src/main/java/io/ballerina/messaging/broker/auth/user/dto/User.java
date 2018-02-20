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
package io.ballerina.messaging.broker.auth.user.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Class for represent user for file based user manager.
 */
public class User {

    private String username;

    private char[] password;

    private Set<String> roles;

    public User(String username, char[] password, Set<String> roles) {
        this.username = username;
        this.password = Arrays.copyOf(password, password.length);
        this.roles = Collections.unmodifiableSet(roles);
    }

    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return Arrays.copyOf(password, password.length);
    }

    public Set<String> getRoles() {
        return roles;
    }
}
