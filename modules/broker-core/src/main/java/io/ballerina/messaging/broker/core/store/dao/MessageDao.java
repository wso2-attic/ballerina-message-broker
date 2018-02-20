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

package io.ballerina.messaging.broker.core.store.dao;

import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.store.DbOperation;

import java.util.Collection;
import java.util.Map;

/**
 * Defines a functionality required for manipulating messages in persistent storage.
 */
public interface MessageDao {

    /**
     * Storage a message in the persistent storage.
     * 
     * @param messageList the messages to persist.
     */
    void persist(Collection<Message> messageList) throws BrokerException;

    /**
     * Removes the linkage a messages has with given queue. after the removal if there are links to any other queues
     * this message should be deleted automatically.
     * 
     * @param dbOperations {@link DbOperation} objects which contain the queue names and the message ids to detach.
     */
    void detachFromQueue(Collection<DbOperation> dbOperations) throws BrokerException;

    /**
     * Deletes a given message and its associations to a queues.
     *
     * @param messageId internal message ids
     */
    void delete(Collection<Long> messageId) throws BrokerException;

    /**
     * Retrieve all messages from a given queue.
     *
     * @param queueName name of the queue.
     */
    Collection<Message> readAll(String queueName) throws BrokerException;

    /**
     * Read message data for given messages.
     *
     * @param readList list of messages.
     */
    Collection<Message> read(Map<Long, Message> readList) throws BrokerException;
}
