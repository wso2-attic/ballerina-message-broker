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
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.amqp.rest.model.ConnectionMetadata;
import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.authorization.AuthorizationHandler;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAuthScope;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.Subject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;

/**
 * Delegate class that handles Connections REST API requests.
 */
public class ConnectionsApiDelegate {

    private final AuthorizationHandler authHandler;

    private AmqpConnectionManager connectionManager;

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
}
