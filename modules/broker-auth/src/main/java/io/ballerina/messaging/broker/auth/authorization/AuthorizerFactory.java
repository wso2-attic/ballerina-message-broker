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
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.RdbmsAuthorizer;
import io.ballerina.messaging.broker.auth.authorization.provider.FileBasedUserStore;
import io.ballerina.messaging.broker.common.BrokerClassLoader;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerCommonConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

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
    @Deprecated
    public static Authorizer getAuthorizer(BrokerCommonConfiguration commonConfiguration,
                                    BrokerAuthConfiguration brokerAuthConfiguration,
                                    StartupContext startupContext) throws Exception {

        if (!commonConfiguration.getEnableInMemoryMode() && brokerAuthConfiguration.getAuthentication().isEnabled() &&
                brokerAuthConfiguration.getAuthorization()
                                                                                              .isEnabled()) {

            String authorizerClassName = RdbmsAuthorizer.class.getCanonicalName();

            if (commonConfiguration.getEnableInMemoryMode() && RdbmsAuthorizer.class.getCanonicalName()
                                                                                    .equals(authorizerClassName)) {
                throw new RuntimeException("Cannot use " + authorizerClassName + " in in-memory mode.");
            }

            LOGGER.info("Initializing authProvider: {}", authorizerClassName);

            Authorizer authorizer = BrokerClassLoader.loadClass(authorizerClassName, Authorizer.class);
            HashMap<String, String> properties = new HashMap<>();
            properties.put(RdbmsAuthorizer.USER_STORE_CLASS_PROPERTY_NAME, FileBasedUserStore.class.getCanonicalName());
            authorizer.initialize(startupContext, properties);
            return authorizer;
        } else {
            return new NoOpAuthorizer();
        }
    }
}
