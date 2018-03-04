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

package io.ballerina.messaging.broker.core.queue;

import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import javax.transaction.xa.Xid;

/**
 * In memory queue implementation for non durable queues.
 */
public class MemQueueImpl extends Queue {

    private final int capacity;

    private final java.util.Queue<Message> queue;

    private final Map<Xid, List<Message>> pendingEnqueueMessages;

    private final Map<Xid, List<Message>> pendingDequeueMessages;

    public MemQueueImpl(String queueName, int capacity, boolean autoDelete) {
        this(queueName, false, capacity, autoDelete);
    }

    public MemQueueImpl(String queueName, boolean durable, int capacity, boolean autoDelete) {
        super(queueName, durable, autoDelete);
        this.capacity = capacity;
        queue = new LinkedBlockingDeque<>(capacity);
        pendingEnqueueMessages = new ConcurrentHashMap<>();
        pendingDequeueMessages = new ConcurrentHashMap<>();
    }

    /**
     * Create an unbounded in memory queue
     *
     * @param name       name of the queue
     * @param autoDelete auto delete capability
     */
    public MemQueueImpl(String name, boolean autoDelete) {
        this(name, Queue.UNBOUNDED, autoDelete);
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
    public void prepareEnqueue(Xid xid, Message message) {
        List<Message> transactionalMessages = pendingEnqueueMessages.computeIfAbsent(xid, k -> new ArrayList<>());
        transactionalMessages.add(message);
    }

    @Override
    public void commit(Xid xid) {
        List<Message> dequeueMessages = pendingDequeueMessages.get(xid);
        if (Objects.nonNull(dequeueMessages)) {
            queue.removeAll(dequeueMessages);
        }

        List<Message> messages = pendingEnqueueMessages.get(xid);
        if (Objects.nonNull(messages)) {
            queue.addAll(messages);
        }
    }

    @Override
    public void rollback(Xid xid) {
        pendingDequeueMessages.remove(xid);
        pendingEnqueueMessages.remove(xid);
    }

    @Override
    public Message dequeue() {
        return queue.poll();
    }

    @Override
    public void detach(Message message) {
        // nothing to do
    }

    @Override
    public void prepareDetach(Xid xid, Message message) {
        List<Message> dequeueMessages = pendingDequeueMessages.computeIfAbsent(xid, k -> new ArrayList<>());
        dequeueMessages.add(message);
    }
}
