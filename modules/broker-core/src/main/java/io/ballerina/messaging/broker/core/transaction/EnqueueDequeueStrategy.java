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

package io.ballerina.messaging.broker.core.transaction;

import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Message;

/**
 * Interface to implement relevant transactional or non transactional
 * message enqueue dequeue strategy.
 */
public interface EnqueueDequeueStrategy {

    /**
     * Depending on the enqueue mode, transactional or non transactional mode, the enqueue strategy changes. For
     * instance add the messages to the underlying queue when non transactional. Add the message to a temporary store
     * when transactional
     *
     * @param message Incoming {@link Message}
     * @throws BrokerException throws when enqueue operation fail
     */
    void enqueue(Message message) throws BrokerException;

    /**
     * Depending on the dequeue mode,  transactional or non transactional mode, the dequeue strategy changes. For
     * instance messages are removed from the queue when non transactiona. Move the message to a temporary store when
     * transactional.
     *
     * @param queueName name of the relevant queue
     * @param detachableMessage {@link Message} to be removed
     * @throws BrokerException throws on dequeue operation failure
     */
    void dequeue(String queueName, DetachableMessage detachableMessage) throws BrokerException;
}
