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

package org.wso2.broker.core.queue;

import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.Queue;
import org.wso2.broker.core.store.SharedMessageStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.transaction.xa.Xid;

/**
 * Database backed queue implementation.
 */
public class DbBackedQueueImpl extends Queue {

    private final SharedMessageStore sharedMessageStore;

    private final QueueBuffer buffer;

    private final Map<Xid, List<Message>> pendingEnqueueMessages;

    private final Map<Xid, List<Message>> pendingDequeueMessages;

    public DbBackedQueueImpl(String queueName, boolean autoDelete,
                             SharedMessageStore sharedMessageStore, QueueBufferFactory queueBufferFactory)
            throws BrokerException {
        super(queueName, true, autoDelete);
        this.sharedMessageStore = sharedMessageStore;
        buffer = queueBufferFactory.createBuffer(sharedMessageStore::readData);
        Collection<Message> messages = sharedMessageStore.readStoredMessages(queueName);
        buffer.addAll(messages);

        pendingEnqueueMessages = new ConcurrentHashMap<>();
        pendingDequeueMessages = new ConcurrentHashMap<>();
    }

    @Override
    public int capacity() {
        return Queue.UNBOUNDED;
    }

    @Override
    public int size() {
        return buffer.size();
    }

    @Override
    public boolean enqueue(Message message) throws BrokerException {
        if (message.getMetadata().isPersistent()) {
            sharedMessageStore.attach(getName(), message.getInternalId());
        }
        buffer.add(message);
        return true;
    }

    @Override
    public void prepareEnqueue(Xid xid, Message message) throws BrokerException {
        if (message.getMetadata().isPersistent()) {
            sharedMessageStore.attach(xid, getName(), message.getInternalId());
        }
        List<Message> messages = pendingEnqueueMessages.computeIfAbsent(xid, k -> new ArrayList<>());
        messages.add(message);
    }

    @Override
    public void commit(Xid xid) {

        List<Message> dequeueMessages = pendingDequeueMessages.get(xid);
        if (Objects.nonNull(dequeueMessages)) {
            buffer.removeAll(dequeueMessages);
        }

        List<Message> messages = pendingEnqueueMessages.get(xid);
        if (Objects.nonNull(messages)) {
            buffer.addAll(messages);
        }
    }

    @Override
    public void rollback(Xid xid) {
        pendingDequeueMessages.remove(xid);
        pendingEnqueueMessages.remove(xid);
    }

    @Override
    public Message dequeue() {
        return buffer.getFirstDeliverable();
    }

    @Override
    public void detach(Message message) {
        sharedMessageStore.detach(getName(), message);
        buffer.remove(message);
    }

    @Override
    public void prepareDetach(Xid xid, Message message) throws BrokerException {
        sharedMessageStore.detach(xid, getName(), message);
        List<Message> dequeueMessages = pendingDequeueMessages.computeIfAbsent(xid, k -> new ArrayList<>());
        dequeueMessages.add(message);
    }
}
