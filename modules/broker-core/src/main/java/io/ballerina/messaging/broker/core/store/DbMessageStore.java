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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.ballerina.messaging.broker.common.DaoException;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.queue.QueueBuffer;
import io.ballerina.messaging.broker.core.store.dao.MessageDao;
import io.ballerina.messaging.broker.core.store.disruptor.SleepingBlockingWaitStrategy;

import java.util.Collection;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;
import javax.transaction.xa.Xid;

/**
 * Message store class that is used by all the durable queues to persist messages.
 * <p>
 * Note: This class is thread safe
 */
@ThreadSafe
public class DbMessageStore extends MessageStore {

    private final Disruptor<DbOperation> disruptor;

    private static final EventTranslatorOneArg<DbOperation, Message> INSERT_MESSAGE =
            (event, sequence, message) -> event.insertMessage(message);

    private static final EventTranslatorTwoArg<DbOperation, String, Long> DETACH_FROM_QUEUE =
            (event, sequence, queueName, messageId) -> event.detachFromQueue(queueName, messageId);

    private static final EventTranslatorOneArg<DbOperation, Long> DELETE_MESSAGE =
            (event, sequence, messageId) -> event.deleteMessage(messageId);

    private static final EventTranslatorTwoArg<DbOperation, QueueBuffer, Message> READ_MESSAGE_DATA =
            (event, sequence, queueBuffer, message) -> event.readMessageData(queueBuffer, message);

    private final MessageDao messageDao;

    @SuppressWarnings("unchecked")
    DbMessageStore(MessageDao messageDao, int bufferSize, int maxDbBatchSize) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("DisruptorMessageStoreThread-%d").build();

        disruptor = new Disruptor<>(DbOperation.getFactory(),
                                    bufferSize, namedThreadFactory, ProducerType.MULTI, new
                                            SleepingBlockingWaitStrategy());

        disruptor.setDefaultExceptionHandler(new DbStoreExceptionHandler());

        disruptor.handleEventsWith(new DbEventMatcher(bufferSize))
                 .then(new DbAccessHandler(messageDao, maxDbBatchSize))
                 .then(new FinalEventHandler());
        disruptor.start();
        this.messageDao = messageDao;
    }

    @Override
    void publishMessageToStore(Message message) {
        disruptor.publishEvent(INSERT_MESSAGE, message);
    }

    @Override
    void detachFromQueue(String queueName, long messageId) {
        disruptor.publishEvent(DETACH_FROM_QUEUE, queueName, messageId);
    }

    @Override
    void deleteMessage(long messageId) {
        disruptor.publishEvent(DELETE_MESSAGE, messageId);
    }

    @Override
    void commit(TransactionData transactionData) throws BrokerException {
        try {
            messageDao.persist(transactionData);
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    @Override
    void commit(Xid xid, TransactionData transactionData) throws BrokerException {
        try {
            messageDao.commitPreparedData(xid, transactionData);
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void rollback(Xid xid) throws BrokerException {
        try {
            messageDao.rollbackPreparedData(xid);
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void fillMessageData(QueueBuffer queueBuffer, Message message) {
        disruptor.publishEvent(READ_MESSAGE_DATA, queueBuffer, message);
    }

    @Override
    public Collection<Message> readAllMessagesForQueue(String queueName) throws BrokerException {
        try {
            return messageDao.readAll(queueName);
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void prepare(Xid xid, TransactionData transactionData) throws BrokerException {
        try {
            messageDao.prepare(xid, transactionData);
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void retrieveStoredXids(Consumer<Xid> xidConsumer) throws BrokerException {
        try {
            messageDao.retrieveAllStoredXids(xidConsumer);
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    @Override
    public Collection<Message> recoverEnqueuedMessages(Xid xid) throws BrokerException {
        try {
            return messageDao.retrieveAllEnqueuedMessages(xid);
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

}
