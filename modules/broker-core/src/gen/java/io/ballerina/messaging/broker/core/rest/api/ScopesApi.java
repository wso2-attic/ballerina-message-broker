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

package io.ballerina.messaging.broker.core.rest.api;

import io.ballerina.messaging.broker.core.rest.BrokerAdminService;
import io.ballerina.messaging.broker.core.rest.model.Error;
import io.ballerina.messaging.broker.core.rest.model.Scope;
import io.ballerina.messaging.broker.core.rest.model.ScopeUpdateRequest;
import io.ballerina.messaging.broker.core.rest.model.ScopeUpdateResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.wso2.msf4j.Request;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


@Path(BrokerAdminService.API_BASE_PATH + "/scopes")
@Api(description = "the scopes API")
@Produces({ "application/json" })
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2018-03-09T19:21:49.793+05:30")
public class ScopesApi {

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all scopes", notes = "Retrieves all the scopes", response = Scope.class, responseContainer = "List", authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of Scopes", response = Scope.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Authentication Data is missing or invalid", response = Error.class) })
    public Response getAllScopes(@Context Request request) {
        return Response.ok().entity("magic!").build();
    }

    @GET
    @Path("/{name}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a scope", notes = "Retrieves scope for given scope name", response = Scope.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Scope", response = Scope.class),
        @ApiResponse(code = 401, message = "Authentication Data is missing or invalid", response = Error.class),
        @ApiResponse(code = 404, message = "Scope not found", response = Error.class) })
    public Response getScope(@Context Request request, @PathParam("name") @ApiParam("Name of the scope") String name) {
        return Response.ok().entity("magic!").build();
    }

    @PUT
    @Path("/{name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a scope", notes = "Update given scope", response = ScopeUpdateResponse.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Scope updated", response = ScopeUpdateResponse.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = Error.class),
        @ApiResponse(code = 401, message = "Authentication Data is missing or invalid", response = Error.class),
        @ApiResponse(code = 404, message = "Scope key not found", response = Error.class),
        @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format.", response = Error.class) })
    public Response updateScope(@Context Request request, @PathParam("name") @ApiParam("Name of the scope needs to update") String name, @Valid ScopeUpdateRequest body) {
        return Response.ok().entity("magic!").build();
    }
}
