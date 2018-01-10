package org.wso2.broker.core.rest.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.rest.BrokerAdminService;
import org.wso2.broker.core.rest.QueuesApiDelegate;
import org.wso2.broker.core.rest.model.ConsumerMetadata;
import org.wso2.broker.core.rest.model.Error;
import org.wso2.broker.core.rest.model.QueueCreateRequest;
import org.wso2.broker.core.rest.model.QueueCreateResponse;
import org.wso2.broker.core.rest.model.QueueMetadata;

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
@Produces({"application/json"})
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJAXRSSpecServerCodegen",
                            date = "2018-01-15T16:26:37.345+05:30")
public class QueuesApi {

    private final QueuesApiDelegate delegate;

    public QueuesApi(Broker broker) {
        this.delegate = new QueuesApiDelegate(broker);
    }

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Creates a queue", notes = "", response = QueueCreateResponse.class, tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Queue created.", response = QueueCreateResponse.class),
            @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.",
                         response = Error.class),
            @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not "
                    + "supported format.", response = Error.class)})
    public Response createQueue(@Valid QueueCreateRequest body) {
        return delegate.createQueue(body);
    }

    @DELETE
    @Path("/{queueName}/consumers/{consumerId}")
    @Produces({"application/json"})
    @ApiOperation(value = "delete a consumer", notes = "Delete a specific consumer from a queue",
                  response = Void.class, tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consumer deleted", response = Void.class),
            @ApiResponse(code = 404, message = "Queue/Consumer not found", response = Error.class)})
    public Response deleteConsumer(
            @PathParam("queueName") @ApiParam("Name of the queue") String queueName,
            @PathParam("consumerId") @ApiParam("Unique consumer identifier") Integer consumerId) {
        return delegate.deleteConsumer(queueName, consumerId);
    }

    @DELETE
    @Path("/{queueName}")
    @Produces({"application/json"})
    @ApiOperation(value = "Delete the specified queue.", notes = "Delete the specified queue if the queue exists in "
            + "the broker and the query param properties ifUnused and ifEmpty are satisfied.",
                  response = Void.class, tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Queue deleted", response = Void.class),
            @ApiResponse(code = 400, message = "Bad request. Invalid request or validation error.",
                         response = Error.class),
            @ApiResponse(code = 404, message = "Queue not found", response = Error.class)})
    public Response deleteQueue(
            @PathParam("queueName") @ApiParam("Name of the queue") String queueName, @QueryParam("ifUnused")
    @ApiParam("If set to true, queue will be deleted only if the queue has no active consumers.") Boolean ifUnused,
             @QueryParam("ifEmpty") @ApiParam("If set to true, queue will be deleted only if the queue is empty.")
                    Boolean ifEmpty) {
        return delegate.deleteQueue(queueName, ifUnused, ifEmpty);
    }

    @GET
    @Path("/{queueName}/consumers")
    @Produces({"application/json"})
    @ApiOperation(value = "Get all consumers of a queue", notes = "Retrieves all the consumers for the queue",
                  response = ConsumerMetadata.class, responseContainer = "List", tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consumers of the queue", response = ConsumerMetadata.class,
                         responseContainer = "List"),
            @ApiResponse(code = 404, message = "Queue not found", response = Error.class)})
    public Response getAllConsumersForQueue(@PathParam("queueName") @ApiParam("Name of the queue") String queueName) {
        return delegate.getAllConsumers(queueName);
    }

    @GET
    @Produces({"application/json"})
    @ApiOperation(value = "Get all queues", notes = "Gets metadata of all the queues in the broker. This includes "
            + "durable  and non durable queues.  ", response = QueueMetadata.class, responseContainer = "List", tags
            = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of queues", response = QueueMetadata.class, responseContainer =
                    "List")})
    public Response getAllQueues(@QueryParam("durable") @ApiParam("filter queues by durability") Boolean durable) {
        return delegate.getAllQueues(durable);
    }

    @GET
    @Path("/{queueName}/consumers/{consumerId}")
    @Produces({"application/json"})
    @ApiOperation(value = "Get a specific consumer", notes = "Retrieves a specific consumer for a given queue",
                  response = ConsumerMetadata.class, tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Consumers of the queue", response = ConsumerMetadata.class),
            @ApiResponse(code = 404, message = "Queue/Consumer not found", response = Error.class)})
    public Response getConsumer(
            @PathParam("queueName") @ApiParam("Name of the queue") String queueName,
            @PathParam("consumerId") @ApiParam("Unique consumer identifier") Integer consumerId) {
        return delegate.getConsumer(queueName, consumerId);
    }

    @GET
    @Path("/{queueName}")
    @Produces({"application/json"})
    @ApiOperation(value = "Get a specific queue", notes = "Gets metadata of the specified queue.", response =
            QueueMetadata.class, tags = {})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Metadata of the queue", response = QueueMetadata.class),
            @ApiResponse(code = 404, message = "Queue not found", response = Error.class)})
    public Response getQueue(@PathParam("queueName") @ApiParam("Name of the queue") String queueName) {
        return delegate.getQueue(queueName);
    }
}
