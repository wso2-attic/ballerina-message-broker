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
package io.ballerina.messaging.broker.auth.authorization;

import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.authorizer.empty.NoOpAuthorizer;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerCommonConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for create new instance of @{@link Authorizer}.
 */
public class AuthorizerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizerFactory.class);

    /**
     * Provides an instance of @{@link Authorizer}
     *
     * @param commonConfiguration     common Configuration
     * @param startupContext          the startup context provides registered services for authenticator functionality.
     * @param brokerAuthConfiguration the auth configuration
     * @return authProvider for given configuration
     * @throws Exception throws if error occurred while providing new instance of authProvider
     */
    public Authorizer getAuthorizer(BrokerCommonConfiguration commonConfiguration,
                                    BrokerAuthConfiguration brokerAuthConfiguration,
                                    StartupContext startupContext) throws Exception {

        if (!commonConfiguration.getEnableInMemoryMode() &&
                brokerAuthConfiguration.getAuthentication().isEnabled() &&
                brokerAuthConfiguration.getAuthorization().isEnabled()) {

            String authorizerClassName = brokerAuthConfiguration.getAuthorization()
                                                               .getAuthorizer()
                                                               .getClassName();
            LOGGER.info("Initializing authProvider: {}", authorizerClassName);


            Authorizer authorizer = (Authorizer) ClassLoader.getSystemClassLoader()
                                                              .loadClass(authorizerClassName).newInstance();
            authorizer.initialize(startupContext,
                                  brokerAuthConfiguration.getAuthorization()
                                                        .getAuthorizer()
                                                        .getProperties());
            return authorizer;
        } else {
            return new NoOpAuthorizer();
        }
    }
}
