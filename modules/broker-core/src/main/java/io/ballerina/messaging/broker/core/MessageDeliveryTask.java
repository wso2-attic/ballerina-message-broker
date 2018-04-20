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
import io.ballerina.messaging.broker.core.util.MessageTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delivers messages to consumers for a given queueHandler.
 */
final class MessageDeliveryTask extends Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDeliveryTask.class);

    private final QueueHandler queueHandler;

    private final int deliveryBatchSize;

    MessageDeliveryTask(QueueHandler queueHandler, int deliveryBatchSize) {
        this.queueHandler = queueHandler;
        this.deliveryBatchSize = deliveryBatchSize;
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
        return queueHandler.getUnmodifiableQueue().getName();
    }

    @Override
    public TaskHint call() throws Exception {
        CyclicConsumerIterator consumerIterator = queueHandler.getCyclicConsumerIterator();
        if (!consumerIterator.hasNext()) {
            return TaskHint.IDLE;
        }

        int deliveredCount = 0;
        Consumer previousConsumer = null;
        while (true) {
            Consumer consumer = consumerIterator.next();

            if (!consumer.isReady()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Consumer {} is not ready for consuming messages from {}",
                                 consumer,
                                 queueHandler.getUnmodifiableQueue().getName());
                }

                if (consumer.equals(previousConsumer)) {
                    return TaskHint.IDLE;
                } else {
                    previousConsumer = consumer;
                    continue;
                }
            } else {
                // TODO: handle send errors
                Message message = queueHandler.takeForDelivery();
                if (message != null) {
                    LOGGER.debug("Sending message {} to {}", message, consumer);
                    MessageTracer.trace(message, queueHandler, MessageTracer.DELIVER);
                    consumer.send(message);
                    deliveredCount++;
                    if (deliveredCount == deliveryBatchSize) {
                        break;
                    }
                } else {
                    // We need to break the while loop if there are no messages in the queue
                    break;
                }
            }

            previousConsumer = consumer;
        }
        if (deliveredCount > 0) {
            return TaskHint.ACTIVE;
        } else {
            return TaskHint.IDLE;
        }
    }
}
