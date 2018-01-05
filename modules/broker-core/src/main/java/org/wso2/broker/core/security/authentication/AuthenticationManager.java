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
package org.wso2.broker.core.security.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.core.configuration.BrokerConfiguration;
import org.wso2.broker.core.security.authentication.sasl.BrokerSecurityProvider;
import org.wso2.broker.core.security.authentication.sasl.SaslServerBuilder;
import org.wso2.broker.core.security.authentication.sasl.plain.PlainSaslServerBuilder;
import org.wso2.broker.core.security.authentication.util.BrokerSecurityConstants;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/**
 * Class for manage authentication of message broker incoming connections
 * This has list of sasl servers registered by the message broker which will be used during authentication of incoming
 * connections.
 */
public class AuthenticationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationManager.class);
    /**
     * Map of SASL Server mechanisms
     */
    private Map<String, SaslServerBuilder> saslMechanisms = new HashMap<>();

    /**
     * Constructor for initialize authentication manager and register sasl servers for security provider mechanisms
     */
    public AuthenticationManager(BrokerConfiguration brokerConfiguration) throws Exception {
        String jaasConfigPath = System.getProperty(BrokerSecurityConstants.SYSTEM_PARAM_JAAS_CONFIG);
        if (jaasConfigPath == null || jaasConfigPath.trim().isEmpty()) {
            Configuration jaasConfig = createJaasConfig(brokerConfiguration.getAuthenticator().getLoginModule(),
                    brokerConfiguration.getAuthenticator().getOptions());
            Configuration.setConfiguration(jaasConfig);
        }
        registerSaslServers();
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
                .insertProviderAt(new BrokerSecurityProvider(BrokerSecurityConstants.PROVIDER_NAME, saslMechanisms), 1)
                == -1) {
            LOGGER.error("Unable to load AMQ security authentication providers.");
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("AMQ security authentication mechanisms providers successfully registered.");
            }
        }
    }

    /**
     * Creates Jaas config
     *
     * @param loginModuleClassName Jaas login module class name
     * @return Configuration
     */
    private static Configuration createJaasConfig(String loginModuleClassName, Map<String, String> options) {
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
     * Provides map of security mechanisms registered for broker
     *
     * @return Registered security Mechanisms
     */
    public Map<String, SaslServerBuilder> getSaslMechanisms() {
        return saslMechanisms;
    }
}
