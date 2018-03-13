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

import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthDuplicateException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthNotFoundException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthServerException;
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
     *  @param startupContext the startup context provides registered services for authProvider
     * */
    void initialize(StartupContext startupContext) throws Exception;

    /**
     * Authorize user with given scope key.
     *
     * @param scopeName a scope key
     * @param userId    an user identifier
     * @return if authorised or not
     * @throws BrokerAuthException throws if error occur during authorization
     */
    boolean authorize(String scopeName, String userId)
            throws BrokerAuthException, BrokerAuthServerException, BrokerAuthNotFoundException;

    /**
     * Authorize resource with given resource and action.
     *
     * @param resourceType resource Type
     * @param resource     resource
     * @param action       action
     * @param userId       user identifier
     * @return if authorised or not
     * @throws BrokerAuthException throws if error occur during authorization
     */
    boolean authorize(String resourceType, String resource, String action, String userId)
            throws BrokerAuthException, BrokerAuthServerException, BrokerAuthNotFoundException;

    /**
     * Create auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param durable      is durable
     * @param owner        resource owner
     * @throws BrokerAuthServerException    throws if error occurs while authorizing resource.
     * @throws BrokerAuthDuplicateException throws if error occurs while authorizing resource.
     */
    void addProtectedResource(String resourceType, String resourceName, boolean durable, String owner)
            throws BrokerAuthServerException, BrokerAuthDuplicateException;

    /**
     * Delete auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @throws BrokerAuthServerException   throws if error occurs while authorizing resource.
     * @throws BrokerAuthNotFoundException throws if error occurs while authorizing resource.
     */
    void deleteProtectedResource(String resourceType, String resourceName)
            throws BrokerAuthServerException, BrokerAuthNotFoundException;

    AuthResource getAuthResource(String s, String name) throws BrokerAuthServerException, BrokerAuthNotFoundException;

    /**
     * Returns auth scope store for auth store
     *
     * @return auth scope store
     */
    AuthScopeStore getAuthScopeStore();
}
