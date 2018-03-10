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
 */

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.auth.AuthManager;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceActions;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAuthScopes;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceTypes;
import io.ballerina.messaging.broker.auth.authorization.handler.AuthorizationHandler;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthException;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.core.transaction.DistributedTransaction;
import io.ballerina.messaging.broker.core.transaction.LocalTransaction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.transaction.xa.Xid;

/**
 * Broker APIs protected by authorization.
 */
public class SecureBrokerImpl implements Broker {

    private Broker broker;
    /**
     * The @{@link AuthorizationHandler} to handle authorization.
     */
    private final AuthorizationHandler authHandler;

    public SecureBrokerImpl(Broker broker, AuthManager authManager, Subject subject) {
        this.broker = broker;
        authHandler = new AuthorizationHandler(authManager, subject);
    }

    @Override
    public void publish(Message message) throws BrokerException {
        broker.publish(message);
    }

    @Override
    public void acknowledge(String queueName, Message message) throws BrokerException {
        broker.acknowledge(queueName, message);
    }

    @Override
    public Set<QueueHandler> enqueue(Xid xid, Message message) throws BrokerException {
        return broker.enqueue(xid, message);
    }

    @Override
    public QueueHandler dequeue(Xid xid, String queueName, Message message) throws BrokerException {
        return broker.dequeue(xid, queueName, message);
    }

    @Override
    public void addConsumer(Consumer consumer) throws BrokerException {
        broker.addConsumer(consumer);
    }

    @Override
    public void removeConsumer(Consumer consumer) {
        broker.removeConsumer(consumer);
    }

    @Override
    public void declareExchange(String exchangeName, String type, boolean passive, boolean durable) throws
            BrokerException, ValidationException {
        broker.declareExchange(exchangeName, type, passive, durable);
    }

    @Override
    public void createExchange(String exchangeName, String type, boolean durable) throws
            BrokerException, ValidationException {
        broker.createExchange(exchangeName, type, durable);
    }

    @Override
    public boolean deleteExchange(String exchangeName, boolean ifUnused) throws BrokerException, ValidationException {
        return broker.deleteExchange(exchangeName, ifUnused);
    }

    @Override
    public boolean createQueue(String queueName, boolean passive, boolean durable, boolean autoDelete)
            throws BrokerException, ValidationException, BrokerAuthException {
        authHandler.handle(ResourceAuthScopes.QUEUES_CREATE);
        boolean succeed = broker.createQueue(queueName, passive, durable, autoDelete);
        if (succeed) {
            authHandler.createAuthResource(ResourceTypes.QUEUE, queueName, durable);
        }
        return succeed;
    }

    @Override
    public int deleteQueue(String queueName, boolean ifUnused, boolean ifEmpty) throws BrokerException,
            ValidationException, ResourceNotFoundException, BrokerAuthException {
        authHandler.handle(ResourceTypes.QUEUE, queueName, ResourceActions.DELETE);
        authHandler.deleteAuthResource(ResourceTypes.QUEUE, queueName);
        return broker.deleteQueue(queueName, ifUnused, ifEmpty);
    }

    @Override
    public void bind(String queueName, String exchangeName, String routingKey, FieldTable arguments)
            throws BrokerException, ValidationException {
        broker.bind(queueName, exchangeName, routingKey, arguments);
    }

    @Override
    public void unbind(String queueName, String exchangeName, String routingKey) throws BrokerException,
            ValidationException {
        broker.unbind(queueName, exchangeName, routingKey);
    }

    @Override
    public void startMessageDelivery() {
        broker.startMessageDelivery();
    }

    @Override
    public int purgeQueue(String queueName) throws ResourceNotFoundException, ValidationException {
        return broker.purgeQueue(queueName);
    }

    @Override
    public void stopMessageDelivery() {
        broker.stopMessageDelivery();
    }

    @Override
    public void shutdown() {
        broker.shutdown();
    }

    @Override
    public void requeue(String queueName, Message message) throws BrokerException, ResourceNotFoundException {
        broker.requeue(queueName, message);
    }

    @Override
    public Collection<QueueHandler> getAllQueues() {
        return broker.getAllQueues();
    }

    @Override
    public QueueHandler getQueue(String queueName) {
        return broker.getQueue(queueName);
    }

    @Override
    public void moveToDlc(String queueName, Message message) throws BrokerException {
        broker.moveToDlc(queueName, message);
    }

    @Override
    public Collection<Exchange> getAllExchanges() {
        return broker.getAllExchanges();
    }

    @Override
    public Map<String, BindingSet> getAllBindingsForExchange(String exchangeName) throws ValidationException {
        return broker.getAllBindingsForExchange(exchangeName);
    }

    @Override
    public Exchange getExchange(String exchangeName) {
        return broker.getExchange(exchangeName);
    }

    @Override
    public LocalTransaction newLocalTransaction() {
        return broker.newLocalTransaction();
    }

    @Override
    public DistributedTransaction newDistributedTransaction() {
        return broker.newDistributedTransaction();
    }
}
