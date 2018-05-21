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
 */

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.task.Task;

/**
 * Factory class responsible of creating message message expiry task objects.
 */
public class MessageExpiryTaskFactory {

    private final int expiryCheckBatchSize;
    private boolean moveToDLX;
    private DLXMover dlxMover;

    MessageExpiryTaskFactory(BrokerCoreConfiguration.MessageExpiryTask config, DLXMover dlxMover) {
        this.expiryCheckBatchSize = Integer.parseInt(config.getExpiryCheckBatchSize());
        this.moveToDLX = Boolean.parseBoolean(config.getMoveToDLX());
        this.dlxMover = dlxMover;
    }

    public Task create(QueueHandler queueHandler) {
        //TODO: queue-wise we can configure if to move to DLX/delete
        if (moveToDLX) {
            return new ExpiredMessageDLXMoveTask(queueHandler, expiryCheckBatchSize, dlxMover);
        } else {
            return new ExpiredMessageRemoveTask(queueHandler, expiryCheckBatchSize);
        }
    }
}
