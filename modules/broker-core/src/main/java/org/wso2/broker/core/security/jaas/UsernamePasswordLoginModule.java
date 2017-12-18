/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.broker.core.security.jaas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.core.security.exception.BrokerLoginException;
import org.wso2.broker.core.security.user.UserStoreManager;

import java.io.IOException;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * Default JaaS login module {@link LoginModule} for Message broker.
 * This will be configured in jaas.conf file.
    AuthConfig {
        org.wso2.broker.core.security.jaas.UsernamePasswordLoginModule required;
    };
 */
public class UsernamePasswordLoginModule implements LoginModule {

    private static final Logger log = LoggerFactory.getLogger(UsernamePasswordLoginModule.class);
    private String username;
    private char[] password;
    private boolean success = false;
    private CallbackHandler callbackHandler;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        NameCallback usernameCallback = new NameCallback("username");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        Callback[] callbacks = { usernameCallback, passwordCallback };
        try {
            callbackHandler.handle(callbacks);
        } catch (UnsupportedCallbackException e) {
            throw new BrokerLoginException("Callback type does not support. ", e);
        } catch (IOException e) {
            throw new BrokerLoginException("Exception occurred while handling authentication data. ", e);
        }
        username = usernameCallback.getName();
        password = passwordCallback.getPassword();
        success = validateUserPassword(username, password);
        return success;
    }

    /**
     * Authenticate user credentials using userstore manager
     * @param username Username
     * @param password Password
     * @return Denotes the whether user authentication success ot not
     */
    private boolean validateUserPassword(String username, char[] password) {
        return username != null && password != null && UserStoreManager
                .authenticate(username, String.valueOf(password));
    }

    @Override
    public boolean commit() throws LoginException {
        username = null;
        for (int i = 0; i < password.length; i++) {
            password[i] = ' ';
        }
        password = null;
        return success;
    }

    @Override
    public boolean abort() throws LoginException {
        logout();
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        success = false;
        username = null;
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = ' ';
            }
            password = null;
        }
        return true;
    }
}
