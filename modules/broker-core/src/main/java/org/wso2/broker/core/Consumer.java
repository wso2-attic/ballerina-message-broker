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

/**
 * Interface representing broker subscription.
 */
public interface Consumer {

    /**
     * Send message to the consumer
     *
     * @param message {@link Message} to be sent to the consumer
     * @param deliveryTag unique identification used track the acknowledgment for the sent message
     * @throws BrokerException throws {@link BrokerException} on message sending failure
     */
    void send(Message message, long deliveryTag) throws BrokerException;

    /**
     * Queue name of the subscriber queue
     *
     * @return queue name
     */
    String getSubscribedQueue();

    /**
     * Close the underlying transport consumer
     *
     * @throws BrokerException
     */
    void close() throws BrokerException;

    /**
     * Identifier for the consumer. This consumer tag should be local to a channel.
     * @return
     */
    String getConsumerTag();

    /**
     * If true only this consumer can access the queue and consume messages
     *
     * @return True if the consumer is exclusive. False otherwise
     */
    boolean isExclusive();
}