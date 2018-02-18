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
package io.ballerina.messaging.broker.auth.authorization.authorizer.empty;

import io.ballerina.messaging.broker.auth.authorization.AuthProvider;
import io.ballerina.messaging.broker.auth.authorization.AuthResourceStore;
import io.ballerina.messaging.broker.auth.authorization.AuthScopeStore;
import io.ballerina.messaging.broker.auth.authorization.Authorizer;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthException;
import io.ballerina.messaging.broker.common.StartupContext;

import java.util.Map;

/**
 * Defines empty @{@link Authorizer} when authorization is disabled.
 */
public class DefaultAuthorizer implements Authorizer {

    private AuthResourceStore authResourceStore = new EmptyAuthResourceStore();

    private AuthScopeStore authScopeStore = new EmptyAuthScopeStore();

    @Override
    public void initialize(AuthProvider authProvider, StartupContext startupContext, Map<String, Object> properties)
            throws Exception {
        // do nothing
    }

    @Override
    public boolean authorize(String scopeName, String userId) throws BrokerAuthException {
        return true;
    }

    @Override
    public boolean authorize(String resourceType, String resource, String action, String userId)
            throws BrokerAuthException {
        return true;
    }

    @Override
    public AuthScopeStore getAuthScopeStore() {
        return authScopeStore;
    }

    @Override
    public AuthResourceStore getAuthResourceStore() {
        return authResourceStore;
    }
}
