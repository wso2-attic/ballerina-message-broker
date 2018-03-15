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

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
import io.ballerina.messaging.broker.auth.authentication.AuthResult;
import io.ballerina.messaging.broker.auth.authentication.Authenticator;
import io.ballerina.messaging.broker.auth.authentication.jaas.PlainSaslCallbackHandler;
import io.ballerina.messaging.broker.auth.authentication.jaas.UserStoreLoginModule;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.common.StartupContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * An implementation of @{@link Authenticator} for provide authentication using
 * Jaas @{@link javax.security.auth.spi.LoginModule}.
 */
public class JaasAuthenticator implements Authenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JaasAuthenticator.class);

    @Override
    public void initialize(StartupContext startupContext,
                           UserStore userStore,
                           Map<String, Object> properties) throws Exception {

        String jaasConfigPath = System.getProperty(BrokerAuthConstants.SYSTEM_PARAM_JAAS_CONFIG);
        if (jaasConfigPath == null || jaasConfigPath.trim().isEmpty()) {
            Object jaasLoginModule = properties.get(BrokerAuthConstants.CONFIG_PROPERTY_JAAS_LOGIN_MODULE);
            if (Objects.nonNull(jaasLoginModule)) {
                // Add user store for default login module
                if (jaasLoginModule.toString().equals(UserStoreLoginModule.class.getCanonicalName())) {
                    properties.put(BrokerAuthConstants.PROPERTY_USER_STORE_CONNECTOR,
                                   userStore);
                }
                Configuration jaasConfig = createJaasConfig(jaasLoginModule.toString(), properties);
                Configuration.setConfiguration(jaasConfig);
            } else {
                throw new AuthException("Jass login module have not been set.");
            }
        }
    }

    @Override
    public AuthResult authenticate(String username, char[] password) throws AuthException {
        LoginContext loginContext = null;
        try {
            PlainSaslCallbackHandler plainCallbackHandler = new PlainSaslCallbackHandler();
            plainCallbackHandler.setUsername(username);
            plainCallbackHandler.setPassword(password);
            loginContext = new LoginContext(BrokerAuthConstants.BROKER_SECURITY_CONFIG,
                                            plainCallbackHandler);
            loginContext.login();
            String userId = username;
            Set<Principal> principals;
            if (Objects.nonNull(loginContext.getSubject()) &&
                    Objects.nonNull(principals = loginContext.getSubject().getPrincipals()) &&
                    !principals.isEmpty()) {
                Principal principal = principals.iterator().next();
                if (Objects.nonNull(principal)) {
                    userId = principal.getName();
                }
            }
            return new AuthResult(true, userId);
        } catch (LoginException e) {
            throw new AuthException("Error while authenticating user with login module", e);
        } finally {
            if (Objects.nonNull(loginContext)) {
                try {
                    loginContext.logout();
                } catch (LoginException e) {
                    LOGGER.error("Error while logout from login module", e);
                }
            }
        }
    }

    /**
     * Creates Jaas config.
     *
     * @param loginModuleClassName jaas login module class name
     * @param options              initial options for login module
     * @return login configuration
     */
    private static Configuration createJaasConfig(String loginModuleClassName,
                                                  Map<String, Object> options) {
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
