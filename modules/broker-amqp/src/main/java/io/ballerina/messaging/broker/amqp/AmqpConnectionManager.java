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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
     * Closes an AMQP connection specified by the identifier.
     *
     * @param id the connection identifier
     */
    public void forceDisconnect(int id) {
        AmqpConnectionHandler connectionHandler = connectionHandlers.get(id);
        if (Objects.nonNull(connectionHandler)) {
            connectionHandler.forceDisconnect();
        } else {
            throw new NoSuchElementException("Connection id " + id + " does not exist.");
        }
    }

    /**
     * Retrieves the AMQP channels that are created on a given connection.
     *
     * @return a list of {@link AmqpChannelView} representing AMQP channels
     */
    public Collection<AmqpChannelView> getChannelViews(int connectionId) {
        AmqpConnectionHandler connectionHandler;
        connectionHandler = connectionHandlers.get(connectionId);
        if (Objects.nonNull(connectionHandler)) {
            return connectionHandler.getChannelViews();
        } else {
            throw new NoSuchElementException("Connection id " + connectionId + " does not exist.");
        }
    }
}
