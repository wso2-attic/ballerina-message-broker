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
import io.ballerina.messaging.broker.core.store.TransactionData;

import java.util.Collection;
import java.util.Map;
import javax.transaction.xa.Xid;

/**
 * Defines a functionality required for manipulating messages in persistent storage.
 */
public interface MessageDao {

    /**
     * Update database with message storing, deleting and detaching from queue operations.
     * All operations are done in a single transaction.
     *
     * @param transactionData {@link TransactionData} object which transactional operations list
     */
    void persist(TransactionData transactionData) throws BrokerException;

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

    /**
     * Store transaction data in a temporary table until subsequent commit or rollback is issued.
     *
     * @param xid {@link Xid} to identify the transaction
     * @param transactionData relevant data of the transaction
     */
    void prepare(Xid xid, TransactionData transactionData) throws BrokerException;
}
