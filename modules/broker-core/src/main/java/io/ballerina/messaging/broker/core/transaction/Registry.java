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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.concurrent.ThreadSafe;
import javax.transaction.xa.Xid;

import static io.ballerina.messaging.broker.core.transaction.DistributedTransaction.UNKNOWN_XID_ERROR_MSG;

/**
 * Manages {@link Branch} objects related to transactions.
 */
@ThreadSafe
public class Registry {

    private static final String ASSOCIATED_XID_ERROR_MSG = "Branch still has associated active sessions for xid ";

    private final Map<Xid, Branch> branchMap;

    Registry() {
        branchMap = new ConcurrentHashMap<>();
    }

    public void register(Branch branch) throws ValidationException {
        if (Objects.nonNull(branchMap.putIfAbsent(branch.getXid(), branch))) {
            throw new ValidationException("Branch with the same xid " + branch.getXid() + " is already registered.");
        }
    }

    public void unregister(Xid xid) {
        branchMap.remove(xid);
    }

    public Branch getBranch(Xid xid) {
        return branchMap.get(xid);
    }

    public synchronized void prepare(Xid xid) throws ValidationException, BrokerException {
        Branch branch = branchMap.get(xid);
        if (Objects.isNull(branch)) {
            throw new ValidationException(UNKNOWN_XID_ERROR_MSG + xid);
        }

        if (branch.hasAssociatedActiveSessions()) {
            throw new ValidationException(ASSOCIATED_XID_ERROR_MSG + xid);
        }

        branch.clearAssociations();

        if (branch.getState() == Branch.State.ROLLBACK_ONLY) {
            throw new ValidationException("Transaction can only be rollbacked");
        } else if (branch.getState() != Branch.State.ACTIVE) {
            throw new ValidationException("Cannot prepare a branch in state " + branch.getState());
        }

        branch.setState(Branch.State.PRE_PREPARE);
        branch.prepare();
        branch.setState(Branch.State.PREPARED);
    }

    public synchronized void commit(Xid xid, boolean onePhase) throws ValidationException, BrokerException {
        Branch branch = branchMap.get(xid);
        validateCommitRequest(xid, onePhase, branch);
        branch.clearAssociations();
        branch.setState(Branch.State.FORGOTTEN);
        branch.commit(onePhase);
        unregister(xid);
    }

    private void validateCommitRequest(Xid xid, boolean onePhase, Branch branch) throws ValidationException {
        if (Objects.isNull(branch)) {
            throw new ValidationException(UNKNOWN_XID_ERROR_MSG + xid);
        } else if (branch.hasAssociatedActiveSessions()) {
            throw new ValidationException(ASSOCIATED_XID_ERROR_MSG + xid);
        } else if (branch.getState() == Branch.State.ROLLBACK_ONLY) {
            throw new ValidationException("Branch is set to rollback only. Can't commit with xid " + xid);
        } else if (onePhase && branch.getState() == Branch.State.PREPARED) {
            throw new ValidationException("Cannot call one-phase commit on a prepared branch for xid " + xid);
        } else if (!onePhase && branch.getState() != Branch.State.PREPARED) {
            throw new ValidationException("Cannot call two-phase commit on a non-prepared branch for xid " + xid);
        }
    }

    public synchronized void rollback(Xid xid) throws ValidationException, BrokerException {
        Branch branch = branchMap.get(xid);
        if (Objects.isNull(branch)) {
            throw new ValidationException(UNKNOWN_XID_ERROR_MSG + xid);
        }

        if (branch.hasAssociatedActiveSessions()) {
            throw new ValidationException(ASSOCIATED_XID_ERROR_MSG + xid);
        }

        branch.clearAssociations();
        branch.dtxRollback();
        branch.setState(Branch.State.FORGOTTEN);
        unregister(xid);
    }

    public void forget(Xid xid) throws ValidationException {
        Branch branch = branchMap.get(xid);
        if (Objects.isNull(branch)) {
            throw new ValidationException(UNKNOWN_XID_ERROR_MSG + xid);
        }

        synchronized (branch) {
            if (branch.hasAssociatedActiveSessions()) {
                throw new ValidationException(ASSOCIATED_XID_ERROR_MSG + xid);
            }

            if (branch.getState() != Branch.State.HEUR_COM && branch.getState() != Branch.State.HEUR_RB) {
                throw new ValidationException("Branch is not heuristically complete, "
                                                      + "hence unable to forget. Xid " + xid);
            }

            branch.setState(Branch.State.FORGOTTEN);
            unregister(xid);
        }
    }
}
