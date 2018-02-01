/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.broker.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.auth.authentication.sasl.BrokerSecurityProvider;
import org.wso2.broker.auth.authentication.sasl.SaslServerBuilder;
import org.wso2.broker.auth.authentication.sasl.plain.PlainSaslServerBuilder;
import org.wso2.broker.auth.user.UserStoreManager;
import org.wso2.broker.common.BrokerConfigProvider;
import org.wso2.broker.common.StartupContext;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

/**
 * Class to manage authentication and authorization of message broker incoming connections.
 * This has a list of sasl servers registered by the message broker which will be used during authentication of incoming
 * connections.
 */
public class AuthManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthManager.class);
    /**
     * Map of SASL Server mechanisms
     */
    private Map<String, SaslServerBuilder> saslMechanisms = new HashMap<>();

    private BrokerAuthConfiguration brokerAuthConfiguration;

    private UserStoreManager userStoreManager;

    public AuthManager(StartupContext startupContext)  throws Exception {
        BrokerConfigProvider configProvider = startupContext.getService(BrokerConfigProvider.class);
        brokerAuthConfiguration = configProvider
                .getConfigurationObject(BrokerAuthConfiguration.NAMESPACE, BrokerAuthConfiguration.class);
        startupContext.registerService(AuthManager.class, this);
        userStoreManager = startupContext.getService(UserStoreManager.class);
    }

    public void start() {
        if (brokerAuthConfiguration.getAuthentication().isEnabled()) {
            String jaasConfigPath = System.getProperty(BrokerAuthConstants.SYSTEM_PARAM_JAAS_CONFIG);
            BrokerAuthConfiguration.JaasConfiguration jaasConf = brokerAuthConfiguration.getAuthentication().getJaas();
            if (jaasConfigPath == null || jaasConfigPath.trim().isEmpty()) {
                Configuration jaasConfig = createJaasConfig(jaasConf.getLoginModule(), userStoreManager,
                                                            jaasConf.getOptions());
                Configuration.setConfiguration(jaasConfig);
            }
            registerSaslServers();
        }
    }

    public void stop() {
        LOGGER.info("Broker auth manager service stopped.");
    }

    /**
     * Register security provider mechanisms
     */
    private void registerSaslServers() {
        // create PLAIN SaslServer builder
        PlainSaslServerBuilder plainSaslServerBuilder = new PlainSaslServerBuilder();
        saslMechanisms.put(plainSaslServerBuilder.getMechanismName(), plainSaslServerBuilder);
        // Register given Sasl Server factories
        if (Security
                .insertProviderAt(new BrokerSecurityProvider(BrokerAuthConstants.PROVIDER_NAME, saslMechanisms), 1)
                == -1) {
            LOGGER.info("AMQ security authentication providers are already installed.");
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("AMQ security authentication mechanisms providers are successfully registered.");
            }
        }
    }

    /**
     * Creates Jaas config
     *
     * @param loginModuleClassName Jaas login module class name
     * @return Configuration
     */
    private static Configuration createJaasConfig(String loginModuleClassName, UserStoreManager userStoreManager,
                                                  Map<String, Object> options
                                                 ) {
        options.put(BrokerAuthConstants.USER_STORE_MANAGER_PROPERTY, userStoreManager);
        AppConfigurationEntry[] entries = {
                new AppConfigurationEntry(loginModuleClassName, AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                          options)
        };
        return new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return entries;
            }
        };
    }

    /**
     * Create sasl server for given mechanism
     *
     * @param hostName  Hostname of the server
     * @param mechanism Sasl mechanism
     * @return Sasl server created for mechanism
     * @throws SaslException Throws if server does not support for given mechanism
     */
    public SaslServer createSaslServer(String hostName, String mechanism) throws SaslException {
        SaslServerBuilder saslServerBuilder = saslMechanisms.get(mechanism);
        if (saslServerBuilder != null) {
            SaslServer saslServer = Sasl.createSaslServer(mechanism, BrokerAuthConstants.AMQP_PROTOCOL_IDENTIFIER,
                                                          hostName,
                                                          saslServerBuilder.getProperties(),
                                                          saslServerBuilder.getCallbackHandler());
            if (saslServer != null) {
                return saslServer;
            } else {
                throw new SaslException("Sasl server cannot be found for mechanism: " + mechanism);
            }
        } else {
            throw new SaslException("Server does not support for mechanism: " + mechanism);
        }
    }

    /**
     * Provides broker authentication enabled.
     * @return broker authentication enabled or not
     */
    public boolean isAuthenticationEnabled() {
        return brokerAuthConfiguration.getAuthentication().isEnabled();
    }
}
