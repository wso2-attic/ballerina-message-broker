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
package org.wso2.broker.core.security.authentication.jaas;

import org.wso2.broker.core.security.authentication.Authenticator;
import org.wso2.broker.core.security.authentication.exception.BrokerAuthenticationException;
import org.wso2.broker.core.security.authentication.util.BrokerSecurityConstants;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Authenticator for Jaas Login module
 */
public class JaasAuthenticator implements Authenticator {

    @Override
    public boolean authenticate(CallbackHandler callbackHandler) throws BrokerAuthenticationException {
        LoginContext loginContext = null;
        try {
            loginContext = new LoginContext(BrokerSecurityConstants.DEFAULT_JAAS_LOGIN_MODULE, callbackHandler);
            loginContext.login();
            return true;
        } catch (LoginException e) {
            throw new BrokerAuthenticationException("Error while authenticate user with login module ", e);
        } finally {
            if (loginContext != null) {
                try {
                    loginContext.logout();
                } catch (LoginException e) {
                    throw new BrokerAuthenticationException("Error while logout from the module ", e);
                }
            }
        }
    }
}
