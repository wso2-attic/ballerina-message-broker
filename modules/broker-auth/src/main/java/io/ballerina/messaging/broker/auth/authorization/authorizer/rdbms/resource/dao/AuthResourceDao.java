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

package io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.dao;

import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;

import java.util.List;

/**
 * Defines functionality required at persistence layer for managing resources.
 */
public interface AuthResourceDao {

    /**
     * Persist resource in the database
     *
     * @param authResource authResource
     * @throws AuthServerException when database operation failed.
     */
    void persist(AuthResource authResource) throws AuthServerException;

    /**
     * Update resource owner in the database
     *
     * @param authResource authResource
     * @throws AuthServerException when database operation failed.
     */
    void update(AuthResource authResource) throws AuthServerException;

    /**
     * Delete resource from database on given given resource type and name and user groups.
     *
     * @param resourceType resource Type
     * @param resource     resource
     * @throws AuthServerException when database operation failed.
     */
    boolean delete(String resourceType, String resource) throws AuthServerException;

    /**
     * Get resource for given resource type and name.
     *
     * @param resourceType resource Type
     * @param resource     resource
     * @return user id
     * @throws AuthServerException when database operation failed.
     */
    AuthResource read(String resourceType, String resource) throws AuthServerException;

    /**
     * Get all resource for given resource type.
     *
     * @param resourceType resource Type
     * @param ownerId      owner
     *                     user id
     * @return user id
     * @throws AuthServerException when database operation failed.
     */
    List<AuthResource> readAll(String resourceType, String ownerId) throws AuthServerException;

    /**
     * Get all resource for given resource type.
     *
     * @param resourceType resource Type
     * @param action       action
     * @param ownerId      owner user id
     * @param userGroups   user groups
     * @return user id
     * @throws AuthServerException when database operation failed.
     */
    List<AuthResource> readAll(String resourceType, String action, String ownerId, List<String> userGroups)
            throws AuthServerException;

    /**
     * Check resource existence for given resource type and name.
     *
     * @param resourceType resource Type
     * @param resource     resource
     * @return is resource exist or not
     * @throws AuthServerException when database operation failed.
     */
    boolean isExists(String resourceType, String resource) throws AuthServerException;

    /**
     * Update owner of the given resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param newOwner     user ID of the new owner
     * @throws AuthServerException when database operation failed
     * @return true if resource was updated
     */
    boolean updateOwner(String resourceType, String resourceName, String newOwner)
            throws AuthServerException;


    /**
     * Add batch of group mapping to the given auth resource
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param action       action
     * @param groups       list of group to add
     * @return true if resource update with  group list
     * @throws AuthServerException when database operation failed.
     */
    boolean addGroups(String resourceType, String resourceName, String action, List<String> groups)
            throws AuthServerException;

    /**
     * Remove a group mapping from the given auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param action       action
     * @param group        group to add
     * @return true if resource was updated
     */
    boolean removeGroup(String resourceType, String resourceName, String action, String group)
            throws AuthNotFoundException, AuthServerException;
}
