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
package io.ballerina.messaging.broker.auth.authorization;

import io.ballerina.messaging.broker.auth.AuthDuplicateException;
import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;

import java.util.List;
import java.util.Set;

/**
 * Representation for auth resource store which managers resource mappings to provide Discretionary access control
 * (DAC) based authorization model for broker resources.
 */
public interface AuthResourceStore {
    /**
     * Authorize user for given resource and action
     *
     * @param resourceType resourceType
     * @param resourceName resource
     * @param action       resource action
     * @param userId       an user identifier
     * @param userGroups   set of user groups of user
     * @return is authorized or not
     * @throws AuthServerException throws if error occurs while authorization
     */
    boolean authorize(String resourceType, String resourceName, String action, String userId, Set<String> userGroups)
            throws AuthServerException, AuthNotFoundException;

    /**
     * Add resource to auth resource store.
     *
     * @param authResource an authResource
     * @throws AuthServerException throws if error occurs while adding resource
     */
    void add(AuthResource authResource) throws AuthServerException, AuthDuplicateException;

    /**
     * Update resource to auth resource store.
     *
     * @param authResource an authResource
     * @throws AuthServerException throws if error occurs while updating resource
     */
    void update(AuthResource authResource) throws AuthServerException, AuthNotFoundException;

    /**
     * Delete resource to auth resource store.
     *
     * @param resourceType resourceType
     * @param resourceName resource
     * @throws AuthServerException throws if error occurs while deleting resource
     */
    boolean delete(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException;

    /**
     * Read resource from auth resource store.
     *
     * @param resourceType resourceType
     * @param resourceName resource
     * @return an authResource
     * @throws AuthServerException throws if error occurs while reading resource
     */
    AuthResource read(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException;

    /**
     * Read all resources for given auth resource type and owner.
     *
     * @param resourceType resourceType
     * @param ownerId      resource owner
     * @return a list of all authResource
     * @throws AuthServerException throws if error occurs while reading resource
     */
    List<AuthResource> readAll(String resourceType, String ownerId) throws AuthServerException;

    /**
     * Read all resources for given auth resource type, owner and action.
     *
     * @param resourceType resourceType
     * @param ownerId      resource owner
     * @param action       resource action
     * @return a list of all authResource
     * @throws AuthServerException throws if error occurs while reading resource
     */
    List<AuthResource> readAll(String resourceType, String action, String ownerId)
            throws AuthServerException, AuthException;

    /**
     * Change owner of the given auth resource.
     *
     * @param resourceType resource type
     * @param resourceName name of the resource
     * @param newOwner     user ID of the new owner
     */
    void updateOwner(String resourceType, String resourceName, String newOwner)
            throws AuthServerException, AuthNotFoundException;

    /**
     * Add a group mapping to the given auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param action       action
     * @param group        group to add
     */
    void addGroup(String resourceType, String resourceName, String action, String group)
            throws AuthNotFoundException, AuthServerException;

    /**
     * Remove a group mapping from the given auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param action       action
     * @param group        group to add
     */
    void removeGroup(String resourceType, String resourceName, String action, String group)
            throws AuthServerException, AuthNotFoundException;

}
