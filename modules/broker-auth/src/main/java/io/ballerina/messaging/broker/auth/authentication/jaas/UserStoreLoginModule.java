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
package io.ballerina.messaging.broker.auth.authentication.jaas;

import com.sun.security.auth.UserPrincipal;
import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
import io.ballerina.messaging.broker.auth.authentication.AuthResult;
import io.ballerina.messaging.broker.auth.authorization.UserStore;

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
 * AuthConfig {
 * {@link UserStoreLoginModule} required;
 * };
 */
public class UserStoreLoginModule implements LoginModule {

    private String userName;
    private String authenticationId;
    private char[] password;
    private boolean success = false;
    private UserPrincipal userPrincipal;
    private Subject subject;
    private CallbackHandler callbackHandler;
    private UserStore userStore;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                           Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.userStore = (UserStore) options.get(BrokerAuthConstants.PROPERTY_USER_STORE_CONNECTOR);
    }

    @Override
    public boolean login() throws AuthException {
        NameCallback userNameCallback = new NameCallback("userName");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        Callback[] callbacks = { userNameCallback, passwordCallback };
        try {
            callbackHandler.handle(callbacks);
        } catch (UnsupportedCallbackException e) {
            throw new AuthException("Callback type does not support. ", e);
        } catch (IOException e) {
            throw new AuthException("Exception occurred while handling authentication data. ", e);
        }
        userName = userNameCallback.getName();
        password = passwordCallback.getPassword();
        AuthResult authResult = userStore.authenticate(userName, password);
        success = authResult.isAuthenticated();
        if (success) {
            authenticationId = authResult.getUserId();
        }
        return success;
    }

    @Override
    public boolean commit() throws LoginException {
        if (success) {
            userPrincipal = new UserPrincipal(authenticationId);
            if (!subject.getPrincipals().contains(userPrincipal)) {
                subject.getPrincipals().add(userPrincipal);
            }
        }
        cleanAuthInputData();
        return success;
    }

    @Override
    public boolean abort() throws LoginException {
        if (success) {
            logout();
        } else {
            cleanAuthInputData();
        }
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(userPrincipal);
        success = false;
        authenticationId = null;
        userPrincipal = null;
        cleanAuthInputData();
        return true;
    }

    private void cleanAuthInputData() {
        userName = null;
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = ' ';
            }
            password = null;
        }
    }
}
