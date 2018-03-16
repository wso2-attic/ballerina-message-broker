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
package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.AuthScopeStore;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope.dao.AuthScopeDao;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.scope.dao.impl.AuthScopeRdbmsDao;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * Class use implementation of @{@link AuthScopeStore} to provide database based auth scope store .
 */
public class AuthScopeStoreImpl implements AuthScopeStore {

    private AuthScopeDao authScopeDao;

    /**
     * auth scope cache which contains scope key vs user groups set.
     */
    private LoadingCache<String, AuthScope> authScopeCache;

    public AuthScopeStoreImpl(BrokerAuthConfiguration brokerAuthConfiguration, DataSource dataSource) {
        this.authScopeDao = new AuthScopeRdbmsDao(dataSource);
        this.authScopeCache = CacheBuilder.newBuilder()
                                          .maximumSize(
                                                  brokerAuthConfiguration.getAuthorization().getCache()
                                                                         .getSize())
                                          .expireAfterWrite(brokerAuthConfiguration.getAuthorization()
                                                                                   .getCache()
                                                                                   .getTimeout(),
                                                            TimeUnit.MINUTES)
                                          .build(new AuthScopeCacheLoader());
    }

    @Override
    public boolean authorize(String authScopeName, Set<String> userGroups)
            throws AuthNotFoundException {
        try {
            AuthScope authScope = authScopeCache.get(authScopeName);
            return Objects.nonNull(authScope) && authScope.getUserGroups().stream().anyMatch(userGroups::contains);
        } catch (ExecutionException e) {
            throw new AuthNotFoundException("A scope was not found for the scope name: " + authScopeName, e);
        }
    }

    @Override
    public void update(String authScopeName, List<String> userGroups) throws AuthServerException {
        authScopeDao.update(authScopeName, userGroups);
        authScopeCache.invalidate(authScopeName);
    }

    @Override
    public AuthScope read(String authScopeName) throws AuthServerException {
        return authScopeDao.read(authScopeName);
    }

    @Override
    public List<AuthScope> readAll() throws AuthServerException {
        return authScopeDao.readAll();
    }

    private class AuthScopeCacheLoader extends CacheLoader<String, AuthScope> {
        @Override
        public AuthScope load(@Nonnull String scopeName) throws AuthNotFoundException, AuthServerException {
            AuthScope authScope = read(scopeName);
            if (Objects.nonNull(authScope)) {
                return authScope;
            } else {
                throw new AuthNotFoundException("Scope does not found");
            }
        }
    }
}
