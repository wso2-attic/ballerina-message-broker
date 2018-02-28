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
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.queue.QueueBuffer;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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
        transactionData.addEnqueueMessage(message);
    }

    public void attach(String queueName, long messageInternalId) throws BrokerException {
        Message message = pendingMessages.get(messageInternalId);
        if (message == null) {
            throw new BrokerException("Unknown message id " + messageInternalId
                                              + " cannot attach to queue " + queueName);
        }
        message.addOwnedQueue(queueName);
    }

    public void attach(Xid xid, String queueName, long messageInternalId) throws BrokerException {
        TransactionData transactionData = getTransactionData(xid);
        transactionData.attach(queueName, messageInternalId);
    }

    private TransactionData getTransactionData(Xid xid) throws BrokerException {
        TransactionData transactionData = transactionMap.get(xid);
        if (Objects.isNull(transactionData)) {
            throw new BrokerException("Unknown Xid " + xid + ". Create a branch with Xid before attaching to a queue");
        }
        return transactionData;
    }

    public synchronized void detach(String queueName, final Message message) {
        message.removeAttachedQueue(queueName);
        if (!message.hasAttachedQueues()) {
            deleteMessage(message.getInternalId());
        } else {
            detachFromQueue(queueName, message);
        }
    }

    public synchronized void detach(Xid xid, String queueName, Message message) throws BrokerException {
        TransactionData transactionData = getTransactionData(xid);
        message.removeAttachedQueue(queueName);
        if (message.hasAttachedQueues()) {
            transactionData.detach(queueName, message.getInternalId());
        } else {
            transactionData.addDeletableMessage(message.getInternalId());
        }
    }


    public void flush(long internalMessageId) {
        Message message = pendingMessages.remove(internalMessageId);
        if (message != null) {

            if (message.hasAttachedQueues()) {
                publishMessageToStore(message);
            } else {
                message.release();
            }
        }
    }

    public void flush(Xid xid) throws BrokerException {
        commitTransactionToStore(getTransactionData(xid));
        clear(xid);
    }


    public void branch(Xid xid) {
        transactionMap.putIfAbsent(xid, new TransactionData());
    }

    public void clear(Xid xid) {
        TransactionData transactionData = transactionMap.remove(xid);
        transactionData.releaseEnqueueMessages();
        transactionData.clear();
    }

    abstract void publishMessageToStore(Message message);

    abstract void detachFromQueue(String queueName, Message message);

    abstract void deleteMessage(long messageId);

    abstract void commitTransactionToStore(TransactionData transactionData) throws BrokerException;

    public abstract void fillMessageData(QueueBuffer queueBuffer, Message message);

    public abstract Collection<Message> readAllMessagesForQueue(String queueName) throws BrokerException;
}
