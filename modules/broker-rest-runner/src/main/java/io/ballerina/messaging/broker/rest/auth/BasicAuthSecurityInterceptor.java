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
package io.ballerina.messaging.broker.rest.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.security.basic.AbstractBasicAuthSecurityInterceptor;

/**
 * Class implements @{@link AbstractBasicAuthSecurityInterceptor} to authenticate requests with basic authentication.
 */
public class BasicAuthSecurityInterceptor extends AbstractBasicAuthSecurityInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthSecurityInterceptor.class);

    private AuthenticateFunction authenticateFunction;

    public BasicAuthSecurityInterceptor(AuthenticateFunction authenticateFunction) {
        this.authenticateFunction = authenticateFunction;
    }

    @Override
    protected boolean authenticate(String userName, String password) {
        try {
            return userName != null
                    && password != null
                    && authenticateFunction.authenticate(userName, password.toCharArray());
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error occurred while authenticating user", e);
            }
            return false;
        }
    }

    /**
     * Function to authenticate user on given username and credentials.
     *
     * @param <E> the type of the exception thrown
     */
    @FunctionalInterface
    public interface AuthenticateFunction<E extends Exception> {
        /**
         * Authenticate based on given username and credentials.
         *
         * @param username    username
         * @param credentials user credentials
         * @throws E if unable to authenticate the user
         */
        boolean authenticate(String username, char... credentials) throws E;
    }
}
