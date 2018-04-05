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
package io.ballerina.messaging.broker.auth.authorization.provider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.DiscretionaryAccessController;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.ResourceCacheKey;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.impl.AuthResourceRdbmsDao;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * Class provides implementation of @{@link DiscretionaryAccessController} with database based auth resource store.
 */
public class RdbmsDacHandler extends DiscretionaryAccessController {

    /**
     * User cache which contains authResource Key vs cache entry.
     */
    private LoadingCache<ResourceCacheKey, AuthResource> authResourceCache;

    private AuthResourceRdbmsDao authResourceDao;

    private UserStore userStore;

    @Override
    public void initialize(StartupContext startupContext, UserStore userStore, Map<String, String> properties)
            throws Exception {
        DataSource dataSource = startupContext.getService(DataSource.class);
        BrokerConfigProvider configProvider = startupContext.getService(BrokerConfigProvider.class);
        BrokerAuthConfiguration brokerAuthConfiguration = configProvider.getConfigurationObject(
                BrokerAuthConfiguration.NAMESPACE, BrokerAuthConfiguration.class);

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
    public void addResource(String resourceType, String resourceName, String owner) throws AuthServerException {
        authResourceDao.persist(new AuthResource(resourceType, resourceName, true, owner));
    }

    @Override
    public boolean deleteResource(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException {
        AuthResource existingResource = getAuthResource(resourceType, resourceName);
        if (Objects.nonNull(existingResource)) {
            authResourceDao.delete(resourceType, resourceName);
            authResourceCache.invalidate(new ResourceCacheKey(resourceType, resourceName));
        }

        return true;
    }

    @Override
    public AuthResource getAuthResource(String resourceType, String resourceName)
            throws AuthNotFoundException {
        try {
            return authResourceCache.get(new ResourceCacheKey(resourceType, resourceName));
        } catch (ExecutionException e) {
            throw new AuthNotFoundException("Error occurred while retrieving resource from cache for type : "
                                                          + resourceType + "  name : " + resourceName, e);
        }
    }

    @Override
    public boolean changeResourceOwner(String resourceType, String resourceName, String newOwner)
            throws AuthServerException {
        boolean success = authResourceDao.updateOwner(resourceType, resourceName, newOwner);

        if (success) {
            authResourceCache.invalidate(new ResourceCacheKey(resourceType, resourceName));
        }

        return success;
    }

    @Override
    public boolean addGroupsToResource(String resourceType, String resourceName, String action, List<String> groups)
            throws AuthServerException {
        boolean success = authResourceDao.addGroups(resourceType, resourceName, action, groups);
        if (success) {
            authResourceCache.invalidate(new ResourceCacheKey(resourceType, resourceName));
        }
        return success;
    }

    @Override
    public boolean removeGroupFromResource(String resourceType, String resourceName, String action, String group)
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
