/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.amqp.rest.api;

import io.ballerina.messaging.broker.amqp.AmqpConnectionManager;
import io.ballerina.messaging.broker.amqp.rest.ConnectionsApiDelegate;
import io.ballerina.messaging.broker.amqp.rest.model.ConnectionMetadata;
import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
import io.ballerina.messaging.broker.auth.authorization.AuthorizationHandler;
import io.ballerina.messaging.broker.core.rest.BrokerAdminService;
import io.ballerina.messaging.broker.core.rest.model.Error;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.wso2.msf4j.Request;
import javax.security.auth.Subject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path(BrokerAdminService.API_BASE_PATH + "/connections")
@Api(description = "the connections API")
@Produces({ "application/json" })
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2018-06-26T13:14:19.513+05:30")
public class ConnectionsApi {

    private final ConnectionsApiDelegate connectionsApiDelegate;

    public ConnectionsApi(AmqpConnectionManager connectionManager, AuthorizationHandler dacHandler) {
        this.connectionsApiDelegate = new ConnectionsApiDelegate(connectionManager, dacHandler);
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all connections", notes = "Retrieves all connections to the broker", response = ConnectionMetadata.class, responseContainer = "List", authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "List of active Connections", response = ConnectionMetadata.class, responseContainer = "List"),
        @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error.class)
    })
    public Response getAllConnections(@Context Request request) {
        return connectionsApiDelegate.getAllConnections((Subject) request.getSession().getAttribute(BrokerAuthConstants.AUTHENTICATION_ID));
    }
}
