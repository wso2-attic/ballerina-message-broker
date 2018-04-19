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

package io.ballerina.messaging.broker.core.store;

import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.queue.QueueBuffer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.transaction.xa.Xid;

/**
 * Message store class that is used to handle messages.
 * Note: This class is thread safe.
 */
public abstract class MessageStore {

    private final Map<Long, Message> pendingMessages = new ConcurrentHashMap<>();

    private final Map<Xid, TransactionData> transactionMap =  new ConcurrentHashMap<>();

    public void add(Message message) {
        pendingMessages.put(message.getInternalId(), message);
    }

    public void add(Xid xid, Message message) throws BrokerException {
        TransactionData transactionData = getTransactionData(xid);
        synchronized (transactionData) {
            transactionData.addEnqueueMessage(message);
        }
    }

    public void attach(String queueName, long messageInternalId) throws BrokerException {
        Message message = pendingMessages.get(messageInternalId);
        if (message == null) {
            throw new BrokerException("Unknown message id " + messageInternalId
                                              + " cannot attach to queue " + queueName);
        }
        message.addAttachedDurableQueue(queueName);
    }

    public void attach(Xid xid, String queueName, long messageInternalId) throws BrokerException {
        TransactionData transactionData = getTransactionData(xid);
        synchronized (transactionData) {
            transactionData.attach(queueName, messageInternalId);
        }
    }

    private TransactionData getTransactionData(Xid xid) throws BrokerException {
        TransactionData transactionData = transactionMap.get(xid);
        if (Objects.isNull(transactionData)) {
            throw new BrokerException("Unknown Xid " + xid + ". Create a branch with Xid before attaching to a queue");
        }
        return transactionData;
    }

    public synchronized void detach(String queueName, DetachableMessage message) {
        message.removeAttachedDurableQueue(queueName);
        if (!message.hasAttachedDurableQueues()) {
            deleteMessage(message.getInternalId());
        } else {
            detachFromQueue(queueName, message.getInternalId());
        }

    }

    public synchronized void detach(Xid xid, String queueName, DetachableMessage message) throws BrokerException {
        TransactionData transactionData = getTransactionData(xid);
        synchronized (transactionData) {
            transactionData.prepareForDetach(queueName, message);
        }
    }


    public void flush(long internalMessageId) {
        Message message = pendingMessages.remove(internalMessageId);
        if (message != null) {
            if (message.hasAttachedDurableQueues()) {
                publishMessageToStore(message);
            } else {
                message.release();
            }
        }
    }

    public void prepare(Xid xid) throws BrokerException {
        prepare(xid, getTransactionData(xid));
    }

    public void flush(Xid xid, boolean onePhase) throws BrokerException {
        TransactionData transactionData = getTransactionData(xid);
        updateDeletableMessages(transactionData);
        if (onePhase) {
            commit(transactionData);
        } else {
            commit(xid, transactionData);
        }
        clear(xid);
    }

    private void updateDeletableMessages(TransactionData transactionData) {
        Map<String, List<DetachableMessage>> preparedDetachEventMap = transactionData.getPreparedDetachEventMap();
        for (Map.Entry<String, List<DetachableMessage>> entry: preparedDetachEventMap.entrySet()) {
            String queueName = entry.getKey();
            for (DetachableMessage message: entry.getValue()) {
                message.removeAttachedDurableQueue(queueName);
                if (!message.hasAttachedDurableQueues()) {
                    transactionData.addDeletableMessage(message.getInternalId());
                }
            }
        }
    }

    public void remove(Xid xid) throws BrokerException {
        rollback(xid);
        clear(xid);
    }

    public void branch(Xid xid) {
        transactionMap.putIfAbsent(xid, new TransactionData());
    }

    public void clear(Xid xid) {
        TransactionData transactionData = transactionMap.remove(xid);
        if (Objects.nonNull(transactionData)) {
            transactionData.releaseEnqueueMessages();
            transactionData.clear();
        }
    }

    abstract void publishMessageToStore(Message message);

    abstract void detachFromQueue(String queueName, long messageId);

    abstract void deleteMessage(long messageId);

    abstract void commit(TransactionData transactionData) throws BrokerException;

    abstract void commit(Xid xid, TransactionData transactionData) throws BrokerException;

    public abstract void rollback(Xid xid) throws BrokerException;

    public abstract void fillMessageData(QueueBuffer queueBuffer, Message message);

    public abstract Collection<Message> readAllMessagesForQueue(String queueName) throws BrokerException;

    public abstract void prepare(Xid xid, TransactionData transactionData) throws BrokerException;

    public abstract void retrieveStoredXids(Consumer<Xid> consumer) throws BrokerException;

    public abstract Collection<Message> recoverEnqueuedMessages(Xid xid) throws BrokerException;
}
