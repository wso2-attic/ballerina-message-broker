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
import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Queue;
import io.ballerina.messaging.broker.core.store.DbMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    private final DbMessageStore dbMessageStore;

    private final QueueBuffer buffer;

    private final Map<Xid, List<Message>> pendingEnqueueMessages;

    private final Map<Xid, List<DetachableMessage>> pendingDequeueMessages;

    public DbBackedQueueImpl(String queueName,
                             boolean autoDelete,
                             DbMessageStore dbMessageStore,
                             QueueBufferFactory queueBufferFactory)
            throws BrokerException {
        super(queueName, true, autoDelete);
        this.dbMessageStore = dbMessageStore;
        buffer = queueBufferFactory.createBuffer(dbMessageStore::fillMessageData);

        LOGGER.debug("Recovering messages for queue {}", queueName);

        Collection<Message> messages = dbMessageStore.readAllMessagesForQueue(queueName);

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
        return buffer.getNumberOfUndeliveredMessages();
    }

    @Override
    public boolean enqueue(Message message) throws BrokerException {
        if (message.getMetadata().isPersistent()) {
            dbMessageStore.attach(getName(), message.getInternalId());
            buffer.add(message);
            return true;
        } else {
            return buffer.addIndelibleMessage(message);
        }
    }

    @Override
    public void prepareEnqueue(Xid xid, Message message) throws BrokerException {
        if (message.getMetadata().isPersistent()) {
            dbMessageStore.attach(xid, getName(), message.getInternalId());
        }
        List<Message> messages = pendingEnqueueMessages.computeIfAbsent(xid, k -> new ArrayList<>());
        messages.add(message);
    }

    @Override
    public void commit(Xid xid) {

        List<DetachableMessage> dequeueMessages = pendingDequeueMessages.get(xid);
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
        List<Message> messages = pendingEnqueueMessages.remove(xid);
        if (Objects.nonNull(messages)) {
            messages.forEach(Message::release);
        }
    }

    @Override
    public Message dequeue() {
        return buffer.getFirstDeliverable();
    }

    @Override
    public void getExpired(Set<Message> expiredMessages, int capacity) {
        buffer.peekExpiredMessages(expiredMessages, capacity);
    }

    @Override
    public void detach(DetachableMessage message) {
        dbMessageStore.detach(getName(), message);
        buffer.remove(message.getInternalId());
    }

    @Override
    public void prepareDetach(Xid xid, DetachableMessage detachableMessage) throws BrokerException {
        dbMessageStore.detach(xid, getName(), detachableMessage);
        List<DetachableMessage> dequeueMessages = pendingDequeueMessages.computeIfAbsent(xid, k -> new ArrayList<>());
        dequeueMessages.add(detachableMessage);
    }

    @Override
    public int clear() {
        String queueName = getName();
        return buffer.clear(message -> {
            dbMessageStore.detach(queueName, message.getDetachableMessage());
            message.release();
        });
    }
}
