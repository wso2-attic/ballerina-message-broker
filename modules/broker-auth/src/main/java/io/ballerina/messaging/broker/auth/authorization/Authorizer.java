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
import io.ballerina.messaging.broker.common.StartupContext;

/**
 * Interface represents authorization for broker resources.
 * This will provide two functions.
 * <p>
 * 1. authorize
 * 2. authorize by matching resource pattern
 */
public interface Authorizer {

    /**
     * Initialize authorization strategy based on given auth configuration, user store manager and data source.
     *
     * @param startupContext the startup context provides registered services for authProvider
     */
    void initialize(StartupContext startupContext) throws Exception;

    /**
     * Authorize user with given scope key.
     *
     * @param scopeName a scope key
     * @param userId    an user identifier
     * @return if authorised or not
     * @throws AuthException throws if error occur during authorization
     */
    boolean authorize(String scopeName, String userId)
            throws AuthException, AuthServerException, AuthNotFoundException;

    /**
     * Authorize resource with given resource and action.
     *
     * @param resourceType resource Type
     * @param resource     resource
     * @param action       action
     * @param userId       user identifier
     * @return if authorised or not
     * @throws AuthException throws if error occur during authorization
     */
    boolean authorize(String resourceType, String resource, String action, String userId)
            throws AuthException, AuthServerException, AuthNotFoundException;

    /**
     * Create auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param durable      is durable
     * @param owner        resource owner
     * @throws AuthServerException    throws if error occurs while authorizing resource
     * @throws AuthDuplicateException throws if error occurs while authorizing resource
     */
    void addProtectedResource(String resourceType, String resourceName, boolean durable, String owner)
            throws AuthServerException, AuthDuplicateException;

    /**
     * Delete auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @throws AuthServerException   throws if error occurs while authorizing resource
     * @throws AuthNotFoundException throws if auth resource is not found
     */
    void deleteProtectedResource(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException;

    /**
     * Query auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @return matching auth resource if exists, null otherwise
     * @throws AuthServerException   throws if error occurs while authorizing resource
     * @throws AuthNotFoundException throws if auth resource is not found
     */
    AuthResource getAuthResource(String resourceType, String resourceName)
            throws AuthServerException, AuthNotFoundException;
}
