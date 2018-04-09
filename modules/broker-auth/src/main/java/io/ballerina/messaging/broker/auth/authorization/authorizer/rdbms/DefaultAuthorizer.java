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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.Authorizer;
import io.ballerina.messaging.broker.auth.authorization.DiscretionaryAccessController;
import io.ballerina.messaging.broker.auth.authorization.MandatoryAccessController;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.ResourceCacheKey;
import io.ballerina.messaging.broker.auth.authorization.provider.MemoryDacHandler;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 * Class provides database based @{@link Authorizer} implementation.
 */
public class DefaultAuthorizer implements Authorizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthorizer.class);
    private final MemoryDacHandler memoryDacHandler;

    private UserStore userStore;

    /**
     * Cache which store user id vs  user cache entry
     */
    private LoadingCache<String, UserCacheEntry> userCache;
    private DiscretionaryAccessController externalDacHandler;
    private MandatoryAccessController macHandler;

    public DefaultAuthorizer(DiscretionaryAccessController externalDacHandler,
                             MandatoryAccessController macHandler,
                             UserStore userStore) {
        this.externalDacHandler = externalDacHandler;
        this.memoryDacHandler = new MemoryDacHandler();
        this.macHandler = macHandler;
        this.userStore = userStore;
    }

    @Override
    public void initialize(StartupContext startupContext) throws Exception {
        BrokerConfigProvider configProvider = startupContext.getService(BrokerConfigProvider.class);
        BrokerAuthConfiguration brokerAuthConfiguration = configProvider.getConfigurationObject(
                BrokerAuthConfiguration.NAMESPACE, BrokerAuthConfiguration.class);

        userCache = CacheBuilder.newBuilder()
                                .maximumSize(brokerAuthConfiguration.getAuthorization().getCache()
                                                                    .getSize())
                                .expireAfterWrite(brokerAuthConfiguration.getAuthorization()
                                                                         .getCache()
                                                                         .getTimeout(),
                                                  TimeUnit.MINUTES)
                                .build(new UserCacheLoader());
    }

    @Override
    public boolean authorize(String scopeName, String userId)
            throws AuthException, AuthServerException, AuthNotFoundException {
        try {
            if (userId != null) {
                UserCacheEntry userCacheEntry = userCache.get(userId);
                boolean anyMatch = userCacheEntry.getAuthorizedScopes()
                                                 .stream()
                                                 .anyMatch(s -> s.equals(scopeName));
                if (anyMatch) {
                    LOGGER.debug("Scopes are loaded from cache for auth scope key : {} ", scopeName);
                    return true;
                } else {
                    if (macHandler.authorize(scopeName, userCacheEntry.getUserGroups())) {
                        userCacheEntry.getAuthorizedScopes().add(scopeName);
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                throw new AuthException("user id cannot be null.");
            }
        } catch (ExecutionException e) {
            throw new AuthServerException("Error occurred while retrieving authorizations from cache for"
                                                        + " scope name : " + scopeName, e);
        }
    }

    @Override
    public boolean authorize(String resourceType, String resourceName, String action, String userId)
            throws AuthException, AuthServerException, AuthNotFoundException {
        ResourceCacheKey resourceCacheKey = new ResourceCacheKey(resourceType, resourceName);
        try {
            if (userId != null) {
                UserCacheEntry userCacheEntry = userCache.get(userId);
                Set<String> authorizedActions =
                        userCacheEntry.getAuthorizedResourceActions().get(resourceCacheKey);
                boolean authorized = Objects.nonNull(authorizedActions) &&
                        authorizedActions
                                .stream()
                                .anyMatch(s -> s.equals(action));
                if (authorized) {
                    LOGGER.debug(
                            "resourceName authorizations are loaded from cache for resourceType : {} resourceName: {}",
                            resourceType,
                            resourceName);
                    return true;
                } else {
                    authorized = authorizeInDac(resourceType, resourceName, action, userId, userCacheEntry);

                    if (authorized) {
                        if (Objects.isNull(authorizedActions)) {
                            authorizedActions = new HashSet<>();
                            userCacheEntry.getAuthorizedResourceActions().put(resourceCacheKey, authorizedActions);
                        }
                        authorizedActions.add(action);
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                throw new AuthException("user id cannot be null.");
            }
        } catch (ExecutionException e) {
            throw new AuthException("Error occurred while retrieving authorizations from cache for "
                                                  + "resourceType : " + resourceType +
                                                  " resourceName: " + resourceName, e);
        }
    }

    private boolean authorizeInDac(String resourceType,
                                   String resourceName,
                                   String action,
                                   String userId,
                                   UserCacheEntry userCacheEntry)
            throws AuthServerException, AuthNotFoundException {
        boolean authorized;

        // First authorize in the in-memory DAC
        authorized = memoryDacHandler.authorize(resourceType,
                                                resourceName,
                                                action,
                                                userId,
                                                userCacheEntry.getUserGroups());
        // Then in the external DAC
        if (!authorized) {
            authorized = externalDacHandler.authorize(resourceType,
                                                      resourceName,
                                                      action,
                                                      userId,
                                                      userCacheEntry.getUserGroups());
        }

        return authorized;
    }

    @Override
    public AuthResource getAuthResource(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException {
        AuthResource authResource = memoryDacHandler.getAuthResource(resourceType, resourceName);

        if (Objects.nonNull(authResource)) {
            return authResource;
        } else {
            return externalDacHandler.getAuthResource(resourceType, resourceName);
        }
    }

    @Override
    public void addProtectedResource(String resourceType, String resourceName, boolean durable, String owner)
            throws AuthServerException {
        getDacHandler(durable).addResource(resourceType, resourceName, owner);
    }

    @Override
    public void deleteProtectedResource(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException {
        if (!memoryDacHandler.deleteResource(resourceType, resourceName)) {
            externalDacHandler.deleteResource(resourceType, resourceName);
        }
    }

    @Override
    public boolean addGroupsToResource(String resourceType, String resourceName, String action, List<String> groups)
            throws AuthServerException {
        boolean success = false;
        if (memoryDacHandler.addGroupsToResource(resourceType, resourceName, action, groups)
                || externalDacHandler.addGroupsToResource(resourceType, resourceName, action, groups)) {
            success = true;
        }
        return success;
    }

    @Override
    public boolean removeGroupFromResource(String resourceType, String resourceName, String action, String group)
            throws AuthServerException, AuthNotFoundException {
        boolean success = false;
        if (memoryDacHandler.removeGroupFromResource(resourceType, resourceName, action, group)
                || externalDacHandler.removeGroupFromResource(resourceType, resourceName, action, group)) {
            success = true;
        }
        return success;
    }

    @Override
    public boolean changeResourceOwner(String resourceType, String resourceName, String owner)
            throws AuthServerException, AuthNotFoundException, AuthException {
        if (!userStore.isUserExists(owner)) {
            throw new AuthException("Invalid username for the owner.");
        }
        boolean success = false;
        if (memoryDacHandler.changeResourceOwner(resourceType, resourceName, owner)
                || externalDacHandler.changeResourceOwner(resourceType, resourceName, owner)) {
            success = true;
        }
        return success;
    }

    private class UserCacheLoader extends CacheLoader<String, UserCacheEntry> {
        @Override
        public UserCacheEntry load(@Nonnull String userId) throws AuthException {
            UserCacheEntry userCacheEntry = new UserCacheEntry();
            userCacheEntry.setUserGroups(userStore.getUserGroupsList(userId));
            return userCacheEntry;
        }
    }

    private DiscretionaryAccessController getDacHandler(boolean durable) {
        if (durable) {
            return externalDacHandler;
        } else {
            return memoryDacHandler;
        }
    }
}
