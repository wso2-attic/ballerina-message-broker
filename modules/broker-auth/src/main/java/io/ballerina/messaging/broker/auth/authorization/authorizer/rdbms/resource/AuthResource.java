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
package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Representation for auth resource.
 */
public class AuthResource {


    private final String resourceName;

    private final String resourceType;

    private final boolean durable;

    private String owner;

    private Map<String, Set<String>> actionsUserGroupsMap = new ConcurrentHashMap<>();

    public AuthResource(String resourceType, String resourceName, boolean durable, String owner,
                        Map<String, Set<String>> actionsUserGroupsMap) {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.durable = durable;
        this.owner = owner;
        this.actionsUserGroupsMap = actionsUserGroupsMap;
    }

    public AuthResource(String resourceType, String resourceName, boolean durable, String owner) {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.durable = durable;
        this.owner = owner;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public boolean isDurable() {
        return durable;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Map<String, Set<String>> getActionsUserGroupsMap() {
        return actionsUserGroupsMap;
    }
}

