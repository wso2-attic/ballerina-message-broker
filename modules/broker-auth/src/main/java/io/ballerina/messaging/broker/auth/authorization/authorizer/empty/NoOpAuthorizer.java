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

import io.ballerina.messaging.broker.auth.authorization.AuthScopeStore;
import io.ballerina.messaging.broker.auth.authorization.Authorizer;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.common.StartupContext;

/**
 * Defines empty @{@link Authorizer} when authorization is disabled.
 */
public class NoOpAuthorizer implements Authorizer {

    private AuthScopeStore authScopeStore = new EmptyAuthScopeStore();

    @Override
    public void initialize(StartupContext startupContext) {
        // do nothing
    }

    @Override
    public boolean authorize(String scopeName, String userId) {
        return true;
    }

    @Override
    public boolean authorize(String resourceType, String resource, String action, String userId) {
        return true;
    }

    @Override
    public AuthScopeStore getAuthScopeStore() {
        return authScopeStore;
    }

    @Override
    public void addProtectedResource(String resourceType, String resourceName, boolean durable, String owner) {
        // Do nothing
    }

    @Override
    public void deleteProtectedResource(String resourceType, String resourceName) {
        // Do nothing
    }

    @Override
    public AuthResource getAuthResource(String s, String name) {
        return null;
    }
}
