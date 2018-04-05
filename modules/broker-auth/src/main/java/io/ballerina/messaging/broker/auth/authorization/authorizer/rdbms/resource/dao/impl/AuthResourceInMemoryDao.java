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
package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.AuthResourceDao;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class implements {@link AuthResourceDao} to provide functionality manage in memory auth resources.
 */
public class AuthResourceInMemoryDao implements AuthResourceDao {

    /**
     * In memory resource store for non durable resources like exchanges, queues in a table structure as,
     * 1. resource type
     * 2. resource name
     * 3. auth resource
     */
    private Table<String, String, AuthResource> inMemoryResourceMap = HashBasedTable.create();

    @Override
    public void persist(AuthResource authResource) throws AuthServerException {
        String resourceType = authResource.getResourceType();
        String resourceName = authResource.getResourceName();
        if (!inMemoryResourceMap.contains(resourceType, resourceName)) {
            inMemoryResourceMap.put(resourceType, resourceName, authResource);
        } else {
            throw new AuthServerException(
                    "Auth resource already exists [" + resourceType + ", " + resourceName + "]");
        }
    }

    @Override
    public void update(AuthResource authResource) {
        inMemoryResourceMap.put(authResource.getResourceType(), authResource.getResourceName(), authResource);
    }

    @Override
    public boolean delete(String resourceType, String resource) {
        AuthResource removedItem = inMemoryResourceMap.remove(resourceType, resource);
        return removedItem != null;
    }

    @Override
    public AuthResource read(String resourceType, String resource) {
        return inMemoryResourceMap.get(resourceType, resource);
    }

    @Override
    public List<AuthResource> readAll(String resourceType, String ownerId) {
        Map<String, AuthResource> resourceMap = inMemoryResourceMap.row(resourceType);
        if (Objects.nonNull(resourceMap)) {
            Collection<AuthResource> authResources = resourceMap.values();
            return authResources.stream()
                                .filter(authResource -> authResource.getOwner().equals(ownerId))
                                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<AuthResource> readAll(String resourceType, String action, String ownerId, List<String> userGroups) {
        Map<String, AuthResource> resourceMap = inMemoryResourceMap.row(resourceType);
        if (Objects.nonNull(resourceMap)) {
            Collection<AuthResource> authResources = resourceMap.values();
            return authResources.stream()
                                .filter(authResource ->
                                                authResource.getOwner().equals(ownerId)
                                                        || checkActionAndGroups(authResource.getActionsUserGroupsMap(),
                                                                                action,
                                                                                userGroups))
                                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean checkActionAndGroups(Map<String, Set<String>> actionUserGroupsMap,
                                         String action,
                                         List<String> userGroups) {
        if (Objects.nonNull(actionUserGroupsMap)) {
            Set<String> authorizedUserGroups = actionUserGroupsMap.get(action);
            return Objects.nonNull(authorizedUserGroups) && authorizedUserGroups.stream()
                                                                                .anyMatch(userGroups::contains);
        }
        return false;
    }

    @Override
    public boolean isExists(String resourceType, String resourceName) {
        return inMemoryResourceMap.contains(resourceType, resourceName);
    }

    @Override
    public boolean updateOwner(String resourceType, String resourceName, String newOwner) {
        AuthResource authResource = inMemoryResourceMap.get(resourceType, resourceName);

        if (Objects.nonNull(authResource)) {
            authResource.setOwner(newOwner);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addGroups(String resourceType, String resourceName, String action, List<String> groups) {
        AuthResource authResource = inMemoryResourceMap.get(resourceType, resourceName);

        if (Objects.nonNull(authResource)) {
            Set<String> userGroups = authResource.getActionsUserGroupsMap()
                    .computeIfAbsent(action, key -> new HashSet<>());
            userGroups.addAll(groups);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeGroup(String resourceType, String resourceName, String action, String group) {
        AuthResource authResource = inMemoryResourceMap.get(resourceType, resourceName);

        if (Objects.nonNull(authResource)) {
            Set<String> userGroups = authResource.getActionsUserGroupsMap()
                                                 .computeIfAbsent(action, key -> new HashSet<>());
            userGroups.remove(group);
            return true;
        } else {
            return false;
        }
    }

}
