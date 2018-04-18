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

import com.lmax.disruptor.EventHandler;
import io.ballerina.messaging.broker.common.DaoException;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.store.dao.MessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class initiates database operations through disruptor.
 */
public class DbAccessHandler implements EventHandler<DbOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbAccessHandler.class);

    private final MessageDao messageDao;

    private final int maxBatchSize;

    private final TransactionData transactionData;

    private final List<DbOperation> transactionEvents;

    private final List<DbOperation> readEvents;

    public DbAccessHandler(MessageDao messageDao, int maxBatchSize) {
        this.messageDao = messageDao;
        this.maxBatchSize = maxBatchSize;
        transactionData = new TransactionData();
        readEvents = new ArrayList<>(maxBatchSize);
        transactionEvents = new ArrayList<>(maxBatchSize);
    }

    @Override
    public void onEvent(DbOperation event, long sequence, boolean endOfBatch) throws Exception {

        // Wait until the event is available for DB writer
        while (!event.acquireForPersisting()) {
            LOGGER.debug("Waiting to acquire event to persist. Sequence {}", sequence);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} event added for id {} for sequence {}", event.getType(), event.getMessageId(), sequence);
        }

        switch (event.getType()) {
            case INSERT_MESSAGE:
            case DELETE_MESSAGE:
            case DETACH_MSG_FROM_QUEUE:
                transactionEvents.add(event);
                break;
            case READ_MSG_DATA:
                readEvents.add(event);
                break;
            case NO_OP:
                break;
            default:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Unknown event type " + event.getType());
                }
        }

        processTransactions(endOfBatch);
        processMessageReads(endOfBatch);
    }

    private void processTransactions(boolean endOfBatch) {
        if (isBatchReady(endOfBatch, transactionEvents)) {
            try {
                clusterTransactionEvents();
                messageDao.persist(transactionData);
            } catch (DaoException e) {
                transactionEvents.forEach(eventObject -> eventObject.setExceptionObject(e));
            } finally {
                transactionData.clear();
                transactionEvents.clear();
            }
        }
    }

    private void clusterTransactionEvents() {
        transactionEvents.forEach(txEvent -> {
            switch (txEvent.getType()) {
                case INSERT_MESSAGE:
                    transactionData.addEnqueueMessage(txEvent.getMessage());
                    break;
                case DELETE_MESSAGE:
                    transactionData.addDeletableMessage(txEvent.getMessageId());
                    break;
                case DETACH_MSG_FROM_QUEUE:
                    transactionData.detach(txEvent.getQueueName(), txEvent.getMessageId());
                    break;
                default:
                    LOGGER.error("Invalid transaction event collected {}", txEvent.getType());
            }
        });
    }

    private void processMessageReads(boolean endOfBatch) {
        if (isBatchReady(endOfBatch, readEvents)) {
            try {
                messageDao.read(getUniqueMessageList());
            } catch (DaoException e) {
                readEvents.forEach(eventObject -> eventObject.setExceptionObject(e));
            } finally {
                readEvents.clear();
            }
        }
    }

    private Map<Long, List<Message>> getUniqueMessageList() {
        Map<Long, List<Message>> readList = new HashMap<>(maxBatchSize);

        readEvents.forEach(action -> readList.computeIfAbsent(action.getBareMessage().getInternalId(),
                                                              messageId -> new ArrayList<>())
                                             .add(action.getBareMessage()));

        return readList;
    }

    private boolean isBatchComplete(int collectionSize, boolean endOfBatch) {
        return collectionSize >= maxBatchSize || endOfBatch;
    }

    private boolean isBatchReady(boolean endOfBatch, Collection collection) {
        return !collection.isEmpty() && isBatchComplete(collection.size(), endOfBatch);
    }
}
