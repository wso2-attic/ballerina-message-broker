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
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope.AuthScope;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthNotFoundException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthServerException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Defines empty @{@link AuthScopeStore} when authorization is disabled.
 */
class EmptyAuthScopeStore implements AuthScopeStore {

    @Override
    public boolean authorize(String authScopeName, Set<String> userGroups)
            throws BrokerAuthServerException, BrokerAuthNotFoundException {
        return true;
    }

    @Override
    public void update(String authScopeName, List<String> userGroups) throws BrokerAuthServerException {

    }

    @Override
    public AuthScope read(String authScopeName) throws BrokerAuthServerException {
        return null;
    }

    @Override
    public List<AuthScope> readAll() throws BrokerAuthServerException {
        return Collections.emptyList();
    }
}
