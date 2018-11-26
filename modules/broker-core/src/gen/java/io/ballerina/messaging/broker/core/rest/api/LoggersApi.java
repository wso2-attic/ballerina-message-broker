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

import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
import io.ballerina.messaging.broker.auth.authorization.Authorizer;
import io.ballerina.messaging.broker.core.rest.BrokerAdminService;
import io.ballerina.messaging.broker.core.rest.LoggersApiDelegate;
import io.ballerina.messaging.broker.core.rest.model.Error;
import io.ballerina.messaging.broker.core.rest.model.LoggerMetadata;
import io.ballerina.messaging.broker.core.rest.model.ResponseMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.wso2.msf4j.Request;

import javax.security.auth.Subject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path(BrokerAdminService.API_BASE_PATH + "/loggers")
@Api(description = "the loggers API")
@Produces({"application/json"})
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date =
        "2018-11-12T11:47:14.461+05:30")
public class LoggersApi {
    private LoggersApiDelegate loggersApiDelegate;

    public LoggersApi(Authorizer authorizer) {
        this.loggersApiDelegate = new LoggersApiDelegate(authorizer);
    }

    @GET
    @Path("/{name}")
    @Produces({"application/json"})
    @ApiOperation(value = "Get loggers", notes = "Gets metadata of the loggers that matches given name.", response =
            LoggerMetadata.class, responseContainer = "List", authorizations = {
            @Authorization(value = "basicAuth")
    }, tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of loggers", response = LoggerMetadata.class, responseContainer
                    = "List"),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error
                    .class),
            @ApiResponse(code = 403, message = "Requested action unauthorized.", response = Error.class)})
    public Response getFilteredLoggers(@Context Request request, @PathParam("name") @ApiParam("Name of the logger")
            String name) {
        return loggersApiDelegate.getFilteredLoggers((Subject) request.getSession().getAttribute(BrokerAuthConstants
                                                                                                         .AUTHENTICATION_ID),
                                                     name);
    }

    @GET
    @Produces({"application/json"})
    @ApiOperation(value = "Get loggers", notes = "Gets metadata of all the loggers in the broker. ", response =
            LoggerMetadata.class, responseContainer = "List", authorizations = {
            @Authorization(value = "basicAuth")
    }, tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of loggers", response = LoggerMetadata.class, responseContainer
                    = "List"),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error
                    .class),
            @ApiResponse(code = 403, message = "Requested action unauthorized.", response = Error.class)})
    public Response getLoggers(@Context Request request) {
        return loggersApiDelegate.getLoggers((Subject) request.getSession().getAttribute(BrokerAuthConstants
                                                                                                 .AUTHENTICATION_ID));
    }

    @PUT
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Update logger", notes = "Update given logger", response = ResponseMessage.class,
            authorizations = {
                    @Authorization(value = "basicAuth")
            }, tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Loggers updated", response = ResponseMessage.class),
            @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = Error
                    .class),
            @ApiResponse(code = 403, message = "Requested action unauthorized.", response = Error.class),
            @ApiResponse(code = 404, message = "Logger not found", response = Error.class),
            @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not "
                                               + "supported format.", response = Error.class)})
    public Response updateLogger(@Context Request request, @Valid LoggerMetadata body) {
        return loggersApiDelegate.updateLogger((Subject) request.getSession().getAttribute(BrokerAuthConstants
                                                                                                   .AUTHENTICATION_ID),
                                               body);
    }
}