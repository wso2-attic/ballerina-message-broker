package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;

import java.util.Collection;

/**
 * Abstract class for Queue Registry objects.
 */
public abstract class QueueRegistry {

    abstract QueueHandler getQueueHandler(String queueName);

    abstract boolean addQueue(String queueName, boolean passive, boolean durable, boolean autoDelete,
                              FieldTable arguments)
            throws BrokerException;

    abstract int removeQueue(String queueName, boolean ifUnused, boolean ifEmpty) throws BrokerException,
            ValidationException,
            ResourceNotFoundException;

    abstract Collection<QueueHandler> getAllQueues();

    /**
     * Method to reload queues on becoming the active node.
     *
     * @throws BrokerException if an error occurs loading messages from the database
     */
    abstract void reloadQueuesOnBecomingActive() throws BrokerException;
}
