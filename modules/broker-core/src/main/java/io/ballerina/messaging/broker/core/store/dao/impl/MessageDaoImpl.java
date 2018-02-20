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
import io.ballerina.messaging.broker.core.store.DbOperation;
import io.ballerina.messaging.broker.core.store.dao.MessageDao;

import java.util.Collection;
import java.util.Map;

/**
 * Implements functionality required to manage messages in persistence storage.
 */
class MessageDaoImpl implements MessageDao {

    private final MessageCrudOperationsDao crudOperationsDao;

    MessageDaoImpl(MessageCrudOperationsDao crudOperationsDao) {
        this.crudOperationsDao = crudOperationsDao;
    }

    @Override
    public void persist(Collection<Message> messageList) throws BrokerException {

        crudOperationsDao.transaction(connection -> crudOperationsDao.persist(connection, messageList),
                                      "persisting messages.");
    }

    @Override
    public void detachFromQueue(Collection<DbOperation> dbOperations) throws BrokerException {
        crudOperationsDao.transaction(connection -> crudOperationsDao.detachFromQueue(connection, dbOperations),
                                      "detaching message from queue");
    }

    @Override
    public void delete(Collection<Long> messageId) throws BrokerException {
        crudOperationsDao.transaction(connection -> crudOperationsDao.delete(connection, messageId),
                                      "deleting messages."
                                     );
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
}
