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

package org.wso2.broker.core.rest.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.rest.BindingsApiDelegate;
import org.wso2.broker.core.rest.BrokerAdminService;
import org.wso2.broker.core.rest.ExchangesApiDelegate;
import org.wso2.broker.core.rest.model.BindingSetInfo;
import org.wso2.broker.core.rest.model.Error;
import org.wso2.broker.core.rest.model.ExchangeCreateRequest;
import org.wso2.broker.core.rest.model.ExchangeCreateResponse;
import org.wso2.broker.core.rest.model.ExchangeMetadata;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path(BrokerAdminService.API_BASE_PATH + "/exchanges")
@Api(description = "the exchanges API")
@Produces({ "application/json" })
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2018-01-30T10:20:21.038+05:30")
public class ExchangesApi {

    private final ExchangesApiDelegate exchangesApiDelegate;
    private final BindingsApiDelegate bindingsApiDelegate;

    public ExchangesApi(Broker broker) {
        this.exchangesApiDelegate = new ExchangesApiDelegate(broker);
        this.bindingsApiDelegate = new BindingsApiDelegate(broker);
    }

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create an exchange", notes = "", response = ExchangeCreateResponse.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Exchange created", response = ExchangeCreateResponse.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = Error.class),
        @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format.", response = Error.class) })
    public Response createExchange(@Valid ExchangeCreateRequest body) {
        return exchangesApiDelegate.createExchange(body);
    }

    @DELETE
    @Path("/{name}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete exchnage", notes = "Delete the exchange with the specified exchange name", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Exchange deleted", response = Void.class),
        @ApiResponse(code = 400, message = "Bad request. Invalid request or validation error.", response = Error.class),
        @ApiResponse(code = 404, message = "Exchange not found", response = Error.class) })
    public Response deleteExchange(@PathParam("name") @ApiParam("Name of the exchange.") String name,
                                   @DefaultValue("true") @QueryParam("ifUnused")    @ApiParam("Delete if the exchange has no bindings.")  Boolean ifUnused) {
        return exchangesApiDelegate.deleteExchange(name, ifUnused);
    }

    @GET
    @Path("/{name}/bindings")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get bindings of a exchange", notes = "Retrieves the bindings set of the exchange", response = BindingSetInfo.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of Bindings", response = BindingSetInfo.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Exchange not found", response = Error.class) })
    public Response getAllBindingsForExchange(@PathParam("name") @ApiParam("Name of the exchange.") String name) {
        return bindingsApiDelegate.getAllBindingsForExchange(name);
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all exchanges", notes = "Retrieves all the exchanges in the broker", response = ExchangeMetadata.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of exchanges", response = ExchangeMetadata.class, responseContainer = "List") })
    public Response getAllExchanges() {
        return exchangesApiDelegate.getAllExchanges();
    }

    @GET
    @Path("/{name}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a specific exchange", notes = "Retrieves the exchange metadata for the specific exchange", response = ExchangeMetadata.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Metadata of the exchange", response = ExchangeMetadata.class),
        @ApiResponse(code = 404, message = "Exchange not found", response = Error.class) })
    public Response getExchange(@PathParam("name") @ApiParam("Name of the exchange.") String name) {
        return exchangesApiDelegate.getExchange(name);
    }
}
