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
package io.ballerina.messaging.broker.auth.authentication.authenticator;

import io.ballerina.messaging.broker.auth.BrokerAuthException;
import io.ballerina.messaging.broker.common.StartupContext;

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
     */
    void initialize(StartupContext startupContext) throws Exception;
    /**
     * Authenticate given user based on defined authentication strategy.
     *
     * @param username    an username
     * @param credentials the credentials of the user
     * @return authentication result with user information
     * @throws BrokerAuthException if error occurred while authenticating user.
     */
    boolean authenticate(String username, char[] credentials) throws BrokerAuthException;
}
