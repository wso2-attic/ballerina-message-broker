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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.AuthResourceStore;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.impl.AuthResourceRdbmsDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * Class provides implementation of @{@link AuthResourceStore} with database based auth resource store.
 */
public class AuthResourceStoreImpl implements AuthResourceStore {

    /**
     * User cache which contains authResource Key vs cache entry.
     */
    private LoadingCache<ResourceCacheKey, AuthResource> authResourceCache;

    private AuthResourceRdbmsDao authResourceDao;

    private UserStore userStore;

    public AuthResourceStoreImpl(BrokerAuthConfiguration brokerAuthConfiguration, DataSource dataSource,
                                 UserStore userStore) {
        this.authResourceDao = new AuthResourceRdbmsDao(dataSource);
        this.userStore = userStore;
        this.authResourceCache = CacheBuilder.newBuilder()
                                             .maximumSize(brokerAuthConfiguration.getAuthorization().getCache()
                                                                                 .getSize())
                                             .expireAfterWrite(brokerAuthConfiguration.getAuthorization()
                                                                                      .getCache()
                                                                                      .getTimeout(), TimeUnit.MINUTES)
                                             .build(new AuthResourceCacheLoader());
    }

    @Override
    public boolean authorize(String resourceType, String resourceName, String action, String userId,
                             Set<String> userGroups) throws AuthNotFoundException {
        AuthResource authResource = read(resourceType, resourceName);
        return Objects.nonNull(authResource) && (authResource.getOwner().equals(userId) ||
                authResource.getActionsUserGroupsMap().get(action).stream().anyMatch(userGroups::contains));
    }

    @Override
    public void add(AuthResource authResource) throws AuthServerException {
        authResourceDao.persist(authResource);
    }

    @Override
    public void update(AuthResource authResource) throws AuthServerException, AuthNotFoundException {
        AuthResource existingResource = read(authResource.getResourceType(),
                                             authResource.getResourceName());
        if (Objects.isNull(existingResource)) {
            throw new AuthNotFoundException("Resource not found for resource type : " +
                                                          authResource.getResourceType() + "  name : " +
                                                          authResource.getResourceName());
        }
        authResourceDao.update(authResource);
        authResourceCache.invalidate(
                new ResourceCacheKey(authResource.getResourceType(), authResource.getResourceName()));
    }

    @Override
    public boolean delete(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException {
        AuthResource existingResource = read(resourceType, resourceName);
        if (Objects.nonNull(existingResource)) {
            authResourceDao.delete(resourceType, resourceName);
            authResourceCache.invalidate(new ResourceCacheKey(resourceType, resourceName));
        }

        return true;
    }

    @Override
    public AuthResource read(String resourceType, String resourceName)
            throws AuthNotFoundException {
        try {
            return authResourceCache.get(new ResourceCacheKey(resourceType, resourceName));
        } catch (ExecutionException e) {
            throw new AuthNotFoundException("Error occurred while retrieving resource from cache for type : "
                                                          + resourceType + "  name : " + resourceName, e);
        }
    }

    @Override
    public List<AuthResource> readAll(String resourceType, String ownerId) throws AuthServerException {
        return authResourceDao.readAll(resourceType, ownerId);
    }

    @Override
    public List<AuthResource> readAll(String resourceType, String action, String ownerId) throws
            AuthServerException, AuthException {
        return authResourceDao.readAll(resourceType,
                                       action,
                                       ownerId,
                                       new ArrayList<>(userStore.getUserGroupsList(ownerId)));
    }

    @Override
    public boolean updateOwner(String resourceType, String resourceName, String newOwner)
            throws AuthServerException {
        boolean success = authResourceDao.updateOwner(resourceType, resourceName, newOwner);

        if (success) {
            authResourceCache.invalidate(new ResourceCacheKey(resourceType, resourceName));
        }

        return success;
    }

    @Override
    public boolean addGroup(String resourceType, String resourceName, String action, String group)
            throws AuthServerException {
        boolean success = authResourceDao.addGroup(resourceType, resourceName, action, group);
        if (success) {
            authResourceCache.invalidate(new ResourceCacheKey(resourceType, resourceName));
        }

        return success;
    }

    @Override
    public boolean removeGroup(String resourceType, String resourceName, String action, String group)
            throws AuthServerException {
        boolean success = authResourceDao.removeGroup(resourceType, resourceName, action, group);
        if (success) {
            authResourceCache.invalidate(new ResourceCacheKey(resourceType, resourceName));
        }

        return success;
    }

    private class AuthResourceCacheLoader extends CacheLoader<ResourceCacheKey, AuthResource> {
        @Override
        public AuthResource load(@Nonnull ResourceCacheKey resourceCacheKey) throws AuthNotFoundException,
                AuthServerException {
            AuthResource authResource = authResourceDao.read(resourceCacheKey.getResourceType(),
                                                             resourceCacheKey.getResourceName());
            if (Objects.nonNull(authResource)) {
                return authResource;
            } else {
                throw new AuthNotFoundException("Resource does not found");
            }
        }
    }
}
