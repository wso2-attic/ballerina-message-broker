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

import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.common.BrokerClassLoader;
import io.ballerina.messaging.broker.common.StartupContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for retrieve an instance of specified implementation of {@link Authenticator}.
 */
public class AuthenticatorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatorFactory.class);

    /**
     * Provides an instance of @{@link Authenticator}
     *
     * @param startupContext              the startup context provides registered services for authenticator
     *                                    functionality.
     * @param authenticationConfiguration the authentication configuration
     * @param userStore                   user store
     * @return authenticator for given configuration
     * @throws Exception throws if error occurred while providing new instance of authenticator
     */
    public Authenticator getAuthenticator(StartupContext startupContext,
                                          BrokerAuthConfiguration.AuthenticationConfiguration
                                                  authenticationConfiguration,
                                          UserStore userStore) throws Exception {
        Authenticator authenticator;
        String authenticatorClass = authenticationConfiguration.getAuthenticator().getClassName();
        LOGGER.info("Initializing authenticator: {}", authenticatorClass);
        authenticator = BrokerClassLoader.loadClass(authenticatorClass, Authenticator.class);
        authenticator.initialize(startupContext,
                                 userStore,
                                 authenticationConfiguration.getAuthenticator().getProperties());
        return authenticator;
    }
}
