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

import java.util.concurrent.LinkedBlockingDeque;

/**
 * In memory queue implementation for non durable queues.
 */
public class MemQueueImpl extends Queue {

    private final int capacity;

    private final java.util.Queue<Message> queue;

    public MemQueueImpl(String name, int capacity, boolean autoDelete) {
        super(name, false, autoDelete);
        this.capacity = capacity;
        queue = new LinkedBlockingDeque<>(capacity);
    }

    /**
     * Create an unbounded in memory queue.
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
    public Message dequeue() {
        return queue.poll();
    }

    @Override
    public void detach(Message message) {
        // nothing to do
    }
}
