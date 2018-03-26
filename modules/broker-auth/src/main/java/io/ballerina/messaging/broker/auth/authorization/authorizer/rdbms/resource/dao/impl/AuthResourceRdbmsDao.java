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

import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao.AuthResourceDao;
import io.ballerina.messaging.broker.common.DaoException;
import io.ballerina.messaging.broker.common.util.function.ThrowingConsumer;
import io.ballerina.messaging.broker.common.util.function.ThrowingFunction;

import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;

/**
 * Class implements {@link AuthResourceDao} to provide database functionality to manage auth resources.
 */
public class AuthResourceRdbmsDao implements AuthResourceDao {

    private final AuthResourceCrudOperationsDao authResourceCrudOperationsDao;

    public AuthResourceRdbmsDao(DataSource dataSource) {
        this.authResourceCrudOperationsDao = new AuthResourceCrudOperationsDao(dataSource);
    }

    @Override
    public void persist(AuthResource authResource) throws AuthServerException {
        transaction(connection -> {
            authResourceCrudOperationsDao.storeResource(connection, authResource);
            authResourceCrudOperationsDao.persistUserGroupMappings(connection, authResource.getResourceType(),
                                                                   authResource.getResourceName(),
                                                                   authResource.getActionsUserGroupsMap());
        });
    }

    @Override
    public void update(AuthResource authResource) throws AuthServerException {
        transaction(connection -> {
            authResourceCrudOperationsDao.updateOwner(connection, authResource.getResourceType(),
                                                      authResource.getResourceName(), authResource.getOwner());

            authResourceCrudOperationsDao.deleteUserGroupMappings(connection, authResource.getResourceType(),
                                                                  authResource.getResourceName());
            authResourceCrudOperationsDao.persistUserGroupMappings(connection, authResource.getResourceType(),
                                                                   authResource.getResourceName(),
                                                                   authResource.getActionsUserGroupsMap());
        });
    }

    @Override
    public boolean delete(String resourceType, String resource) throws AuthServerException {
        return transaction((ThrowingFunction<Connection, Boolean, Exception>) connection ->
                authResourceCrudOperationsDao.deleteResource(connection, resourceType, resource));
    }

    @Override
    public AuthResource read(String resourceType, String resourceName) throws AuthServerException {

        return selectOperation(connection ->
                                       authResourceCrudOperationsDao.read(connection, resourceType, resourceName));
    }

    @Override
    public List<AuthResource> readAll(String resourceType, String ownerId) throws AuthServerException {

        return selectOperation((ThrowingFunction<Connection, List<AuthResource>, Exception>) connection ->
                authResourceCrudOperationsDao.readAll(connection, resourceType, ownerId));
    }

    @Override
    public List<AuthResource> readAll(String resourceType, String action, String ownerId,
                                      List<String> userGroups) throws AuthServerException {
        return selectOperation((ThrowingFunction<Connection, List<AuthResource>, Exception>) connection ->
                authResourceCrudOperationsDao.readAll(connection, resourceType, action, ownerId, userGroups));
    }

    @Override
    public boolean isExists(String resourceType, String resource) throws AuthServerException {
        return selectOperation(connection ->
                              authResourceCrudOperationsDao.isExists(connection, resourceType, resource)
                              );
    }

    @Override
    public boolean updateOwner(String resourceType, String resourceName, String newOwner) throws AuthServerException {
        return transaction((ThrowingFunction<Connection, Boolean, Exception>) connection ->
                authResourceCrudOperationsDao.updateOwner(connection, resourceType, resourceName, newOwner));
    }

    @Override
    public boolean addGroup(String resourceType, String resourceName, String action, String group)
            throws AuthServerException {
        return transaction((ThrowingFunction<Connection, Boolean, Exception>) connection ->
                authResourceCrudOperationsDao.addGroup(connection, resourceType, resourceName, action, group));
    }

    @Override
    public boolean removeGroup(String resourceType, String resourceName, String action, String group)
            throws AuthServerException {
        return transaction((ThrowingFunction<Connection, Boolean, Exception>) connection ->
                authResourceCrudOperationsDao.removeGroup(connection, resourceType, resourceName, action, group));
    }

    private <E extends Exception> void transaction(ThrowingConsumer<Connection, E> command)
            throws AuthServerException {
        try {
            authResourceCrudOperationsDao.transaction(command);
        } catch (DaoException e) {
            throw new AuthServerException("Error occurred while executing transaction", e);
        }
    }

    private <R, E extends Exception> R transaction(ThrowingFunction<Connection, R, E> command)
            throws AuthServerException {
        try {
            return authResourceCrudOperationsDao.transaction(command);
        } catch (DaoException e) {
            throw new AuthServerException("Error occurred while executing transaction", e);
        }
    }

    private  <R, E extends Exception> R selectOperation(ThrowingFunction<Connection, R, E> command)
            throws AuthServerException {
        try {
            return authResourceCrudOperationsDao.selectAndGetOperation(command);
        } catch (DaoException e) {
            throw new AuthServerException("Error occurred while executing transaction", e);
        }
    }
}
