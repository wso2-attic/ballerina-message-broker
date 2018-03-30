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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interface representing broker subscription.
 */
public abstract class Consumer {

    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    private final int id;

    public Consumer() {
        this.id = idGenerator.incrementAndGet();
    }

    public final int getId() {
        return id;
    }

    /**
     * Send message to the consumer.
     *
     * @param message {@link Message} to be sent to the consumer
     * @throws BrokerException throws {@link BrokerException} on message sending failure
     */
    protected abstract void send(Message message) throws BrokerException;

    /**
     * Queue name of the subscriber queue.
     *
     * @return queue name
     */
    public abstract String getQueueName();

    /**
     * Close the underlying transport consumer.
     *
     * @throws BrokerException
     */
    protected abstract void close() throws BrokerException;

    /**
     * If true only this consumer can access the queue and consume messages.
     *
     * @return True if the consumer is exclusive. False otherwise
     */
    public abstract boolean isExclusive();

    /**
     * Indicate if consumer is ready to receive messages.
     *
     * @return true if the consumer can receive messages, false otherwise
     */
    public abstract boolean isReady();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Consumer) {
            return id == ((Consumer) obj).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
