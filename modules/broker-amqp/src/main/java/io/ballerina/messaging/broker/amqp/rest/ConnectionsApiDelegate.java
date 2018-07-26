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
import io.ballerina.messaging.broker.amqp.rest.model.ConnectionCloseResponse;
import io.ballerina.messaging.broker.amqp.rest.model.ConnectionMetadata;
import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.authorization.AuthorizationHandler;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAuthScope;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.security.auth.Subject;
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
     * @param subject authentication subject containing user information of the user that has invoked the API
     * @return HTTP/1.1 202 accepted with {@link ConnectionCloseResponse}
     */
    public Response closeConnection(int id, Subject subject) {
        try {
            authHandler.handle(ResourceAuthScope.CONNECTIONS_CLOSE, subject);
            connectionManager.forceDisconnect(id);
            return Response.accepted().entity(
                    new ConnectionCloseResponse().message("Forceful disconnection of connection " + id + " accepted."))
                           .build();
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage(), e);
        } catch (AuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
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
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage(), e);
        }
    }
}
