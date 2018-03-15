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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.authorizer.empty.NoOpAuthorizer;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.DefaultAuthorizer;
import io.ballerina.messaging.broker.auth.authorization.provider.DefaultMacHandler;
import io.ballerina.messaging.broker.auth.authorization.provider.RdbmsDacHandler;
import io.ballerina.messaging.broker.common.BrokerClassLoader;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerCommonConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Factory class for create new instance of @{@link Authorizer}.
 */
public class AuthorizerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizerFactory.class);

    /**
     * Provides an instance of @{@link Authorizer}
     *
     * @param commonConfiguration     common Configuration
     * @param brokerAuthConfiguration the auth configuration
     * @param startupContext          the startup context provides registered services for authenticator functionality.
     * @return authProvider for given configuration
     * @throws Exception throws if error occurred while providing new instance of authProvider
     */
    public static Authorizer getAuthorizer(BrokerCommonConfiguration commonConfiguration,
                                           BrokerAuthConfiguration brokerAuthConfiguration,
                                           UserStore userStore,
                                           StartupContext startupContext) throws Exception {

        if (brokerAuthConfiguration.getAuthentication().isEnabled()
                && brokerAuthConfiguration.getAuthorization().isEnabled()) {

            DiscretionaryAccessController dacHandler = getDac(brokerAuthConfiguration,
                                                              commonConfiguration,
                                                              startupContext,
                                                              userStore);

            MandatoryAccessController macHandler = getMandatoryAccessController(brokerAuthConfiguration,
                                                                                commonConfiguration,
                                                                                startupContext,
                                                                                userStore);
            Authorizer authorizer = new DefaultAuthorizer(dacHandler, macHandler, userStore);
            authorizer.initialize(startupContext);
            return authorizer;
        } else {
            return new NoOpAuthorizer();
        }
    }

    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION",
                        justification = "Not an issue since a RuntimeException is thrown")
    private static MandatoryAccessController getMandatoryAccessController(BrokerAuthConfiguration
                                                                                  brokerAuthConfiguration,
                                                                          BrokerCommonConfiguration commonConfiguration,
                                                                          StartupContext startupContext,
                                                                          UserStore userStore) {
        String macHandlerClassName = brokerAuthConfiguration.getAuthorization()
                                                            .getMandatoryAccessController()
                                                            .getClassName();

        if (commonConfiguration.getEnableInMemoryMode() && DefaultMacHandler.class.getCanonicalName()
                                                                                  .equals(macHandlerClassName)) {
            throw new RuntimeException("Cannot use " + macHandlerClassName + " with in-memory mode.");
        }

        LOGGER.info("Initializing Mandatory Access Controller {}", macHandlerClassName);

        try {
            MandatoryAccessController macHandler = BrokerClassLoader.loadClass(macHandlerClassName,
                                                                               MandatoryAccessController.class);
            Map<String, String> macProperties = brokerAuthConfiguration.getAuthorization()
                                                                       .getMandatoryAccessController()
                                                                       .getProperties();
            macHandler.initialize(startupContext, userStore, macProperties);
            return macHandler;
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize Mandatory Access Controller", e);
        }
    }

    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION",
                        justification = "Not an issue since a RuntimeException is thrown")
    private static DiscretionaryAccessController getDac(BrokerAuthConfiguration brokerAuthConfiguration,
                                                        BrokerCommonConfiguration commonConfiguration,
                                                        StartupContext startupContext,
                                                        UserStore userStore) {
        String dacHandlerClassName = brokerAuthConfiguration.getAuthorization()
                                                            .getDiscretionaryAccessController()
                                                            .getClassName();
        if (commonConfiguration.getEnableInMemoryMode() && RdbmsDacHandler.class.getCanonicalName()
                                                                                .equals(dacHandlerClassName)) {
            throw new RuntimeException("Cannot use " + dacHandlerClassName + " with in-memory mode.");
        }

        LOGGER.info("Initializing Discretionary Access Controller {}", dacHandlerClassName);

        try {
            DiscretionaryAccessController dacHandler = BrokerClassLoader.loadClass(dacHandlerClassName,
                                                                                   DiscretionaryAccessController.class);
            Map<String, String> dacProperties = brokerAuthConfiguration.getAuthorization()
                                                                       .getDiscretionaryAccessController()
                                                                       .getProperties();
            dacHandler.initialize(startupContext, userStore, dacProperties);
            return dacHandler;
        } catch (Exception e) {
            throw new RuntimeException("Cannot initialize Discretionary Access Controller", e);
        }
    }

    public static UserStore createUserStore(StartupContext startupContext,
                                            BrokerAuthConfiguration brokerAuthConfiguration) {
        String userStoreClassName = brokerAuthConfiguration.getAuthorization().getUserStore().getClassName();
        Map<String, String> properties = brokerAuthConfiguration.getAuthorization().getUserStore().getProperties();

        if (Objects.nonNull(userStoreClassName)) {
            try {
                UserStore userStore = BrokerClassLoader.loadClass(userStoreClassName, UserStore.class);
                userStore.initialize(startupContext, properties);
                return userStore;
            } catch (Exception e) {
                throw new RuntimeException("Cannot initialize user store", e);
            }
        } else {
            throw new RuntimeException("Please configure a user store for ");
        }

    }
}
