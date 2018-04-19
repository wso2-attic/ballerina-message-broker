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
import java.util.Collections;
import java.util.function.Consumer;
import javax.transaction.xa.Xid;

/**
 * Null message store object used to avoid calling the database access layer.
 */
public class NullMessageStore extends MessageStore {

    @Override
    void publishMessageToStore(Message message) {
        message.release();
    }

    @Override
    void detachFromQueue(String queueName, long messageId) {
        // Do nothing
    }

    @Override
    void deleteMessage(long messageId) {
        // Do nothing
    }

    @Override
    void commit(TransactionData transactionData) {
        // Do nothing
    }

    @Override
    void commit(Xid xid, TransactionData transactionData) {
        // Do nothing
    }

    @Override
    public void rollback(Xid xid) {
        // Do nothing
    }

    @Override
    public void fillMessageData(QueueBuffer queueBuffer, Message message) {
        // Do nothing
    }

    @Override
    public Collection<Message> readAllMessagesForQueue(String queueName) {
        return Collections.emptyList();
    }

    @Override
    public void prepare(Xid xid, TransactionData transactionData) {
        // Do nothing
    }

    @Override
    public void retrieveStoredXids(Consumer<Xid> consumer) throws BrokerException {
        // Do nothing
    }

    @Override
    public Collection<Message> recoverEnqueuedMessages(Xid xid) {
        return Collections.emptyList();
    }
}
