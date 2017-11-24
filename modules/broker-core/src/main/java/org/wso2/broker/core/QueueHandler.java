/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.core;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents the queue of the broker. Contains a bounded queue to store messages. Subscriptions for the queue
 * are maintained as an in-memory set.
 */
final class QueueHandler {

    private static final Logger log = LoggerFactory.getLogger(QueueHandler.class);

    private final String name;

    private final LinkedBlockingQueue<Message> messageQueue;

    private final CyclicConsumerIterator consumerIterator;

    private final Set<Consumer> consumers;

    private final boolean durable;

    private final boolean autoDelete;

    private final Map<Long, Message> pendingMessages;

    QueueHandler(String name, boolean durable, boolean autoDelete, int capacity) {
        this.name = name;
        this.durable = durable;
        this.autoDelete = autoDelete;
        this.messageQueue = new LinkedBlockingQueue<>(capacity);
        this.consumers = new ConcurrentSkipListSet<>();
        consumerIterator = new CyclicConsumerIterator();
        pendingMessages = new ConcurrentHashMap<>();
    }

    /**
     * Name of the underlying queue
     *
     * @return String representation of the underlying queue name
     */
    String getName() {
        return name;
    }

    /**
     * Retrieve all the current consumers for the queue
     *
     * @return Set of unmodifiable subscription objects
     */
    Collection<Consumer> getConsumers() {
        return Collections.unmodifiableCollection(consumers);
    }

    /**
     * Add a new consumer to the queue
     *
     * @param consumer {@link Consumer} implementation
     */
    void addConsumer(Consumer consumer) {
        consumers.add(consumer);
    }

    /**
     * Remove consumer from the queue.
     * NOTE: This method is synchronised with getting next subscriber for the queue to avoid concurrent issues
     *
     * @param consumer {@link Consumer} to be removed
     */
    void removeConsumer(Consumer consumer) {
        consumers.remove(consumer);
    }

    /**
     * Put the message to the tail of the queue. If the queue is full returns falls.
     * <p>
     * Note: The caller should handle the message queue full scenario
     *
     * @param message {@link Message}
     * @return True if successfully enqueued, false otherwise
     */
    boolean enqueue(Message message) {
        return messageQueue.offer(message);
    }

    /**
     * Retrieves and removes the head of this queue, or returns null if this queue is empty.
     *
     * @return Message
     */
    Message dequeue() {
        Message message = messageQueue.poll();
        if (message != null) {
            pendingMessages.put(message.getMetadata().getMessageId(), message);
        }
        return message;
    }

    void acknowledge(long messageId, boolean multiple) {
        // TODO handle nacks
        // TODO handle multiple
        pendingMessages.remove(messageId);
    }

    /**
     * Get the current consumer list iterator for the queue. This is a snapshot of the consumers at the time
     * when the when this method is invoked
     *
     * @return CyclicConsumerIterator
     */
    CyclicConsumerIterator getCyclicConsumerIterator() {
        consumerIterator.setIterator(Iterables.cycle(consumers).iterator());
        return consumerIterator;
    }

    /**
     * True if there are no {@link Message} objects in the queue and false otherwise
     *
     * @return True if the queue doesn't contain any {@link Message} objects
     */
    boolean isEmpty() {
        return messageQueue.isEmpty();
    }

    /**
     * Returns the number of {@link Message} objects in this queue.
     *
     * @return Number of {@link Message} objects in the queue.
     */
    int size() {
        return messageQueue.size();
    }

    /**
     * True if there are no consumers and false otherwise
     *
     * @return True if there are no {@link Consumer} for the queue.
     */
    boolean isUnused() {
        return consumers.isEmpty();
    }

    /**
     * If true the queue will be durable. Durable queues remain active when the broker restarts
     * Non­durable queues (transient queues) are purged if/when the broker restarts
     *
     * @return True if the queue is durable. False otherwise
     */
    public boolean isDurable() {
        return durable;
    }

    /**
     * If true queue can be deleted once there are no consumers for the queue
     *
     * @return True if the queue is auto deletable
     */
    public boolean isAutoDelete() {
        return autoDelete;
    }

    void closeAllConsumers() {
        Iterator<Consumer> iterator = consumers.iterator();
        while (iterator.hasNext()) {

            Consumer consumer = iterator.next();
            try {
                consumer.close();
            } catch (BrokerException e) {
                log.error("Error occurred while closing the consumer [ " + consumer + " ] " +
                        "for queue [ " + name + " ]", e);
            } finally {
                iterator.remove();
            }
        }
    }
}
