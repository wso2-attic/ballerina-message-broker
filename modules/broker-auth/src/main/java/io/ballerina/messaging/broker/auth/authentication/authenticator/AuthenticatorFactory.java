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

import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authentication.authenticator.impl.DefaultAuthenticator;
import io.ballerina.messaging.broker.auth.authentication.authenticator.impl.JaasAuthenticator;
import io.ballerina.messaging.broker.common.StartupContext;

/**
 * Factory class for retrieve an instance of specified implementation of {@link Authenticator}.
 */
public class AuthenticatorFactory {

    /**
     * Provides an instance of @{@link Authenticator}
     *
     * @param startupContext the startup context provides registered services for authenticator functionality.
     * @param authenticationConfiguration the authentication configuration
     * @return authenticator for given configuration
     * @throws Exception throws if error occurred while providing new instance of authenticator
     */
    public Authenticator getAuthenticator(StartupContext startupContext,
                                          BrokerAuthConfiguration.AuthenticationConfiguration
                                                  authenticationConfiguration) throws Exception {
        Authenticator authenticator;
        if (authenticationConfiguration.isEnabled()) {
            authenticator = new JaasAuthenticator();
        } else {
            authenticator = new DefaultAuthenticator();
        }
        authenticator.initialize(startupContext);
        return authenticator;
    }
}
