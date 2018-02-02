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
package org.wso2.broker.auth.authentication.authenticator.impl;

import org.wso2.broker.auth.BrokerAuthConfiguration;
import org.wso2.broker.auth.BrokerAuthConstants;
import org.wso2.broker.auth.BrokerAuthException;
import org.wso2.broker.auth.authentication.authenticator.Authenticator;
import org.wso2.broker.auth.authentication.sasl.plain.PlainSaslCallbackHandler;
import org.wso2.broker.auth.user.UserStoreManager;
import org.wso2.broker.common.StartupContext;

import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * An implementation of @{@link Authenticator} for provide authentication using
 * Jaas @{@link javax.security.auth.spi.LoginModule}.
 */
public class JaasAuthenticator implements Authenticator {

    @Override
    public void initialize(StartupContext startupContext,
                           BrokerAuthConfiguration.AuthenticationConfiguration authenticationConfiguration) {
        UserStoreManager userStoreManager = startupContext.getService(UserStoreManager.class);
        String jaasConfigPath = System.getProperty(BrokerAuthConstants.SYSTEM_PARAM_JAAS_CONFIG);
        BrokerAuthConfiguration.JaasConfiguration jaasConf = authenticationConfiguration.getJaas();
        if (jaasConfigPath == null || jaasConfigPath.trim().isEmpty()) {
            Configuration jaasConfig = createJaasConfig(jaasConf.getLoginModule(), userStoreManager,
                                                        jaasConf.getOptions());
            Configuration.setConfiguration(jaasConfig);
        }
    }

    @Override
    public boolean authenticate(String username, char[] credentials) throws BrokerAuthException {
        try {
            PlainSaslCallbackHandler plainCallbackHandler = new PlainSaslCallbackHandler();
            plainCallbackHandler.setUsername(username);
            plainCallbackHandler.setPassword(credentials);
            LoginContext loginContext = new LoginContext(BrokerAuthConstants.BROKER_SECURITY_CONFIG,
                                                         plainCallbackHandler);
            loginContext.login();
            return true;
        } catch (LoginException e) {
            throw new BrokerAuthException("Error while authenticating user with login module", e);
        }
    }

    /**
     * Creates Jaas config.
     *
     * @param loginModuleClassName jaas login module class name
     * @param userStoreManager user store manager use for authenticate users
     * @param options initial options for login module
     * @return login configuration
     */
    private static Configuration createJaasConfig(String loginModuleClassName,
                                           UserStoreManager userStoreManager,
                                           Map<String, Object> options) {
        options.put(BrokerAuthConstants.USER_STORE_MANAGER_PROPERTY, userStoreManager);
        AppConfigurationEntry[] entries = {
                new AppConfigurationEntry(loginModuleClassName,
                                          AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                          options)
        };
        return new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return entries;
            }
        };
    }
}
