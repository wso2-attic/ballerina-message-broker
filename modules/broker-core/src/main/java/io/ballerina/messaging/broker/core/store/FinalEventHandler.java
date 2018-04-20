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
import io.ballerina.messaging.broker.core.queue.QueueBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * This class initiates database operations through disruptor.
 */
public class FinalEventHandler implements EventHandler<DbOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinalEventHandler.class);

    @Override
    public void onEvent(DbOperation event, long sequence, boolean endOfBatch) {
        Throwable exceptionObject = event.getExceptionObject();
        if (Objects.nonNull(exceptionObject)) {
            handleError(event, sequence, exceptionObject);
            return;
        }

        try {
            switch (event.getType()) {
                case READ_MSG_DATA:
                    Message message = event.getBareMessage();
                    QueueBuffer queueBuffer = event.getQueueBuffer();

                    if (message.hasContent()) {
                        queueBuffer.markMessageFilled(message);
                    } else {
                        LOGGER.error("Message {} was not read from the DB. Therefore dropping message",
                                     message.getInternalId());
                        queueBuffer.remove(message.getInternalId());
                    }
                    break;
                case INSERT_MESSAGE:
                case DELETE_MESSAGE:
                case DETACH_MSG_FROM_QUEUE:
                case NO_OP:
                    break;
                default:
                    LOGGER.error("Unknown event type {}", event.getType());
            }
        } finally {
            event.clear();
        }
    }

    private void handleError(DbOperation event, long sequence, Throwable exceptionObject) {
        switch (event.getType()) {
            case READ_MSG_DATA:
                Message message = event.getBareMessage();

                event.getQueueBuffer().markMessageFillFailed(message);
                LOGGER.warn("Message read failed for message {}", message.getInternalId());
                break;
            case INSERT_MESSAGE:
            case DELETE_MESSAGE:
            case DETACH_MSG_FROM_QUEUE:
                LOGGER.error("Error occurred while processing DB write event for sequence {} db operation {}",
                             sequence,
                             event,
                             exceptionObject);
                break;
            case NO_OP:
                break;
            default:
                LOGGER.error("Unknown event type {}", event.getType(), exceptionObject);
        }
    }

}
