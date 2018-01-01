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

package org.wso2.broker.core.store;

import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class goes through the list of db operations and removes any operations that can be canceled out.
 */
public class DbEventMatcher implements EventHandler<DbOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbEventMatcher.class);

    private final Map<Long, DbOperation> insertMap;

    private final Map<Long, List<DbOperation>> detachMap;

    private final int maxBatchSize;

    private int currentBatchSize;

    public DbEventMatcher(int ringBufferSize) {
        insertMap = new HashMap<>();
        detachMap = new HashMap<>();
        this.maxBatchSize = ringBufferSize;
    }

    @Override
    public void onEvent(DbOperation event, long sequence, boolean endOfBatch) {
        switch (event.getType()) {
            case INSERT_MESSAGE:
                insertMap.put(event.getMessage().getMetadata().getInternalId(), event);
                break;
            case DELETE_MESSAGE:
                long internalId = event.getMessageId();
                DbOperation insertRequest;
                List<DbOperation> detachRequestList;
                if ((insertRequest = insertMap.remove(internalId)) != null) {
                    if (insertRequest.acquireForPreProcess()) {
                        insertRequest.clear();
                        event.clear();
                        insertRequest.completePreProcess();
                    }
                }
                if ((detachRequestList = detachMap.remove(internalId)) != null) {
                    for (DbOperation detachRequest: detachRequestList) {
                        if (detachRequest.acquireForPersisting()) {
                            detachRequest.clear();
                            detachRequest.completePreProcess();
                        }
                    }
                }
                break;
            case DETACH_MSG_FROM_QUEUE:
                detachMap.computeIfAbsent(event.getMessageId(), k -> new ArrayList<>()).add(event);
                break;
            case NO_OP:
                break;
            default:
                LOGGER.error("Unknown event type " + event.getType());
        }
        currentBatchSize++;
        if (currentBatchSize == maxBatchSize) {
            insertMap.clear();
            detachMap.clear();
            currentBatchSize = 0;
        }


        event.completePreProcess();
    }
}
