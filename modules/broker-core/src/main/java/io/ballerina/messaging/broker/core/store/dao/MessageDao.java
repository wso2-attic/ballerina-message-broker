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
import java.util.List;
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
    void read(Map<Long, List<Message>> readList) throws BrokerException;

    /**
     * Store transaction data in a separate table until subsequent commit or rollback is issued.
     *
     * @param xid {@link Xid} to identify the transaction
     * @param transactionData relevant data of the transaction
     */
    void prepare(Xid xid, TransactionData transactionData) throws BrokerException;

    /**
     * Move data from prepared data tables to persistence storage.
     *
     * @param xid {@link Xid} of the transaction
     * @param transactionData {@link TransactionData} object relating to the transaction
     * @throws BrokerException throws exception on persistence failure.
     */
    void commitPreparedData(Xid xid, TransactionData transactionData) throws BrokerException;

    /**
     * If prepared data exist, revert the requested operations in prepare stage.
     *
     * @param xid {@link Xid} of the rollback operation related transaction
     */
    void rollbackPreparedData(Xid xid) throws BrokerException;
}
