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

import io.ballerina.messaging.broker.common.DaoException;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.store.TransactionData;
import io.ballerina.messaging.broker.core.store.dao.MessageDao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.transaction.xa.Xid;

/**
 * Implements functionality required to manage messages in persistence storage.
 */
class MessageDaoImpl implements MessageDao {

    private static final long INVALID_XID = -1;

    private final MessageCrudOperationsDao crudOperationsDao;

    private final DtxCrudOperationsDao dtxCrudOperationsDao;

    private final Map<Xid, Long> xidToInternalIdMap;

    MessageDaoImpl(MessageCrudOperationsDao crudOperationsDao, DtxCrudOperationsDao dtxCrudOperationsDao) {
        this.crudOperationsDao = crudOperationsDao;
        this.dtxCrudOperationsDao = dtxCrudOperationsDao;
        this.xidToInternalIdMap = new ConcurrentHashMap<>();
    }

    @Override
    public void persist(TransactionData transactionData) throws BrokerException {
        try {
            crudOperationsDao.transaction(connection -> {
                crudOperationsDao.storeMessages(connection, transactionData.getEnqueueMessages());
                crudOperationsDao.detachFromQueue(connection, transactionData.getDetachMessageMap());
                crudOperationsDao.delete(connection, transactionData.getDeletableMessage());
            });
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    @Override
    public Collection<Message> readAll(String queueName) throws BrokerException {
        try {
            return crudOperationsDao.selectAndGetOperation(connection ->
                    crudOperationsDao.readAll(connection, queueName));
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void read(Map<Long, List<Message>> readList) throws BrokerException {
        try {
            crudOperationsDao.selectOperation(connection -> crudOperationsDao.read(connection, readList));
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void prepare(Xid xid, TransactionData transactionData) throws BrokerException {
        try {
            dtxCrudOperationsDao.transaction(connection -> {
                long internalXid = dtxCrudOperationsDao.storeXid(connection, xid);
                dtxCrudOperationsDao.prepareEnqueueMessages(connection, internalXid,
                        transactionData.getEnqueueMessages());
                dtxCrudOperationsDao.prepareDetachMessages(connection, internalXid,
                        transactionData.getDetachMessageMap());
                crudOperationsDao.detachFromQueue(connection, transactionData.getDetachMessageMap());
                xidToInternalIdMap.put(xid, internalXid);
            });
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
    }

    @Override
    public void commitPreparedData(Xid xid, TransactionData transactionData) throws BrokerException {

        try {
            dtxCrudOperationsDao.transaction(connection -> {
                long internalXid = getInternalXid(xid);
                dtxCrudOperationsDao.copyEnqueueMessages(connection, internalXid);
                crudOperationsDao.delete(connection, transactionData.getDeletableMessage());
                dtxCrudOperationsDao.removePreparedData(connection, internalXid);
            });
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
        xidToInternalIdMap.remove(xid);
    }

    @Override
    public void rollbackPreparedData(Xid xid) throws BrokerException {
        try {
            dtxCrudOperationsDao.transaction(connection -> {
                long internalXid = getInternalXid(xid);
                if (internalXid != INVALID_XID) {
                    dtxCrudOperationsDao.restoreDequeueMessages(connection, internalXid);
                    dtxCrudOperationsDao.removePreparedData(connection, internalXid);
                }
            });
        } catch (DaoException e) {
            throw new BrokerException(e.getMessage(), e);
        }
        xidToInternalIdMap.remove(xid);
    }

    private long getInternalXid(Xid xid) {
        Long id = xidToInternalIdMap.get(xid);
        if (Objects.isNull(id)) {
            return INVALID_XID;
        }
        return id;
    }
}
