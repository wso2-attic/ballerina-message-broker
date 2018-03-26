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
package io.ballerina.messaging.broker.auth.authentication;

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.Map;

/**
 * Interface represents the authenticator.
 * This provides ability to extend different authentication strategies for broker auth.
 * <p>
 * {@link #authenticate(String, char[])} is used to authenticate user on given username and credentials.
 */
public interface Authenticator {

    /**
     * Initiate authenticator with startup context.
     *
     * @param startupContext the startup context provides registered services for authenticator functionality.
     * @param userStore      {@link UserStore} to get the user information
     * @param properties     set of properties
     */
    void initialize(StartupContext startupContext,
                    UserStore userStore,
                    Map<String, Object> properties) throws Exception;

    /**
     * Authenticate given user based on defined authentication strategy.
     *
     * @param username an username
     * @param password the password of the user
     * @return authentication result with user information
     * @throws AuthException if error occurred while authenticating user.
     */
    AuthResult authenticate(String username, char[] password) throws AuthException;
}
