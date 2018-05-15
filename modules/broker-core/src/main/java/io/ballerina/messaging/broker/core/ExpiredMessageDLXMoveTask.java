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

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.core.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Check for expired messages of a given queue and move to DLX
 */
public class ExpiredMessageDLXMoveTask extends Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpiredMessageDLXMoveTask.class);

    /**
     * Queue whose messages are handle by this expiration task
     */
    private QueueHandler queueHandler;

    /**
     * size of chunk of messages traversed for expiration on one iteration
     */
    private final int expiryCheckBatchSize;

    /**
     * Handler to move messages to DLX
     */
    private DLXMover dlxMover;


    /**
     * Constructor for ExpiredMessageDLXMoveTask
     *
     * @param queueHandler         Queue whose messages are handle by this expiration task
     * @param expiryCheckBatchSize Size of chunk of messages traversed for expiration on one iteration
     * @param dlxMover             Handler to move messages to DLX
     */
    public ExpiredMessageDLXMoveTask(QueueHandler queueHandler, int expiryCheckBatchSize, DLXMover dlxMover) {
        this.queueHandler = queueHandler;
        this.expiryCheckBatchSize = expiryCheckBatchSize;
        this.dlxMover = dlxMover;
    }

    @Override
    public void onAdd() {
        // ignore
    }

    @Override
    public void onRemove() {
        // ignore
    }

    @Override
    public String getId() {
        return queueHandler.getName();
    }

    @Override
    public TaskHint call() throws Exception {
        int movedMessageCount = 0;

        if (queueHandler.size() == 0) {
            return TaskHint.IDLE;
        }

        Set<Message> expiredMessages = new HashSet<>(expiryCheckBatchSize);
        queueHandler.getExpired(expiredMessages, expiryCheckBatchSize);

        //move expired messages to DLX
        for (Message expiredMessage : expiredMessages) {
            dlxMover.moveMessageToDlc(queueHandler.getName(), expiredMessage);
            LOGGER.warn("Moved expired message id = {} from queue {} to DLX. Expiry time = {}",
                    expiredMessage.getInternalId(), queueHandler.getName(), expiredMessage.getExpiryTimestamp());
            movedMessageCount = movedMessageCount + 1;
        }

        if (movedMessageCount > 0) {
            return TaskHint.ACTIVE;
        } else {
            return TaskHint.IDLE;
        }

    }
}
