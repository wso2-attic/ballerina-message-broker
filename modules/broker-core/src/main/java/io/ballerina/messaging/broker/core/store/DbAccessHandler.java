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

    private final Map<Long, List<Message>> readList;

    private final TransactionData transactionData;

    public DbAccessHandler(MessageDao messageDao, int maxBatchSize) {
        this.messageDao = messageDao;
        this.maxBatchSize = maxBatchSize;
        readList = new HashMap<>(maxBatchSize);
        transactionData = new TransactionData();
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
                transactionData.addEnqueueMessage(event.getMessage());
                break;
            case DELETE_MESSAGE:
                transactionData.addDeletableMessage(event.getMessageId());
                break;
            case DETACH_MSG_FROM_QUEUE:
                transactionData.detach(event.getQueueName(), event.getMessageId());
                break;
            case READ_MSG_DATA:
                List<Message> messages = readList.computeIfAbsent(event.getBareMessage().getInternalId(),
                        messageId -> new ArrayList<>());
                messages.add(event.getBareMessage());
                break;
            case NO_OP:
                break;
            default:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Unknown event type " + event.getType());
                }
        }

        if (isBatchReady(endOfBatch, transactionData)) {
            messageDao.persist(transactionData);
            transactionData.clear();
        }

        if (isBatchReady(endOfBatch, readList.values())) {
            messageDao.read(readList);
            readList.clear();
        }
    }

    private boolean isBatchReady(boolean endOfBatch, Collection collection) {
        return !collection.isEmpty() && (collection.size() >= maxBatchSize || endOfBatch);
    }

    private boolean isBatchReady(boolean endOfBatch, TransactionData transactionData) {
        return !transactionData.isEmpty() && (transactionData.size() >= maxBatchSize || endOfBatch);
    }
}
