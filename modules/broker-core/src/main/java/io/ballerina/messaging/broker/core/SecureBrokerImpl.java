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

import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.auth.authorization.AuthorizationHandler;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAction;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceAuthScope;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceType;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.core.transaction.BrokerTransaction;
import io.ballerina.messaging.broker.core.transaction.SecureBrokerTransaction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.transaction.xa.Xid;

/**
 * Broker APIs protected by authorization.
 */
public class SecureBrokerImpl implements Broker {

    /**
     * Wrapper object of the {@link BrokerImpl}
     */
    private final Broker broker;
    /**
     * Username entity
     */
    private final Subject subject;
    /**
     * The @{@link AuthorizationHandler} to handle authorization.
     */
    private final AuthorizationHandler authHandler;

    SecureBrokerImpl(Broker broker, Subject subject, AuthorizationHandler authHandler) {
        this.broker = broker;
        this.subject = subject;
        this.authHandler = authHandler;
    }

    @Override
    public void publish(Message message) throws BrokerException {
        try {
            authHandler.handle(ResourceAuthScope.EXCHANGES_PUBLISH, ResourceType.EXCHANGE,
                               message.getMetadata().getExchangeName(), ResourceAction.PUBLISH, subject);
            broker.publish(message);
        } catch (AuthException e) {
            message.release();
            throw new BrokerAuthException(e.getMessage(), e);
        } catch (AuthNotFoundException e) {
            message.release();
            throw new BrokerAuthNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public void acknowledge(String queueName, DetachableMessage detachableMessage) throws BrokerException {
        broker.acknowledge(queueName, detachableMessage);
    }

    @Override
    public Set<QueueHandler> enqueue(Xid xid, Message message) throws BrokerException {
        return broker.enqueue(xid, message);
    }

    @Override
    public QueueHandler dequeue(Xid xid, String queueName, DetachableMessage detachableMessage) throws BrokerException {
        return broker.dequeue(xid, queueName, detachableMessage);
    }

    @Override
    public void addConsumer(Consumer consumer) throws BrokerException {
        try {
            authHandler.handle(ResourceAuthScope.QUEUES_CONSUME, ResourceType.QUEUE, consumer.getQueueName(),
                               ResourceAction.CONSUME, subject);
            broker.addConsumer(consumer);
        } catch (AuthException e) {
            throw new BrokerAuthException(e.getMessage(), e);
        } catch (AuthNotFoundException e) {
            throw new BrokerAuthNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public boolean removeConsumer(Consumer consumer) {
        return broker.removeConsumer(consumer);
    }

    @Override
    public void declareExchange(String exchangeName, String type, boolean passive, boolean durable)
            throws BrokerException, ValidationException {
        try {
            if (!passive) {
                authHandler.handle(ResourceAuthScope.EXCHANGES_CREATE, subject);
            }
            broker.declareExchange(exchangeName, type, passive, durable);
            if (!passive) {
                authHandler.createAuthResource(ResourceType.EXCHANGE, exchangeName, durable, subject);
            }
        } catch (AuthException e) {
            throw new BrokerAuthException(e.getMessage(), e);
        }
    }

    @Override
    public void createExchange(String exchangeName, String type, boolean durable)
            throws BrokerException, ValidationException {
        try {
            authHandler.handle(ResourceAuthScope.EXCHANGES_CREATE, subject);
            broker.createExchange(exchangeName, type, durable);
            authHandler.createAuthResource(ResourceType.EXCHANGE, exchangeName, durable, subject);
        } catch (AuthException e) {
            throw new BrokerAuthException(e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteExchange(String exchangeName, boolean ifUnused)
            throws BrokerException, ValidationException, ResourceNotFoundException {
        try {
            authHandler.handle(ResourceAuthScope.EXCHANGES_DELETE, ResourceType.EXCHANGE, exchangeName,
                               ResourceAction.DELETE, subject);
            boolean success = broker.deleteExchange(exchangeName, ifUnused);
            if (success) {
                authHandler.deleteAuthResource(ResourceType.EXCHANGE, exchangeName);
            }
            return success;
        } catch (AuthException e) {
            throw new BrokerAuthException(e.getMessage(), e);
        } catch (AuthNotFoundException e) {
            throw new BrokerAuthNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public boolean createQueue(String queueName, boolean passive, boolean durable, boolean autoDelete)
            throws BrokerException, ValidationException {
        try {
            if (!queueExists(queueName) && !passive) {
                authHandler.handle(ResourceAuthScope.QUEUES_CREATE, subject);
            }
            boolean succeed = broker.createQueue(queueName, passive, durable, autoDelete);
            if (succeed) {
                authHandler.createAuthResource(ResourceType.QUEUE, queueName, durable, subject);
            }
            return succeed;
        } catch (AuthException e) {
            throw new BrokerAuthException(e.getMessage(), e);
        }
    }

    @Override
    public int deleteQueue(String queueName, boolean ifUnused, boolean ifEmpty)
            throws BrokerException, ValidationException, ResourceNotFoundException {

        if (!queueExists(queueName)) {
            throw new ResourceNotFoundException("Queue [ " + queueName + " ] Not found");
        }

        try {
            authHandler.handle(ResourceAuthScope.QUEUES_DELETE, ResourceType.QUEUE, queueName,
                               ResourceAction.DELETE, subject);
            int messageCount = broker.deleteQueue(queueName, ifUnused, ifEmpty);
            authHandler.deleteAuthResource(ResourceType.QUEUE, queueName);
            return messageCount;
        } catch (AuthException e) {
            throw new BrokerAuthException(e.getMessage(), e);
        } catch (AuthNotFoundException e) {
            throw new BrokerAuthNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public boolean queueExists(String queueName) {
        return broker.queueExists(queueName);
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
    public Collection<QueueHandler> getAllQueues() throws BrokerException {
        try {
            authHandler.handle(ResourceAuthScope.QUEUES_GET, subject);
            return broker.getAllQueues();
        } catch (AuthException e) {
            throw new BrokerAuthException(e.getMessage(), e);
        }
    }

    @Override
    public QueueHandler getQueue(String queueName) throws BrokerException, ResourceNotFoundException {

        if (!broker.queueExists(queueName)) {
            throw new ResourceNotFoundException("Queue [ " + queueName + " ] Not found");
        }
        try {
            authHandler.handle(ResourceAuthScope.QUEUES_GET, ResourceType.QUEUE, queueName,
                               ResourceAction.GET, subject);
            return broker.getQueue(queueName);
        } catch (AuthException e) {
            throw new BrokerAuthException(e.getMessage(), e);
        } catch (AuthNotFoundException e) {
            throw new BrokerAuthNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public void moveToDlc(String queueName, Message message) throws BrokerException {
        broker.moveToDlc(queueName, message);
    }

    @Override
    public Collection<Exchange> getAllExchanges() throws BrokerException {
        try {
            authHandler.handle(ResourceAuthScope.SCOPES_GET, subject);
            return broker.getAllExchanges();
        } catch (AuthException e) {
            throw new BrokerAuthException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, BindingSet> getAllBindingsForExchange(String exchangeName) throws ValidationException {
        return broker.getAllBindingsForExchange(exchangeName);
    }

    @Override
    public Exchange getExchange(String exchangeName) throws BrokerException {
        try {
            authHandler.handle(ResourceAuthScope.EXCHANGES_GET, ResourceType.EXCHANGE, exchangeName,
                               ResourceAction.GET, subject);
            return broker.getExchange(exchangeName);
        } catch (AuthException e) {
            throw new BrokerAuthException(e.getMessage(), e);
        } catch (AuthNotFoundException e) {
            throw new BrokerAuthNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public BrokerTransaction newLocalTransaction() {
        return new SecureBrokerTransaction(broker.newLocalTransaction(), subject, authHandler);
    }

    @Override
    public BrokerTransaction newDistributedTransaction() {
        return new SecureBrokerTransaction(broker.newDistributedTransaction(), subject, authHandler);
    }

    @Override
    public Set<QueueHandler> restoreDtxPreparedMessages(Xid xid, Collection<Message> messages) throws BrokerException {
        return broker.restoreDtxPreparedMessages(xid, messages);
    }
}
