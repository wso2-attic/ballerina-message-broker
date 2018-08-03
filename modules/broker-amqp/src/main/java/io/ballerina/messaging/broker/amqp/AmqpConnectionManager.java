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

package io.ballerina.messaging.broker.amqp;

import io.ballerina.messaging.broker.amqp.codec.AmqpChannelView;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AmqpConnectionManager stores the list of amqp connection established and manages those connections.
 */
public class AmqpConnectionManager {

    /**
     * List of {@link AmqpConnectionHandler} representing AMQP connections.
     */
    private final Map<Integer, AmqpConnectionHandler> connectionHandlers;

    AmqpConnectionManager() {
        connectionHandlers = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    /**
     * Adds a connection handler upon registration of an AMQP connection.
     *
     * @param handler {@link AmqpConnectionHandler} representing AMQP connections
     */
    public void addConnectionHandler(AmqpConnectionHandler handler) {
        connectionHandlers.put(handler.getId(), handler);
    }

    /**
     * Removes a connection handler upon closing of an AMQP connection.
     *
     * @param handler {@link AmqpConnectionHandler} representing AMQP connections
     */
    public void removeConnectionHandler(AmqpConnectionHandler handler) {
        connectionHandlers.remove(handler.getId());
    }

    /**
     * Retrieves the AMQP connections that are established.
     *
     * @return a list of {@link AmqpConnectionHandler} representing AMQP connections
     */
    public List<AmqpConnectionHandler> getConnections() {
        return new ArrayList<>(connectionHandlers.values());
    }

    /**
     * Closes an AMQP connection specified by the identifier according to the parameters set.
     *
     * @param id     the connection identifier
     * @param force  if set to true the connection will be closed by the broker without negotiating with the AMQP client
     * @param used   if set to true, the connection will be closed regardless of the number of active channels
     *               registered
     * @param reason reason to close connection  @return integer representing the number of channels registered
     * @throws ResourceNotFoundException if the the specified connection id does not exist
     * @throws ValidationException       if active channels exist for the connection, and if used is set to false
     */
    public int closeConnection(int id, boolean force, boolean used, String reason)
            throws ResourceNotFoundException, ValidationException {
        AmqpConnectionHandler connectionHandler = getConnection(id);
        return connectionHandler.closeConnection(reason, force, used);
    }

    /**
     * Closes an AMQP channel specified by the connection id and the channel id.
     *
     * @param connectionId unique integer representing the connection
     * @param channelId    integer representing the channel id within the connection id
     * @param used         if set to true, the channel will be closed regardless of the number of active consumers
     *                     registered
     * @param reason       reason to close channel
     */
    public void closeChannel(int connectionId, int channelId, boolean used, String reason)
            throws ResourceNotFoundException, ValidationException {
        AmqpConnectionHandler connectionHandler = getConnection(connectionId);
        if (Objects.nonNull(connectionHandler.getChannel(channelId))) {
            connectionHandler.closeChannel(channelId, used, reason);
        } else {
            throw new ResourceNotFoundException(
                    "Channel id " + channelId + " does not exist for connection " + connectionId);
        }
    }

    /**
     * Retrieves the AMQP channels that are created on a given connection.
     *
     * @return a list of {@link AmqpChannelView} representing AMQP channels
     */
    public Collection<AmqpChannelView> getChannelViews(int connectionId) throws ResourceNotFoundException {
        AmqpConnectionHandler connectionHandler = getConnection(connectionId);
        return connectionHandler.getChannelViews();
    }

    private AmqpConnectionHandler getConnection(int connectionId) throws ResourceNotFoundException {
        AmqpConnectionHandler connectionHandler = connectionHandlers.get(connectionId);
        if (Objects.nonNull(connectionHandler)) {
            return connectionHandler;
        } else {
            throw new ResourceNotFoundException("Connection id " + connectionId + " does not exist.");
        }
    }
}
