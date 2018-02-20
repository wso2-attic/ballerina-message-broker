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

/**
 * This class initiates database operations through disruptor
 */
public class FinalEventHandler implements EventHandler<DbOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinalEventHandler.class);

    @Override
    public void onEvent(DbOperation event, long sequence, boolean endOfBatch) {
        try {
            switch (event.getType()) {
                case READ_MSG_DATA:
                    event.getQueueBuffer().markMessageFilled(event.getBareMessage());
                    break;
                case INSERT_MESSAGE:
                case DELETE_MESSAGE:
                case DETACH_MSG_FROM_QUEUE:
                case NO_OP:
                    break;
                default:
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error("Unknown event type " + event.getType());
                    }
            }
        } finally {
            event.clear();
        }
    }

}
