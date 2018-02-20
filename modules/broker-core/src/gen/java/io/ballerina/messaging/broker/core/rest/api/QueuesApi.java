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

import io.ballerina.messaging.broker.core.rest.model.QueueCreateRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.rest.BindingsApiDelegate;
import io.ballerina.messaging.broker.core.rest.BrokerAdminService;
import io.ballerina.messaging.broker.core.rest.ConsumersApiDelegate;
import io.ballerina.messaging.broker.core.rest.QueuesApiDelegate;
import io.ballerina.messaging.broker.core.rest.model.BindingCreateRequest;
import io.ballerina.messaging.broker.core.rest.model.BindingCreateResponse;
import io.ballerina.messaging.broker.core.rest.model.BindingInfo;
import io.ballerina.messaging.broker.core.rest.model.ConsumerMetadata;
import io.ballerina.messaging.broker.core.rest.model.Error;
import io.ballerina.messaging.broker.core.rest.model.QueueCreateResponse;
import io.ballerina.messaging.broker.core.rest.model.QueueMetadata;

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

@Path(BrokerAdminService.API_BASE_PATH + "/queues")
@Api(description = "the queues API")
@Produces({ "application/json" })
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2018-02-16T16:43:30.881+05:30")
public class QueuesApi {

    private final QueuesApiDelegate queuesApiDelegate;

    private final ConsumersApiDelegate consumersApiDelegate;

    private final BindingsApiDelegate bindingsApiDelegate;

    public QueuesApi(Broker broker) {
        this.queuesApiDelegate = new QueuesApiDelegate(broker);
        this.consumersApiDelegate = new ConsumersApiDelegate(broker);
        this.bindingsApiDelegate = new BindingsApiDelegate(broker);
    }

    @POST
    @Path("/{name}/bindings")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a binding", notes = "Create a binding for a queue", response = BindingCreateResponse.class, authorizations = {
            @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Binding created", response = BindingCreateResponse.class),
            @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = Error.class),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error.class),
            @ApiResponse(code = 404, message = "Exchange not found", response = Error.class),
            @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format.", response = Error.class) })
    public Response createBinding(@PathParam("name") @ApiParam("Name of the queue to bind to") String name,@Valid BindingCreateRequest body) {
        return bindingsApiDelegate.createBinding(name, body);
    }

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Creates a queue", notes = "", response = QueueCreateResponse.class, authorizations = {
            @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Queue created.", response = QueueCreateResponse.class),
            @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = Error.class),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error.class),
            @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format.", response = Error.class) })
    public Response createQueue(@Valid QueueCreateRequest body) {
        return queuesApiDelegate.createQueue(body);
    }

    @DELETE
    @Path("/{name}/bindings/{bindingPattern}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Unbind a queue", notes = "Delete a specific binding", response = Void.class, authorizations = {
            @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Binding deleted", response = Void.class),
            @ApiResponse(code = 400, message = "Bad request. Invalid request or validation error.", response = Error.class),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error.class),
            @ApiResponse(code = 404, message = "Binding not found", response = Error.class) })
    public Response deleteBinding(@PathParam("name") @ApiParam("Name of the queue") String name,@PathParam("bindingPattern") @ApiParam("Binding pattern for the bindings") String bindingPattern,@QueryParam("filterExpression")   @ApiParam("JMS selector relater message filter pattern")  String filterExpression) {
        return bindingsApiDelegate.deleteBinding(name, bindingPattern, filterExpression);
    }

    @DELETE
    @Path("/{name}/consumers/{consumerId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "delete a consumer", notes = "Delete a specific consumer from a queue", response = Void.class, authorizations = {
            @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consumer deleted", response = Void.class),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error.class),
            @ApiResponse(code = 404, message = "Queue/Consumer not found", response = Error.class) })
    public Response deleteConsumer(@PathParam("name") @ApiParam("Name of the queue") String name,@PathParam("consumerId") @ApiParam("Unique consumer identifier") Integer consumerId) {
        return consumersApiDelegate.deleteConsumer(name, consumerId);
    }

    @DELETE
    @Path("/{name}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete the specified queue.", notes = "Delete the specified queue if the queue exists in the broker and the query param properties ifUnused and ifEmpty are satisfied.", response = Void.class, authorizations = {
            @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Queue deleted", response = Void.class),
            @ApiResponse(code = 400, message = "Bad request. Invalid request or validation error.", response = Error.class),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error.class),
            @ApiResponse(code = 404, message = "Queue not found", response = Error.class) })
    public Response deleteQueue(@PathParam("name") @ApiParam("Name of the queue") String name, @DefaultValue("true")  @QueryParam("ifUnused")  @ApiParam("If set to true, queue will be deleted only if the queue has no active consumers.")  Boolean ifUnused,  @DefaultValue("true") @QueryParam("ifEmpty") @ApiParam("If set to true, queue will be deleted only if the queue is empty.")  Boolean ifEmpty) {
        return queuesApiDelegate.deleteQueue(name, ifUnused, ifEmpty);
    }

    @GET
    @Path("/{name}/consumers")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all consumers of a queue", notes = "Retrieves all the consumers for the queue", response = ConsumerMetadata.class, responseContainer = "List", authorizations = {
            @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consumers of the queue", response = ConsumerMetadata.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error.class),
            @ApiResponse(code = 404, message = "Queue not found", response = Error.class) })
    public Response getAllConsumersForQueue(@PathParam("name") @ApiParam("Name of the queue") String name) {
        return consumersApiDelegate.getAllConsumers(name);
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all queues", notes = "Gets metadata of all the queues in the broker. This includes durable and non durable queues. ", response = QueueMetadata.class, responseContainer = "List", authorizations = {
            @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of queues", response = QueueMetadata.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error.class) })
    public Response getAllQueues(@QueryParam("durable")   @ApiParam("filter queues by durability")  Boolean durable) {
        return queuesApiDelegate.getAllQueues(durable);
    }

    @GET
    @Path("/{name}/bindings/{bindingPattern}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve bindings for a queue with specific binding pattern", notes = "", response = BindingInfo.class, authorizations = {
            @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Binding info", response = BindingInfo.class),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error.class),
            @ApiResponse(code = 404, message = "Exchange not found", response = Error.class) })
    public Response getBinding(@PathParam("name") @ApiParam("Name of the queue") String name,@PathParam("bindingPattern") @ApiParam("Binding pattern for the bindings") String bindingPattern,@QueryParam("filterExpression")   @ApiParam("JMS selector relater message filter pattern")  String filterExpression) {
        return bindingsApiDelegate.getBinding(name, bindingPattern, filterExpression);
    }

    @GET
    @Path("/{name}/consumers/{consumerId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a specific consumer", notes = "Retrieves a specific consumer for a given queue", response = ConsumerMetadata.class, authorizations = {
            @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consumers of the queue", response = ConsumerMetadata.class),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error.class),
            @ApiResponse(code = 404, message = "Queue/Consumer not found", response = Error.class) })
    public Response getConsumer(@PathParam("name") @ApiParam("Name of the queue") String name,@PathParam("consumerId") @ApiParam("Unique consumer identifier") Integer consumerId) {
        return consumersApiDelegate.getConsumer(name, consumerId);
    }

    @GET
    @Path("/{name}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a specific queue", notes = "Gets metadata of the specified queue.", response = QueueMetadata.class, authorizations = {
            @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Metadata of the queue", response = QueueMetadata.class),
            @ApiResponse(code = 401, message = "Authentication information is missing or invalid", response = Error.class),
            @ApiResponse(code = 404, message = "Queue not found", response = Error.class) })
    public Response getQueue(@PathParam("name") @ApiParam("Name of the queue") String name) {
        return queuesApiDelegate.getQueue(name);
    }
}
