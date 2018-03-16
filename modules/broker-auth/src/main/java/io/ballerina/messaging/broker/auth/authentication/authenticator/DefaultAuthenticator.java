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

import io.ballerina.messaging.broker.auth.authentication.AuthResult;
import io.ballerina.messaging.broker.auth.authentication.Authenticator;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.Map;

/**
 * Disabled authentication representation for @{@link Authenticator}.
 */
public class DefaultAuthenticator implements Authenticator {

    @Override
    public void initialize(StartupContext startupContext,
                           UserStore userStore,
                           Map<String, Object> properties) throws Exception {
        // nothing to do when authentication is disabled.
    }

    @Override
    public AuthResult authenticate(String username, char[] password) {
        return new AuthResult(true, username);
    }
}
