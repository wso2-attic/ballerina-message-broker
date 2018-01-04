/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.core.store.dao;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.store.DbEventMatcher;
import org.wso2.broker.core.store.DbOperation;
import org.wso2.broker.core.store.DbWriter;
import org.wso2.broker.core.store.LogExceptionHandler;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * Message store class that is used by all the durable queues to persist messages
 * <p>
 * Note: This class is thread safe
 */
public class SharedMessageStore {

    private final Map<Long, Message> pendingMessages;

    private final Disruptor<DbOperation> disruptor;

    private static final EventTranslatorOneArg<DbOperation, Message> INSERT_MESSAGE =
            (event, sequence, message) -> event.insertMessage(message);

    private static final EventTranslatorTwoArg<DbOperation, String, Long> DETACH_FROM_QUEUE =
            (event, sequence, queueName, messageId) -> event.detachFromQueue(queueName, messageId);

    private static final EventTranslatorOneArg<DbOperation, Long> DELETE_MESSAGE =
            (event, sequence, messageId) -> event.deleteMessage(messageId);

    private final MessageDao messageDao;

    @SuppressWarnings("unchecked")
    public SharedMessageStore(MessageDao messageDao, int bufferSize, int maxDbBatchSize) {

        pendingMessages = new ConcurrentHashMap<>();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("DisruptorMessageStoreThread-%d").build();

        disruptor = new Disruptor<>(DbOperation.getFactory(),
                bufferSize, namedThreadFactory, ProducerType.MULTI, new SleepingWaitStrategy());

        disruptor.setDefaultExceptionHandler(new LogExceptionHandler());

        disruptor.handleEventsWith(new DbEventMatcher(bufferSize))
                .then(new DbWriter(messageDao, maxDbBatchSize))
                .then((EventHandler<DbOperation>) (event, sequence, endOfBatch) -> event.clear());
        disruptor.start();
        this.messageDao = messageDao;
    }

    public void add(Message message) {
        pendingMessages.put(message.getMetadata().getInternalId(), message.shallowCopy());
    }

    public void attach(String queueName, long messageInternalId) throws BrokerException {
        Message message = pendingMessages.get(messageInternalId);
        if (message == null) {
            throw new BrokerException("Unknown message id " + messageInternalId
                    + " cannot attach to queue " + queueName);
        }
        message.getMetadata().addOwnedQueue(queueName);
    }

    private void delete(long messageId) {
        disruptor.publishEvent(DELETE_MESSAGE, messageId);
    }

    public void detach(String queueName, Message message) {
        message.getMetadata().removeOwnedQueue(queueName);
        if (!message.getMetadata().hasAttachedQueues()) {
            delete(message.getMetadata().getInternalId());
        } else {
            disruptor.publishEvent(DETACH_FROM_QUEUE, queueName, message.getMetadata().getInternalId());
        }
    }

    public void flush(long internalMessageId) {
        Message message = pendingMessages.remove(internalMessageId);
        if (message != null) {

            if (message.getMetadata().hasAttachedQueues()) {
                disruptor.publishEvent(INSERT_MESSAGE, message);
            } else {
                message.release();
            }
        }
    }

    public Collection<Message> readStoredMessages(String queueName) throws BrokerException {
        return messageDao.readAll(queueName);
    }
}
