package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.util.function.ThrowingConsumer;

import java.util.Collection;
import javax.transaction.xa.Xid;

/**
 * Abstract class for QueueHandlerImpl Handler objects.
 */
public abstract class QueueHandler {

    public abstract Queue getUnmodifiableQueue();

    /**
     * Retrieve all the current consumers for the queue.
     *
     * @return Set of unmodifiable subscription objects
     */
    public abstract Collection<Consumer> getConsumers();

    /**
     * Add a new consumer to the queue.
     *
     * @param consumer {@link Consumer} implementation.
     * @return true if {@link Consumer} was successfully added.
     */
    abstract boolean addConsumer(Consumer consumer);

    /**
     * Remove consumer from the queue. NOTE: This method is synchronized with getting next subscriber for the queue to
     * avoid concurrent issues
     *
     * @param consumer {@link Consumer} to be removed.
     * @return True if the {@link Consumer} is removed.
     */
    abstract boolean removeConsumer(Consumer consumer);

    /**
     * Put the message to the tail of the queue. If the queue is full message will get dropped
     *
     * @param message {@link Message}
     */
    abstract void enqueue(Message message) throws BrokerException;

    abstract void prepareForEnqueue(Xid xid, Message message) throws BrokerException;

    abstract void prepareForDetach(Xid xid, DetachableMessage detachableMessage) throws BrokerException;

    public abstract void commit(Xid xid);

    public abstract void rollback(Xid xid);

    /**
     * Retrieves next available message for delivery. If the queue is empty, null is returned.
     *
     * @return Message
     */
    abstract Message takeForDelivery();

    /**
     * Removes the message from the queue.
     *
     * @param detachableMessage message to be removed.
     * @throws BrokerException throws on failure to dequeue the message.
     */
    abstract void dequeue(DetachableMessage detachableMessage) throws BrokerException;

    abstract void requeue(Message message) throws BrokerException;

    /**
     * Get the current consumer list iterator for the queue. This is a snapshot of the consumers at the time when the
     * when this method is invoked.
     *
     * @return CyclicConsumerIterator
     */
    abstract CyclicConsumerIterator getCyclicConsumerIterator();

    /**
     * True if there are no {@link Message} objects in the queue and false otherwise.
     *
     * @return True if the queue doesn't contain any {@link Message} objects
     */
    abstract boolean isEmpty();

    /**
     * Returns the number of {@link Message} objects in this queue.
     *
     * @return Number of {@link Message} objects in the queue.
     */
    public abstract int size();

    /**
     * True if there are no consumers and false otherwise.
     *
     * @return True if there are no {@link Consumer} for the queue.
     */
    abstract boolean isUnused();

    public abstract int consumerCount();

    abstract void addBinding(Binding binding, ThrowingConsumer<Binding, BrokerException> bindingChangeListener);

    abstract int releaseResources() throws BrokerException;

    abstract void removeBinding(Binding binding);

    abstract int purgeQueue() throws ValidationException;
}
