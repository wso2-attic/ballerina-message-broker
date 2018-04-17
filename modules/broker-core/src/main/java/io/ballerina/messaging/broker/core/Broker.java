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

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.core.transaction.BrokerTransaction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.transaction.xa.Xid;

/**
 * Interface represents broker APIs
 */
public interface Broker {

    /**
     * In memory message id.
     */
    UniqueIdGenerator MESSAGE_ID_GENERATOR = new UniqueIdGenerator();

    /**
     * Publish message to queue(s).
     *
     * @param message message enqueue message
     * @throws BrokerException if publishing failed
     */
    void publish(Message message) throws BrokerException;

    /**
     * Acknowledge single or a given set of messages. Removes the message from underlying queue.
     *
     * @param queueName name of the queue the relevant messages belongs to
     * @param detachableMessage   detachable message
     * @throws BrokerException if an internal error occurred
     */
    void acknowledge(String queueName, DetachableMessage detachableMessage) throws BrokerException;

    /**
     * Preparing transaction to enqueue messages.
     *
     * @param xid     id of the distributed transaction
     * @param message publish message
     * @return set of subscriptions of the queue
     * @throws BrokerException if message enqueue to unknown exchange
     */
    Set<QueueHandler> enqueue(Xid xid, Message message) throws BrokerException;

    /**
     * Preparing transaction to dequeue messages
     *
     * @param xid       id of the distributed transaction
     * @param queueName name of the queue the  relevant message belongs to
     * @param detachableMessage   name of the queue the  relevant message belongs to
     * @return subscription of the queue
     * @throws BrokerException if  an internal error occurred
     */
    QueueHandler dequeue(Xid xid, String queueName, DetachableMessage detachableMessage) throws BrokerException;

    /**
     * Adds a consumer for a queue. Queue will be the queue returned from {@link Consumer#getQueueName()}
     *
     * @param consumer {@link Consumer} implementation
     * @throws BrokerException {@link BrokerException} if unable to add the consumer
     */
    void addConsumer(Consumer consumer) throws BrokerException;

    /**
     * Delete consumer
     *
     * @param consumer consumer to delete
     * @return True if the undelying queue is deleted, false otherwise.
     */
    boolean removeConsumer(Consumer consumer);

    /**
     * Create exchange if not exist
     *
     * @param exchangeName exchange name
     * @param type         exchange type
     * @param passive      do not create exchange
     * @param durable      request a durable exchange
     * @throws BrokerException     if unable to add exchange
     * @throws ValidationException if exchange name is empty
     */
    void declareExchange(String exchangeName, String type, boolean passive, boolean durable)
            throws BrokerException, ValidationException;

    /**
     * Create an exchange with given parameters
     *
     * @param exchangeName exchange name
     * @param type         exchange type
     * @param durable      exchange durability
     * @throws BrokerException     if there is an internal error when creating the exchange
     * @throws ValidationException if exchange already exist
     */
    void createExchange(String exchangeName, String type, boolean durable) throws BrokerException,
            ValidationException;

    /**
     * Delete given exchange
     *
     * @param exchangeName exchange name
     * @param ifUnused     exchange has no bindings
     * @return successfully deleted or not
     * @throws BrokerException     if exchange has bindings
     * @throws ValidationException if exchange is built-in
     */
    boolean deleteExchange(String exchangeName, boolean ifUnused)
            throws BrokerException, ValidationException, ResourceNotFoundException;

    /**
     * Create given queue
     *
     * @param queueName  name of the underlying queue
     * @param passive    do not create queue
     * @param durable    request a durable queue
     * @param autoDelete auto-delete queue when unused
     * @return successfully created or not
     * @throws BrokerException     if existing queue does not match parameters or
     *                             could not found when passive set to true
     * @throws ValidationException if queue binding already exist
     */
    boolean createQueue(String queueName, boolean passive, boolean durable, boolean autoDelete)
            throws BrokerException, ValidationException;

    /**
     * Delete given queue
     *
     * @param queueName queue name
     * @param ifUnused  queue has consumers
     * @param ifEmpty   queue has messages
     * @return successfully deleted or not
     * @throws BrokerException             if an internal error occurred
     * @throws ValidationException         if not empty and the ifEmpty parameter is set
     */
    int deleteQueue(String queueName, boolean ifUnused, boolean ifEmpty)
            throws BrokerException, ValidationException, ResourceNotFoundException;

    /**
     * Check if a queue exists with the given name.
     *
     * @param queueName name of the queue
     * @return true if queue exists, false otherwise
     */
    boolean queueExists(String queueName);

    /**
     * Bind queue to exchange
     *
     * @param queueName    queue name
     * @param exchangeName exchange name
     * @param routingKey   message routing key
     * @param arguments    arguments for declaration
     * @throws BrokerException     if an internal error occurred
     * @throws ValidationException if unknown queue name or exchange name
     */
    void bind(String queueName, String exchangeName,
              String routingKey, FieldTable arguments) throws BrokerException, ValidationException;

    /**
     * Unbind queue from exchange
     *
     * @param queueName    queue name
     * @param exchangeName exchange name
     * @param routingKey   message routing key
     * @throws BrokerException     if an internal error occurred
     * @throws ValidationException if unknown qeueu name or exchange name
     */
    void unbind(String queueName, String exchangeName, String routingKey)
            throws BrokerException, ValidationException;

    /**
     * Send a signal to start delivering messages to consumers
     */
    void startMessageDelivery();

    /**
     * Purge all messages in the queue.
     *
     * @param queueName name of the queue
     * @return number of messages purged
     * @throws ResourceNotFoundException if the queue is not found
     * @throws ValidationException       if there are online consumers for queue
     */
    int purgeQueue(String queueName) throws ResourceNotFoundException, ValidationException;

    /**
     * Stop delivery task
     */
    void stopMessageDelivery();

    /**
     * Retrieve given queue
     *
     * @param queueName queue name
     * @return selected queue
     */
    QueueHandler getQueue(String queueName) throws BrokerException, ResourceNotFoundException;

    /**
     * Generate unique message id
     *
     * @return a message id
     */
    static long getNextMessageId() {
        return MESSAGE_ID_GENERATOR.getNextId();
    }

    /**
     * Requeue a given message
     *
     * @param queueName queue name
     * @param message   message to requeue
     * @throws BrokerException if an internal error occurred
     */
    void requeue(String queueName, Message message) throws BrokerException, ResourceNotFoundException;

    /**
     * Retrieve all queues
     *
     * @return collection of queue
     */
    Collection<QueueHandler> getAllQueues() throws BrokerException;

    /**
     * Send a signal to stop delivering messages to consumers
     */
    void shutdown();

    /**
     * Retrieve all exchanges
     *
     * @return collection of exchange
     */
    Collection<Exchange> getAllExchanges() throws BrokerException;

    /**
     * Move given message to dead letter channel
     *
     * @param queueName queue name
     * @param message   message to move to dead letter channel
     * @throws BrokerException if an internal error occurred
     */
    void moveToDlc(String queueName, Message message) throws BrokerException;

    /**
     * Retrieve all bindings for  given exchange
     *
     * @param exchangeName exchange name
     * @return Collection of binding
     * @throws ValidationException if non existing exchange
     */
    Map<String, BindingSet> getAllBindingsForExchange(String exchangeName) throws ValidationException;

    /**
     * Retrieve given exchange
     *
     * @param exchangeName exchange name
     * @return selected exchange
     */
    Exchange getExchange(String exchangeName) throws BrokerException;

    /**
     * Start local transaction flow
     *
     * @return local transactional object
     */
    BrokerTransaction newLocalTransaction();

    /**
     * Start distributed transaction flow.
     *
     * @return a new DistributedTransaction object
     */
    BrokerTransaction newDistributedTransaction();

    /**
     * Restore enqueued data that is already in prepared state.
     * @param xid
     * @param messages
     */
    Set<QueueHandler> restoreDtxPreparedMessages(Xid xid, Collection<Message> messages) throws BrokerException;
}
