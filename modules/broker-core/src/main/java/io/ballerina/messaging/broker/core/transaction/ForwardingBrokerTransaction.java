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

import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Message;

import java.util.concurrent.TimeUnit;
import javax.transaction.xa.Xid;

/**
 * Follows forwarding pattern to invoke broker transaction operations.
 */
public class ForwardingBrokerTransaction implements BrokerTransaction {


    private BrokerTransaction brokerTransaction;

    public ForwardingBrokerTransaction(BrokerTransaction brokerTransaction) {
        this.brokerTransaction = brokerTransaction;
    }

    @Override
    public void dequeue(String queue, DetachableMessage detachableMessage) throws BrokerException {
        brokerTransaction.dequeue(queue, detachableMessage);
    }

    @Override
    public void enqueue(Message message) throws BrokerException {
        brokerTransaction.enqueue(message);
    }

    @Override
    public void commit() throws ValidationException, BrokerException {
        brokerTransaction.commit();
    }

    @Override
    public void rollback() throws ValidationException {
        brokerTransaction.rollback();
    }

    @Override
    public void addPostTransactionAction(Action postTransactionAction) {
        brokerTransaction.addPostTransactionAction(postTransactionAction);
    }

    @Override
    public void onClose() {
        brokerTransaction.onClose();
    }

    @Override
    public void start(Xid xid, int sessionId, boolean join, boolean resume) throws ValidationException {
        brokerTransaction.start(xid, sessionId, join, resume);
    }

    @Override
    public void end(Xid xid, int sessionId, boolean fail, boolean suspend) throws ValidationException {
        brokerTransaction.end(xid, sessionId, fail, suspend);
    }

    @Override
    public void prepare(Xid xid) throws ValidationException, BrokerException {
        brokerTransaction.prepare(xid);
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws ValidationException, BrokerException {
        brokerTransaction.commit(xid, onePhase);
    }

    @Override
    public void rollback(Xid xid) throws ValidationException, BrokerException {
        brokerTransaction.rollback(xid);
    }

    @Override
    public void forget(Xid xid) throws ValidationException {
        brokerTransaction.forget(xid);
    }

    @Override
    public void setTimeout(Xid xid, long timeout, TimeUnit timeUnit) throws ValidationException {
        brokerTransaction.setTimeout(xid, timeout, timeUnit);
    }

    @Override
    public boolean inTransactionBlock() {
        return brokerTransaction.inTransactionBlock();
    }
}
