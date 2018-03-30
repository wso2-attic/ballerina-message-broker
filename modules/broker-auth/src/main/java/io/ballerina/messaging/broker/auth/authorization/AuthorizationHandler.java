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

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.UsernamePrincipal;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAction;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAuthScope;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceType;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;

import javax.security.auth.Subject;

/**
 * Class handles authorization for broker actions.
 */
public class AuthorizationHandler {

    private Authorizer authorizer;

    public AuthorizationHandler(Authorizer authorizer) {
        this.authorizer = authorizer;
    }

    /**
     * Handle given auth scope and auth resource authorization.
     *
     * @param brokerAuthScope auth scope
     * @param resourceType    resource type
     * @param resourceName    resource name
     * @param action          action
     * @throws AuthException throws if error occurs while authorizing resource.
     */
    public void handle(ResourceAuthScope brokerAuthScope, ResourceType resourceType, String resourceName,
                       ResourceAction action, Subject subject)
            throws AuthException, AuthNotFoundException {
        try {
            if (!authorizer.authorize(brokerAuthScope.toString(), getUserFromSubject(subject))
                    && !authorizer.authorize(resourceType.toString(),
                                             resourceName,
                                             action.toString(),
                                             getUserFromSubject(subject))) {
                throw new AuthException(
                        "Unauthorized action on : " + resourceType.toString() + " resourceName: " + resourceName
                                + " action: " + action.toString());
            }
        } catch (AuthServerException e) {
            throw new AuthException("Error occurred while authorizing on : " + resourceType.toString() +
                                            " resourceName: " + resourceName +
                                            " action: " + action.toString(), e);
        }
    }

    /**
     * Handle given auth scope  authorization.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param action       action
     * @throws AuthException throws if error occurs while authorizing resource.
     */
    public void handle(ResourceType resourceType, String resourceName, ResourceAction action, Subject subject)
            throws AuthException, AuthNotFoundException {
        try {
            if (!authorizer.authorize(resourceType.toString(),
                                      resourceName,
                                      action.toString(),
                                      getUserFromSubject(subject))) {
                throw new AuthException(resourceType, resourceName, action);
            }
        } catch (AuthServerException e) {
            throw new AuthException(
                    "Error occurred while authorizing on : " + resourceType.toString() + " resourceName: "
                            + resourceName + " action: " + action.toString(), e);
        }
    }

    /**
     * Handle given auth scope authorization.
     *
     * @param authScope authScope
     * @throws AuthException throws if error occurs while authorizing resource.
     */
    public void handle(ResourceAuthScope authScope, Subject subject)
            throws AuthException {
        try {
            if (!authorizer.authorize(authScope.toString(), getUserFromSubject(subject))) {
                throw new AuthException("Unauthorized action on auth scope key : " + authScope.toString());
            }
        } catch (AuthServerException | AuthNotFoundException e) {
            throw new AuthException("Error occurred while authorizing auth scope key : " + authScope.toString(), e);
        }
    }

    /**
     * Create auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @param durable      is durable
     * @throws AuthException throws if error occurs while authorizing resource.
     */
    public void createAuthResource(ResourceType resourceType, String resourceName, boolean durable, Subject subject)
            throws AuthException {
        try {
            authorizer.addProtectedResource(resourceType.toString(),
                                            resourceName,
                                            durable,
                                            getUserFromSubject(subject));
        } catch (AuthServerException e) {
            throw new AuthException("Error while creating " + resourceType + " with name : " + resourceName, e);
        }
    }

    /**
     * Delete auth resource.
     *
     * @param resourceType resource type
     * @param resourceName resource name
     * @throws AuthException throws if error occurs while authorizing resource.
     */
    public void deleteAuthResource(ResourceType resourceType, String resourceName)
            throws AuthException, ResourceNotFoundException {
        try {
            authorizer.deleteProtectedResource(resourceType.toString(), resourceName);
        } catch (AuthServerException e) {
            throw new AuthException("Error while deleting " + resourceType + " with name : " + resourceName, e);
        } catch (AuthNotFoundException e) {
            throw new ResourceNotFoundException(
                    "Error occurred while authorizing due to resource name : " + resourceName + " not found.");
        }
    }

    /**
     * Extract username from the {@link Subject}
     *
     * @param subject an entity with username
     * @return username
     */
    private String getUserFromSubject(Subject subject) {
        UsernamePrincipal usernamePrincipal = (UsernamePrincipal) subject.getPrincipals().iterator().next();
        return usernamePrincipal.getName();
    }
}
