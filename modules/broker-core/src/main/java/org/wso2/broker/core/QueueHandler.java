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
import org.wso2.broker.core.store.dao.MessageDao;
import org.wso2.broker.core.store.dao.QueueDao;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents the queue of the broker. Contains a bounded queue to store messages. Subscriptions for the queue
 * are maintained as an in-memory set.
 */
final class QueueHandler {

    private static final Logger log = LoggerFactory.getLogger(QueueHandler.class);

    private Queue queue;
      
    private final CyclicConsumerIterator consumerIterator;

    private final Set<Consumer> consumers;

    private QueueHandler(Queue queue) {
       
        this.queue = queue;
        this.consumers = ConcurrentHashMap.newKeySet();
        consumerIterator = new CyclicConsumerIterator();
    }

    public static QueueHandler createNonDurableQueue(String queueName, int capacity, boolean autoDelete) {
        Queue queue = new MemQueueImpl(queueName, capacity, autoDelete);
        return new QueueHandler(queue);
    }

    public static QueueHandler createDurableQueue(String queueName, MessageDao messageDao,
                                                  QueueDao queueDao, boolean autoDelete) {
        Queue queue = new DbBackedQueueImpl(queueName, messageDao, autoDelete);
        queueDao.persist(queue);
        return new QueueHandler(queue);
    }

    Queue getQueue() {
        return queue;
    }

    /**
     * Retrieve all the current consumers for the queue.
     *
     * @return Set of unmodifiable subscription objects
     */
    Collection<Consumer> getConsumers() {
        return Collections.unmodifiableCollection(consumers);
    }

    /**
     * Add a new consumer to the queue.
     *
     * @param consumer {@link Consumer} implementation
     */
    void addConsumer(Consumer consumer) {
        consumers.add(consumer);
    }

    /**
     * Remove consumer from the queue.
     * NOTE: This method is synchronized with getting next subscriber for the queue to avoid concurrent issues
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
        return queue.enqueue(message);
    }

    /**
     * Retrieves and removes the head of this queue, or returns null if this queue is empty.
     *
     * @return Message
     */
    Message dequeue() {
       
        Message message = queue.dequeue();
        return message;
    }

    void acknowledge(long messageId) {
        // TODO handle nacks
    }

    public void requeue(Message message) {
        queue.enqueue(message);
    }

    /**
     * Get the current consumer list iterator for the queue. This is a snapshot of the consumers at the time
     * when the when this method is invoked.
     *
     * @return CyclicConsumerIterator
     */
    CyclicConsumerIterator getCyclicConsumerIterator() {
        consumerIterator.setIterator(Iterables.cycle(consumers).iterator());
        return consumerIterator;
    }

    /**
     * True if there are no {@link Message} objects in the queue and false otherwise.
     *
     * @return True if the queue doesn't contain any {@link Message} objects
     */
    boolean isEmpty() {
        return queue.size() == 0;
    }

    /**
     * Returns the number of {@link Message} objects in this queue.
     *
     * @return Number of {@link Message} objects in the queue.
     */
    int size() {
        return queue.size();
    }

    /**
     * True if there are no consumers and false otherwise.
     *
     * @return True if there are no {@link Consumer} for the queue.
     */
    boolean isUnused() {
        return consumers.isEmpty();
    }

    void closeAllConsumers() {
        Iterator<Consumer> iterator = consumers.iterator();
        while (iterator.hasNext()) {

            Consumer consumer = iterator.next();
            try {
                consumer.close();
            } catch (BrokerException e) {
                log.error("Error occurred while closing the consumer [ " + consumer + " ] " +
                        "for queue [ " + queue.toString() + " ]", e);
            } finally {
                iterator.remove();
            }
        }
    }

    /**
     * In memory queue implementation for non durable queues.
     */
    private static class MemQueueImpl extends Queue {

        private final int capacity;

        private final java.util.Queue<Message> queue;

        MemQueueImpl(String name, int capacity, boolean autoDelete) {
            super(name, false, autoDelete);
            this.capacity = capacity;
            queue = new LinkedBlockingDeque<>(capacity);
        }

        @Override
        public int capacity() {
            return capacity;
        }

        @Override
        public int size() {
            return queue.size();
        }

        @Override
        public boolean enqueue(Message message) {
            return queue.offer(message);
        }

        @Override
        public Message dequeue() {
            return queue.poll();
        }

    }

    /**
     * Database backed queue implementation.
     * // TODO: need to write the db related logic
     */
    private static class DbBackedQueueImpl extends Queue {

        private final java.util.Queue<Message> memQueue;

        private final MessageDao messageDao;

        DbBackedQueueImpl(String queueName, MessageDao messageDao, boolean autoDelete) {
            super(queueName, true, autoDelete);
            this.messageDao = messageDao;
            this.memQueue = new LinkedBlockingQueue<>();
        }

        @Override
        public int capacity() {
            return Queue.UNBOUNDED;
        }

        @Override
        public int size() {
            return memQueue.size();
        }

        @Override
        public boolean enqueue(Message message) {
            messageDao.persist(message);
            return memQueue.offer(message);
        }

        @Override
        public Message dequeue() {
            return memQueue.poll();
        }
    }
}
