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
package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms;

import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.ResourceCacheKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class represents user cache entry which will be cached for given user.
 */
class UserCacheEntry {

    private Set<String> userGroups;

    private Set<String> authorizedScopes = new HashSet<>();

    private Map<ResourceCacheKey, Set<String>> authorizedResourceActions = new HashMap<>();

    Set<String> getUserGroups() {
        return userGroups;
    }

    void setUserGroups(Set<String> userGroups) {
        this.userGroups = userGroups;
    }

    Set<String> getAuthorizedScopes() {
        return authorizedScopes;
    }

    public Map<ResourceCacheKey, Set<String>> getAuthorizedResourceActions() {
        return authorizedResourceActions;
    }

    public void setAuthorizedResourceActions(
            Map<ResourceCacheKey, Set<String>> authorizedResourceActions) {
        this.authorizedResourceActions = authorizedResourceActions;
    }
}
