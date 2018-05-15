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

import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Queue;
import io.ballerina.messaging.broker.core.QueueHandler;
import java.util.Set;
import javax.transaction.xa.Xid;

/**
 * Queue representation which is unmodifiable. Used to return a view of the underlying queue to the outside.
 */
public class UnmodifiableQueueWrapper extends Queue {

    private final Queue queue;

    public UnmodifiableQueueWrapper(Queue queue) {
        super(queue.getName(), queue.isDurable(), queue.isAutoDelete());
        this.queue = queue;
    }

    @Override
    public QueueHandler getQueueHandler() {
        return queue.getQueueHandler();
    }

    @Override
    public int capacity() {
        return queue.capacity();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean enqueue(Message message) {
        throw new UnsupportedOperationException("Queue " + queue.getName() + " is unmodifiable");
    }

    @Override
    public void prepareEnqueue(Xid xid, Message message) {
        throw new UnsupportedOperationException("Queue " + queue.getName() + " is unmodifiable");
    }

    @Override
    public void commit(Xid xid) {
        throw new UnsupportedOperationException("Queue " + queue.getName() + " is unmodifiable");
    }

    @Override
    public void rollback(Xid xid) {
        throw new UnsupportedOperationException("Queue " + queue.getName() + " is unmodifiable");
    }

    @Override
    public Message dequeue() {
        throw new UnsupportedOperationException("Queue " + queue.getName() + " is unmodifiable");
    }

    @Override
    public void getExpired(Set<Message> expiredMessages, int capacity) {
        queue.getExpired(expiredMessages, capacity);
    }

    @Override
    public void detach(DetachableMessage message) {
        throw new UnsupportedOperationException("Queue " + queue.getName() + " is unmodifiable");
    }

    @Override
    public void prepareDetach(Xid xid, DetachableMessage message) {
        throw new UnsupportedOperationException("Queue " + queue.getName() + " is unmodifiable");
    }

    @Override
    public int clear() {
        throw new UnsupportedOperationException("Queue " + queue.getName() + " is unmodifiable");
    }
}
