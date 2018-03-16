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
package io.ballerina.messaging.broker.auth.authentication.sasl.plain.jaas;

import io.ballerina.messaging.broker.auth.AuthException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
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
 * Login module for unit tests.
 */
public class TestLoginModule implements LoginModule {
    private CallbackHandler callbackHandler;

    private Map<String, char[]> usersMap = new HashMap<>();

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                           Map<String, ?> options) {
        this.callbackHandler = callbackHandler;
        usersMap.put("user", new char[] { 'p', 'a', 's', 's' });
        usersMap.put("u#@a.com", new char[] { 'P', '1', '@', '$', '&', '#' });
    }

    @Override
    public boolean login() throws LoginException {
        NameCallback userNameCallback = new NameCallback("userName");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        Callback[] callbacks = { userNameCallback, passwordCallback };
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException | UnsupportedCallbackException e) {
            throw new AuthException("Error while handling callback ", e);
        }
        String userName = userNameCallback.getName();
        char[] password = passwordCallback.getPassword();
        return Arrays.equals(password, usersMap.get(userName));
    }

    @Override
    public boolean commit() throws LoginException {
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        return false;
    }
}
