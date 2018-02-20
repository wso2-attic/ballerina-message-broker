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

package org.wso2.broker.core.transaction;

import org.wso2.broker.common.ValidationException;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Message;

import javax.transaction.xa.Xid;

/**
 * Distributed transactional operation handle in this implementation.
 */
public class DistributedTransaction implements BrokerTransaction {

    @Override
    public void dequeue(String queue, Message message) throws BrokerException {

    }

    @Override
    public void enqueue(Message message) throws BrokerException {

    }

    @Override
    public void commit() throws ValidationException {
        throw new ValidationException("tx.commit called on distributed-transactional channel");
    }

    @Override
    public void rollback() throws ValidationException {
        throw new ValidationException("tx.rollback called on distributed-transactional channel");
    }

    @Override
    public void addPostTransactionAction(Action postTransactionAction) {

    }

    @Override
    public boolean isTransactional() {
        return false;
    }

    @Override
    public void start(Xid xid, boolean join, boolean resume) {

    }

    @Override
    public void end(Xid xid, boolean fail, boolean suspend) {

    }

    @Override
    public void prepare(Xid xid) {

    }

    @Override
    public void commit(Xid xid, boolean onePhase) {

    }

    @Override
    public void rollback(Xid xid) {

    }

    @Override
    public void forget(Xid xid) {

    }

    @Override
    public void setTimeout(Xid xid, long timeout) {

    }
}
