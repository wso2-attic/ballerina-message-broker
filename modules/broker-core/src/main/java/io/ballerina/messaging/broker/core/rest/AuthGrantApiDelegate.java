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

package io.ballerina.messaging.broker.core.rest;

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.authorization.AuthorizationHandler;
import io.ballerina.messaging.broker.auth.authorization.Authorizer;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAction;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAuthScope;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceType;
import io.ballerina.messaging.broker.core.rest.model.ResponseMessage;
import io.ballerina.messaging.broker.core.rest.model.UserGroupList;

import java.util.Arrays;
import java.util.List;
import javax.security.auth.Subject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

/**
 * Delegate class to handle authorization grant events
 */
public class AuthGrantApiDelegate {

    private final Authorizer authorizer;

    private final AuthorizationHandler authorizationHandler;

    public AuthGrantApiDelegate(Authorizer authorizer) {
        this.authorizer = authorizer;
        this.authorizationHandler = new AuthorizationHandler(authorizer);
    }

    public Response changeOwner(ResourceType resourceType, String resourceName, String owner, Subject subject) {
        try {
            authorizationHandler.handle(ResourceAuthScope.RESOURCE_GRANT_PERMISSION, resourceType, resourceName,
                                        ResourceAction.GRANT_PERMISSION, subject);
            boolean success = authorizer.changeResourceOwner(resourceType.toString(), resourceName, owner);
            if (!success) {
                throw new BadRequestException("Invalid input. Resource type: " + resourceType.toString()
                        + ", Resource name: " + resourceName + ", Owner: " + owner);
            }
            return Response.noContent().build();
        } catch (AuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        } catch (AuthNotFoundException e) {
            throw new NotFoundException(e.getMessage(), e);
        } catch (AuthServerException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    public Response addUserGroupsToAction(ResourceType resourceType, String resourceName, String action,
                                          UserGroupList userGroupList, Subject subject) {
        try {
            authorizationHandler.handle(ResourceAuthScope.RESOURCE_GRANT_PERMISSION, resourceType, resourceName,
                                        ResourceAction.GRANT_PERMISSION, subject);

            List<String> userGroups = userGroupList.getUserGroups();
            boolean success = authorizer.addGroupsToResource(resourceType.toString(), resourceName, action, userGroups);
            if (!success) {
                throw new BadRequestException("Invalid input. Resource type: " + resourceType.toString()
                        + ", Resource name: " + resourceName + ", Action name: " + action
                        + ", User groups: " + Arrays.toString(userGroups.toArray()));
            }
            return Response.ok(new ResponseMessage().message("User groups successfully added.")).build();
        } catch (AuthException e) {
            throw new ForbiddenException(e.getMessage(), e);
        } catch (AuthNotFoundException e) {
            throw new NotFoundException(e.getMessage(), e);
        } catch (AuthServerException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    public Response removeUserGroup(ResourceType resourceType, String resourceName, String action,
                                    String groupName, Subject subject) {
        try {
            authorizationHandler.handle(ResourceAuthScope.RESOURCE_GRANT_PERMISSION, resourceType, resourceName,
                                        ResourceAction.GRANT_PERMISSION, subject);
            boolean success = authorizer.removeGroupFromResource(resourceType.toString(),
                                                                 resourceName,
                                                                 action,
                                                                 groupName);
            if (!success) {
                throw new BadRequestException("Invalid input. Resource type: " + resourceType.toString()
                        + ", Resource name: " + resourceName + ", Action name: " + action
                        + ", Group name: " + groupName);
            }
            return Response.ok().entity(new ResponseMessage().message("User group successfully removed.")).build();
        } catch (AuthException e) {
            throw new ForbiddenException(e.getMessage(), e);
        } catch (AuthNotFoundException e) {
            throw new NotFoundException(e.getMessage(), e);
        } catch (AuthServerException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }
}
