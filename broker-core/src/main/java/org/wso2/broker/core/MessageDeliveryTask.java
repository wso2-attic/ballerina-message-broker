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

package org.wso2.broker.core;

import org.wso2.broker.core.task.Task;

/**
 * Delivers messages to consumers for a given queueHandler.
 */
final class MessageDeliveryTask extends Task {

    private final QueueHandler queueHandler;

    MessageDeliveryTask(QueueHandler queueHandler) {
        this.queueHandler = queueHandler;
    }

    @Override
    public void onAdd() {

    }

    @Override
    public void onRemove() {

    }

    @Override
    public String getId() {
        return queueHandler.getName();
    }

    @Override
    public TaskHint call() throws Exception {
        CyclicConsumerIterator consumerIterator = queueHandler.getCyclicConsumerIterator();
        if (!consumerIterator.hasNext()) {
            return TaskHint.IDLE;
        }

        Message message = queueHandler.dequeue();
        int deliveredCount = 0;
        while (message != null) {
            Consumer consumer = consumerIterator.next();
            // TODO: handle send errors
            consumer.send(message);
            deliveredCount++;

            // TODO: make the value configurable
            if (deliveredCount == 1000) {
                break;
            }
        }
        if (deliveredCount > 0) {
            return TaskHint.ACTIVE;
        } else {
            return TaskHint.IDLE;
        }
    }
}
