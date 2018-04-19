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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.transaction.xa.Xid;

/**
 * Transactional enqueue or dequeue operation handle in this implementation. The caller responsible for invoking
 * commit or rollback.
 */
public class LocalTransaction implements BrokerTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalTransaction.class);

    private final List<Action> postTransactionActions = new ArrayList<>();

    private final Registry transactionRegistry;

    private Branch branch;

    private boolean preConditionFailed;

    private StringBuilder errorMessageBuilder;

    private final BranchFactory branchFactory;

    LocalTransaction(Registry registry, BranchFactory branchFactory) {
        this.transactionRegistry = registry;
        preConditionFailed = false;
        this.branchFactory = branchFactory;
        errorMessageBuilder = new StringBuilder();
        branch = null;
    }

    @Override
    public void dequeue(String queueName, DetachableMessage detachableMessage) {
        try {
            createBranchIfNeeded();
            branch.dequeue(queueName, detachableMessage);
        } catch (BrokerException e) {
            preConditionFailed = true;
            errorMessageBuilder.append(e.getMessage()).append('\n');
        }
    }

    @Override
    public void enqueue(Message message) {
        try {
            createBranchIfNeeded();
            branch.enqueue(message);
        } catch (BrokerException e) {
            preConditionFailed = true;
            errorMessageBuilder.append(e.getMessage()).append('\n');
        }
    }

    @Override
    public void commit() throws BrokerException, ValidationException {
        if (preConditionFailed) {
            throw new ValidationException("Pre conditions failed for commit. Errors " + errorMessageBuilder.toString());
        }
        if (Objects.isNull(branch)) {
            LOGGER.debug("Nothing to commit. Transaction branch is null");
            return;
        }

        branch.commit(true);
        doPostCommit();
        clear();
    }

    @Override
    public void rollback() {
        if (Objects.isNull(branch)) {
            LOGGER.debug("Nothing to commit. Transaction branch is null");
            return;
        }
        branch.rollback();
        doOnRollback();
        clear();
    }

    @Override
    public void onClose() {
        rollback();
    }

    @Override
    public void start(Xid xid, int sessionId, boolean join, boolean resume) throws ValidationException {
        throw new ValidationException("dtx.start called on local-transactional channel");
    }

    @Override
    public void end(Xid xid, int sessionId, boolean fail, boolean suspend) throws ValidationException {
        throw new ValidationException("dtx.end called on local-transactional channel");
    }

    @Override
    public void prepare(Xid xid) throws ValidationException {
        throw new ValidationException("dtx.prepare called on local-transactional channel");
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws ValidationException {
        throw new ValidationException("dtx.commit called on local-transactional channel");
    }

    @Override
    public void rollback(Xid xid) throws ValidationException {
        throw new ValidationException("dtx.rollback called on local-transactional channel");
    }

    @Override
    public void forget(Xid xid) throws ValidationException {
        throw new ValidationException("dtx.forget called on local-transactional channel");
    }

    @Override
    public void setTimeout(Xid xid, long timeout, TimeUnit timeUnit) throws ValidationException {
        throw new ValidationException("dtx.set-timeout called on local-transactional channel");
    }

    @Override
    public boolean inTransactionBlock() {
        return true;
    }

    @Override
    public void addPostTransactionAction(Action postTransactionAction) {
        postTransactionActions.add(postTransactionAction);
    }

    /**
     * Execute post transaction action after commit.
     */
    private void doPostCommit() {
        for (Action postTransactionAction : postTransactionActions) {
            postTransactionAction.postCommit();
        }
    }

    private void clear() {
        preConditionFailed = false;
        errorMessageBuilder.setLength(0);
        transactionRegistry.unregister(branch.getXid());
        branch = null;
    }

    /**
     * Execute post transaction action after rollback.
     */
    private void doOnRollback() {
        for (Action postTransactionAction : postTransactionActions) {
            postTransactionAction.onRollback();
        }
    }

    private void createBranchIfNeeded() throws BrokerException {
        if (Objects.nonNull(branch)) {
            return;
        }

        try {
            branch = branchFactory.createBranch();
            transactionRegistry.register(branch);
        } catch (ValidationException e) {
            // Throws BrokerException since this doesn't depend on user input
            throw new BrokerException("Error occurred while registering branch.", e);
        }
    }
}
