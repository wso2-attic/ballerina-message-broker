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

import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Queue;
import io.ballerina.messaging.broker.core.store.SharedMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DbBackedQueueImpl.class);

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

        LOGGER.debug("Recovering messages for queue {}", queueName);

        Collection<Message> messages = sharedMessageStore.readStoredMessages(queueName);

        pendingEnqueueMessages = new ConcurrentHashMap<>();
        pendingDequeueMessages = new ConcurrentHashMap<>();
        buffer.addAllBareMessages(messages);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} messages recovered for queue {}", messages.size(), queueName);
        }
    }

    @Override
    public int capacity() {
        return Queue.UNBOUNDED;
    }

    @Override
    public int size() {
        return buffer.getNumberOfInflightMessages();
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
