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

package io.ballerina.messaging.broker.amqp.rest;

import io.ballerina.messaging.broker.amqp.AmqpConnectionManager;
import io.ballerina.messaging.broker.amqp.codec.AmqpChannelView;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.amqp.rest.model.ChannelMetadata;
import io.ballerina.messaging.broker.amqp.rest.model.CloseConnectionResponse;
import io.ballerina.messaging.broker.amqp.rest.model.ConnectionMetadata;
import io.ballerina.messaging.broker.amqp.rest.model.RequestAcceptedResponse;
import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.authorization.AuthorizationHandler;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAuthScope;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.Subject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

/**
 * Delegate class that handles Connections REST API requests.
 */
public class ConnectionsApiDelegate {

    private final AuthorizationHandler authHandler;

    private final AmqpConnectionManager connectionManager;

    public ConnectionsApiDelegate(AmqpConnectionManager connectionManager, AuthorizationHandler authHandler) {
        this.authHandler = authHandler;
        this.connectionManager = connectionManager;
    }

    /**
     * Retrieves all active amqp connections established with the broker.
     *
     * @param subject The authentication subject containing user information of the user that has invoked the API
     * @return list of {@link ConnectionMetadata}
     */
    public Response getAllConnections(Subject subject) {
        try {
            authHandler.handle(ResourceAuthScope.CONNECTIONS_GET, subject);
            List<ConnectionMetadata> connections = new ArrayList<>();
            for (AmqpConnectionHandler connectionHandler : connectionManager.getConnections()) {
                connections.add(new ConnectionMetadata().id(connectionHandler.getId())
                                                        .remoteAddress(connectionHandler.getRemoteAddress())
                                                        .channelCount(connectionHandler.getChannelCount())
                                                        .connectedTime(connectionHandler.getConnectedTime()));
            }
            return Response.ok().entity(connections).build();
        } catch (AuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        }
    }

    /**
     * Forces disconnection of the AMQP connection.
     *
     * @param id      connection identifier
     * @param force   indicates if the connection should be closed forcefully or not
     * @param used    if set to true, the connection will be closed regardless of the number of active channels
     *                registered
     * @param subject authentication subject containing user information of the user that has invoked the API
     * @return HTTP/1.1 202 accepted with {@link RequestAcceptedResponse}
     */
    public Response closeConnection(int id, boolean force, boolean used, Subject subject) {
        try {
            authHandler.handle(ResourceAuthScope.CONNECTIONS_CLOSE, subject);
            int registeredChannelCount = connectionManager.closeConnection(id,
                                                                           force,
                                                                           used,
                                                                           "Connection close request received from "
                                                                           + "REST API with parameters "
                                                                           + "force=" + force + ", "
                                                                           + "used=" + used + "");
            return Response.accepted()
                           .entity(new CloseConnectionResponse().numberOfChannelsRegistered(registeredChannelCount))
                           .build();
        } catch (ResourceNotFoundException e) {
            throw new NotFoundException(e.getMessage(), e);
        } catch (AuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    /**
     * Forces disconnection of an AMQP channel.
     *
     * @param connectionId connection identifier
     * @param channelId    channel id
     * @param used         if set to true, the channel will be closed regardless of the number of active consumers
     *                     registered
     * @param subject      authentication subject containing user information of the user that has invoked the API
     * @return HTTP/1.1 202 accepted with {@link RequestAcceptedResponse}
     */
    public Response closeChannel(Integer connectionId, Integer channelId, boolean used, Subject subject) {
        try {
            authHandler.handle(ResourceAuthScope.CHANNEL_CLOSE, subject);
            connectionManager.closeChannel(connectionId, channelId, used, "Channel close request received from "
                                                                            + "REST API.");
            return Response.accepted()
                           .entity(new RequestAcceptedResponse().message(
                                   "Request accepted for forceful disconnection of channel " + channelId + " of "
                                   + "connection " + connectionId))
                           .build();
        } catch (ResourceNotFoundException e) {
            throw new NotFoundException(e.getMessage(), e);
        } catch (AuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves all active amqp channels created on a connection.
     *
     * @param subject The authentication subject containing user information of the user that has invoked the API
     * @return list of {@link ChannelMetadata}
     */
    public Response getAllChannels(Integer connectionId, Subject subject) {
        try {
            authHandler.handle(ResourceAuthScope.CHANNELS_GET, subject);
            List<ChannelMetadata> channels = new ArrayList<>();
            for (AmqpChannelView channel : connectionManager.getChannelViews(connectionId)) {
                channels.add(new ChannelMetadata().id(channel.getChannelId())
                                                  .consumerCount(channel.getConsumerCount())
                                                  .createdTime(channel.getCreatedTime())
                                                  .deliveryPendingMessageCount(channel.getDeliveryPendingMessageCount())
                                                  .isClosed(channel.isClosed())
                                                  .isFlowEnabled(channel.isFlowEnabled())
                                                  .prefetchCount(channel.getPrefetchCount())
                                                  .unackedMessageCount(channel.getUnackedMessageCount())
                                                  .transactionType(channel.getTransactionType()));
            }
            return Response.ok().entity(channels).build();
        } catch (AuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        } catch (ResourceNotFoundException e) {
            throw new NotFoundException(e.getMessage(), e);
        }
    }
}
