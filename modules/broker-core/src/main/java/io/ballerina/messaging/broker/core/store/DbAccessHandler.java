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

    private final Map<Long, Message> insertMap;

    private final List<Long> deleteList;

    private final Map<Long, DbOperation> detachMap;

    private final Map<Long, Message> readList;

    public DbAccessHandler(MessageDao messageDao, int maxBatchSize) {
        this.messageDao = messageDao;
        this.maxBatchSize = maxBatchSize;
        insertMap = new HashMap<>(maxBatchSize);
        deleteList = new ArrayList<>(maxBatchSize);
        detachMap = new HashMap<>(maxBatchSize);
        readList = new HashMap<>(maxBatchSize);
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
                insertMap.put(event.getMessage().getInternalId(), event.getMessage());
                break;
            case DELETE_MESSAGE:
                deleteList.add(event.getMessageId());
                break;
            case DETACH_MSG_FROM_QUEUE:
                detachMap.put(event.getMessageId(), event);
                break;
            case READ_MSG_DATA:
                readList.put(event.getBareMessage().getInternalId(), event.getBareMessage());
                break;
            case NO_OP:
                break;
            default:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Unknown event type " + event.getType());
                }
        }

        if (isBatchReady(endOfBatch, insertMap.values())) {
            messageDao.persist(insertMap.values());
            insertMap.clear();
        }

        if (isBatchReady(endOfBatch, deleteList)) {
            messageDao.delete(deleteList);
            deleteList.clear();
        }

        if (isBatchReady(endOfBatch, detachMap.values())) {
            messageDao.detachFromQueue(detachMap.values());
            detachMap.clear();
        }

        if (isBatchReady(endOfBatch, readList.values())) {
            messageDao.read(readList);
            readList.clear();
        }
    }

    private boolean isBatchReady(boolean endOfBatch, Collection collection) {
        return !collection.isEmpty() && (collection.size() >= maxBatchSize || endOfBatch);
    }
}
