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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * This class goes through the list of db operations and removes any operations that can be canceled out.
 */
public class DbEventMatcher implements EventHandler<DbOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbEventMatcher.class);

    private final Map<Long, DbOperation> insertMap;

    private final Map<Long, List<DbOperation>> detachMap;

    private final int maxBatchSize;

    private final Queue<Long> eventQueue;

    public DbEventMatcher(int ringBufferSize) {
        insertMap = new HashMap<>();
        detachMap = new HashMap<>();
        this.maxBatchSize = ringBufferSize;
        eventQueue = new ArrayDeque<>(ringBufferSize);
    }

    @Override
    public void onEvent(DbOperation event, long sequence, boolean endOfBatch) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} event with message id {} for sequence {}", event.getType(), event.getMessage(), sequence);
        }
        eventQueue.add(event.getMessageId());

        switch (event.getType()) {
            case INSERT_MESSAGE:
                insertMap.put(event.getMessage().getInternalId(), event);
                break;
            case DELETE_MESSAGE:
                long internalId = event.getMessageId();
                removeMatchingInsertEvent(event, sequence, internalId);
                removeMatchingDetachEvents(internalId);
                break;
            case DETACH_MSG_FROM_QUEUE:
                detachMap.computeIfAbsent(event.getMessageId(), k -> new ArrayList<>())
                         .add(event);
                break;
            case READ_MSG_DATA:
            case NO_OP:
                break;
            default:
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Unknown event type " + event.getType());
                }
        }

        removeOldestEntryFromIndex();
        event.completeProcessing();
    }

    /**
     * This method is invoked when a delete event is processed. If there are corresponding detach events found we can
     * safely ignore those events since current delete operation will remove the message from attached queues.
     *
     * @param internalId Internal message id of the event.
     */
    private void removeMatchingDetachEvents(long internalId) {
        List<DbOperation> detachRequestList;
        if ((detachRequestList = detachMap.remove(internalId)) != null) {
            for (DbOperation detachRequest : detachRequestList) {
                if (detachRequest.acquireForPersisting()) {
                    detachRequest.clear();
                    detachRequest.completeProcessing();
                }
            }
        }
    }

    /**
     * This method is invoked when a delete event is processed. If the corresponding insert is not written to the
     * database as well we cancel out both insert and delete events.
     *
     * @param event Current {@link DbOperation} event
     * @param sequence Disruptor sequence number
     * @param internalId Internal message id of the event
     */
    private void removeMatchingInsertEvent(DbOperation event, long sequence, long internalId) {
        DbOperation insertRequest;
        if ((insertRequest = insertMap.remove(internalId)) != null) {
            if (insertRequest.acquireToProcess()) {
                insertRequest.clear();

                event.clear();
                insertRequest.completeProcessing();
                LOGGER.debug("Matching insert event found and cleared "
                        + "for message id {} for sequence {}", internalId, sequence);
            }
        }
    }

    /**
     * Inserts are added first and then detach events. When removing, if we find a matching insert we don't
     * remove the detach since it is set after the insert. We need to remove only one entry at a time.
     */
    private void removeOldestEntryFromIndex() {
        if (eventQueue.size() == maxBatchSize) {
            Long id = eventQueue.poll();

            List<DbOperation> detachList;
            if (insertMap.remove(id) == null &&
                    ((detachList = detachMap.get(id)) != null) && !detachList.isEmpty()) {
                detachList.remove(0); // Remove first element in list
                if (detachList.isEmpty()) {
                    detachMap.remove(id);
                }
            }
        }
    }
}
