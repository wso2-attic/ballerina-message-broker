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
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.AuthResourceStore;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.impl.AuthResourceDaoFactory;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthDuplicateException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthNotFoundException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthServerException;

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

    private AuthResourceDaoFactory authResourceDaoFactory;

    private UserStore userStore;

    public AuthResourceStoreImpl(BrokerAuthConfiguration brokerAuthConfiguration, DataSource dataSource,
                                 UserStore userStore) {
        this.authResourceDaoFactory = new AuthResourceDaoFactory(dataSource);
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
                             Set<String> userGroups) throws BrokerAuthServerException, BrokerAuthNotFoundException {
        AuthResource authResource = read(resourceType, resourceName);
        return Objects.nonNull(authResource) && (authResource.getOwner().equals(userId) ||
                authResource.getActionsUserGroupsMap().get(action).stream().anyMatch(userGroups::contains));
    }

    @Override
    public void add(AuthResource authResource)
            throws BrokerAuthServerException, BrokerAuthDuplicateException {
        if (isAvailable(authResource.getResourceType(), authResource.getResourceName(), authResource.isDurable())) {
            throw new BrokerAuthDuplicateException("Duplicate resource found for resource type : " +
                                                           authResource.getResourceType() + "  name : " +
                                                           authResource.getResourceName());
        }
        authResourceDaoFactory.getAuthResourceDao(authResource.isDurable()).persist(authResource);
    }

    @Override
    public void update(AuthResource authResource) throws BrokerAuthServerException, BrokerAuthNotFoundException {
        AuthResource existingResource = read(authResource.getResourceType(),
                                             authResource.getResourceName());
        if (Objects.isNull(existingResource)) {
            throw new BrokerAuthNotFoundException("Resource not found for resource type : " +
                                                          authResource.getResourceType() + "  name : " +
                                                          authResource.getResourceName());
        }
        authResourceDaoFactory.getAuthResourceDao(existingResource.isDurable()).update(authResource);
        authResourceCache.invalidate(
                new ResourceCacheKey(authResource.getResourceType(), authResource.getResourceName()));
    }

    @Override
    public boolean delete(String resourceType, String resourceName)
            throws BrokerAuthServerException, BrokerAuthNotFoundException {
        AuthResource existingResource = read(resourceType, resourceName);
        if (Objects.nonNull(existingResource)) {
            authResourceDaoFactory.getAuthResourceDao(existingResource.isDurable()).delete(resourceType, resourceName);
            authResourceCache.invalidate(new ResourceCacheKey(resourceType, resourceName));
        }

        return true;
    }

    @Override
    public AuthResource read(String resourceType, String resourceName)
            throws BrokerAuthServerException, BrokerAuthNotFoundException {
        /*
         * Auth resources can be durable or non durable. So first we try to read from memory for non durable resources.
         * Then read from cache for durable resources.
         */
        AuthResource authResource = authResourceDaoFactory.getAuthResourceDao(false).read(resourceType, resourceName);
        if (Objects.isNull(authResource)) {
            try {
                authResource = authResourceCache.get(new ResourceCacheKey(resourceType, resourceName));
            } catch (ExecutionException e) {
                throw new BrokerAuthNotFoundException("Error occurred while retrieving resource from cache for type : "
                                                              + resourceType + "  name : " + resourceName, e);
            }
        }
        return authResource;
    }

    @Override
    public List<AuthResource> readAll(String resourceType, String ownerId) throws BrokerAuthServerException {
        List<AuthResource> durableAuthResources = authResourceDaoFactory.getAuthResourceDao(true).readAll
                (resourceType, ownerId);
        List<AuthResource> nonDurableAuthResources = authResourceDaoFactory.getAuthResourceDao(false).readAll
                (resourceType, ownerId);
        if (Objects.nonNull(durableAuthResources) && Objects.nonNull(nonDurableAuthResources)) {
            durableAuthResources.addAll(nonDurableAuthResources);
            return durableAuthResources;
        } else if (Objects.nonNull(durableAuthResources)) {
            return durableAuthResources;
        }
        return nonDurableAuthResources;
    }

    @Override
    public List<AuthResource> readAll(String resourceType, String action, String ownerId) throws
            BrokerAuthServerException, BrokerAuthException {
        List<AuthResource> durableAuthResources =
                authResourceDaoFactory.getAuthResourceDao(true)
                                      .readAll(resourceType, action, ownerId,
                                               new ArrayList<>(userStore.getUserGroupsList(ownerId)));
        List<AuthResource> nonDurableAuthResources =
                authResourceDaoFactory.getAuthResourceDao(false)
                                      .readAll(resourceType, action, ownerId,
                                               new ArrayList<>(userStore.getUserGroupsList(ownerId)));
        if (Objects.nonNull(durableAuthResources) && Objects.nonNull(nonDurableAuthResources)) {
            durableAuthResources.addAll(nonDurableAuthResources);
            return durableAuthResources;
        } else if (Objects.nonNull(durableAuthResources)) {
            return durableAuthResources;
        }
        return nonDurableAuthResources;
    }

    @Override
    public void updateOwner(String resourceType, String resourceName, String newOwner)
            throws BrokerAuthServerException, BrokerAuthNotFoundException {
        authResourceDaoFactory.getAuthResourceDao(true).updateOwner(resourceType, resourceName, newOwner);
        authResourceCache.invalidate(new ResourceCacheKey(resourceType, resourceName));
    }

    @Override
    public void addGroup(String resourceType, String resourceName, String action, String group)
            throws BrokerAuthServerException, BrokerAuthNotFoundException {
        authResourceDaoFactory.getAuthResourceDao(true).addGroup(resourceType, resourceName, action, group);
        authResourceCache.invalidate(new ResourceCacheKey(resourceType, resourceName));
    }

    private boolean isAvailable(String resourceType, String resourceName, boolean durable)
            throws BrokerAuthServerException {
        return (durable &&
                Objects.nonNull(authResourceCache.getIfPresent(new ResourceCacheKey(resourceType,
                                                                                    resourceName)))) ||
                Objects.nonNull(authResourceDaoFactory.getAuthResourceDao(durable).read(resourceType, resourceName));
    }

    private class AuthResourceCacheLoader extends CacheLoader<ResourceCacheKey, AuthResource> {
        @Override
        public AuthResource load(@Nonnull ResourceCacheKey resourceCacheKey) throws BrokerAuthNotFoundException,
                BrokerAuthServerException {
            AuthResource authResource =
                    authResourceDaoFactory.getAuthResourceDao(true).read(resourceCacheKey.getResourceType(),
                                                                         resourceCacheKey.getResourceName());
            if (Objects.nonNull(authResource)) {
                return authResource;
            } else {
                throw new BrokerAuthNotFoundException("Resource does not found");
            }
        }
    }
}
