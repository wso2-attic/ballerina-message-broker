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

package io.ballerina.messaging.broker.core.store.dao.impl;

import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.store.TransactionData;
import io.ballerina.messaging.broker.core.store.dao.MessageDao;

import java.util.Collection;
import java.util.Map;
import javax.transaction.xa.Xid;

/**
 * Implements functionality required to manage messages in persistence storage.
 */
class MessageDaoImpl implements MessageDao {

    private final MessageCrudOperationsDao crudOperationsDao;

    private final DtxCrudOperationsDao dtxCrudOperationsDao;

    MessageDaoImpl(MessageCrudOperationsDao crudOperationsDao, DtxCrudOperationsDao dtxCrudOperationsDao) {
        this.crudOperationsDao = crudOperationsDao;
        this.dtxCrudOperationsDao = dtxCrudOperationsDao;
    }

    @Override
    public void persist(TransactionData transactionData) throws BrokerException {
        crudOperationsDao.transaction(connection -> {
            crudOperationsDao.persist(connection, transactionData.getEnqueueMessages());
            crudOperationsDao.detachFromQueue(connection, transactionData.getDetachMessageMap());
            crudOperationsDao.delete(connection, transactionData.getDeletableMessage());
        });
    }

    @Override
    public Collection<Message> readAll(String queueName) throws BrokerException {
        return crudOperationsDao.selectOperation(connection -> crudOperationsDao.readAll(connection, queueName),
                                                 "retrieving messages for queue " + queueName);
    }

    @Override
    public Collection<Message> read(Map<Long, Message> readList) throws BrokerException {
        return crudOperationsDao.selectOperation(connection -> crudOperationsDao.read(connection, readList),
                                                 "retrieving messages for delivery");
    }

    @Override
    public void prepare(Xid xid, TransactionData transactionData) throws BrokerException {
        dtxCrudOperationsDao.transaction(connection -> {
            long internalXid = dtxCrudOperationsDao.storeXid(connection, xid);
            dtxCrudOperationsDao.prepareEnqueueMessages(connection, internalXid, transactionData.getEnqueueMessages());
            dtxCrudOperationsDao.prepareDetachMessages(connection, internalXid, transactionData.getDetachMessageMap());
        });
    }
}
