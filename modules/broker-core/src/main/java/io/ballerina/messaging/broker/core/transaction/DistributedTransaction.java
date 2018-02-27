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
import io.ballerina.messaging.broker.core.Message;

import java.util.Objects;
import javax.transaction.xa.Xid;

/**
 * Distributed transactional operation handle in this implementation.
 */
public class DistributedTransaction implements BrokerTransaction {

    private static final String SAME_XID_ERROR_MSG = "Branch not found with xid ";

    private final BranchFactory branchFactory;

    private final Registry transactionRegistry;

    private Branch currentBranch;

    public DistributedTransaction(BranchFactory branchFactory, Registry transactionRegistry) {

        this.branchFactory = branchFactory;
        this.transactionRegistry = transactionRegistry;
        this.currentBranch = null;
    }

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
    public void onClose() {
    }

    @Override
    public void start(Xid xid, int sessionId, boolean join, boolean resume) throws ValidationException {
        if (join && resume) {
            throw new ValidationException("Cannot start a branch with both join and resume set " + xid);
        }

        Branch branch = transactionRegistry.getBranch(xid);
        if (join) {
            if (Objects.isNull(branch)) {
                throw new ValidationException(SAME_XID_ERROR_MSG + xid);
            }
            this.currentBranch = branch;
            currentBranch.associateSession(sessionId);
        } else if (resume) {
            if (Objects.isNull(branch)) {
                throw new ValidationException(SAME_XID_ERROR_MSG + xid);
            }

            this.currentBranch = branch;
            branch.resumeSession(sessionId);
        } else {
            if (Objects.nonNull(branch)) {
                throw new ValidationException("Xid " + xid + " cannot be started as it is already known");
            }
            branch = branchFactory.createBranch(xid);
            transactionRegistry.register(branch);
            branch.associateSession(sessionId);
            this.currentBranch = branch;
        }
    }

    @Override
    public void end(Xid xid, int sessionId, boolean fail, boolean suspend) throws ValidationException {
        Branch branch = transactionRegistry.getBranch(xid);

        if (Objects.isNull(branch)) {
            throw new ValidationException(SAME_XID_ERROR_MSG + xid);
        }

        if (suspend && fail) {
            branch.disassociateSession(sessionId);
            this.currentBranch = null;
            throw new ValidationException("Cannot end a branch with both suspend and fail set " + xid);
        } else if (!branch.isAssociated(sessionId)) {
            throw new ValidationException("Xid " + xid + " not associated with the current session");
        } else if (suspend) {
            branch.suspendSession(sessionId);
        } else {
            if (fail) {
                branch.setState(Branch.State.ROLLBACK_ONLY);
            }
            branch.disassociateSession(sessionId);
        }
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
