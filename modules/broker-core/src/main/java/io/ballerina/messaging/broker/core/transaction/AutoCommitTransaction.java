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
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Message;

import java.util.concurrent.TimeUnit;
import javax.transaction.xa.Xid;

/**
 * Non transactional enqueue or dequeue operation handle in this implementation. No effect on commit or rollback
 * operation.
 */
public class AutoCommitTransaction implements BrokerTransaction {

    private final Broker broker;

    public AutoCommitTransaction(Broker broker) {
        this.broker = broker;
    }

    @Override
    public void dequeue(String queue, DetachableMessage detachableMessage) throws BrokerException {
        broker.acknowledge(queue, detachableMessage);
    }

    @Override
    public void enqueue(Message message) throws BrokerException {
        broker.publish(message);
    }

    @Override
    public void commit() throws ValidationException {
        throw new ValidationException("tx.commit called on non-transactional channel");
    }

    @Override
    public void rollback() throws ValidationException {
        throw new ValidationException("tx.rollback called on non-transactional channel");
    }

    @Override
    public void addPostTransactionAction(Action postTransactionAction) {
        // ignore
    }

    @Override
    public void onClose() {
        // ignore
    }

    @Override
    public void start(Xid xid, int sessionId, boolean join, boolean resume) throws ValidationException {
        throw new ValidationException("dtx.start called on non-transactional channel");
    }

    @Override
    public void end(Xid xid, int sessionId, boolean fail, boolean suspend) throws ValidationException {
        throw new ValidationException("dtx.end called on non-transactional channel");
    }

    @Override
    public void prepare(Xid xid) throws ValidationException {
        throw new ValidationException("dtx.prepare called on non-transactional channel");
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws ValidationException {
        throw new ValidationException("dtx.commit called on non-transactional channel");
    }

    @Override
    public void rollback(Xid xid) throws ValidationException {
        throw new ValidationException("dtx.rollback called on non-transactional channel");
    }

    @Override
    public void forget(Xid xid) throws ValidationException {
        throw new ValidationException("dtx.forget called on non-transactional channel");
    }

    @Override
    public void setTimeout(Xid xid, long timeout, TimeUnit timeUnit) throws ValidationException {
        throw new ValidationException("dtx.set-timeout called on non-transactional channel");
    }

    @Override
    public boolean inTransactionBlock() {
        return false;
    }


}
