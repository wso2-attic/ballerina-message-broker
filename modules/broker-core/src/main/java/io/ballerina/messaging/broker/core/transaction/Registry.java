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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.store.MessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;
import javax.transaction.xa.Xid;

import static io.ballerina.messaging.broker.core.transaction.DistributedTransaction.UNKNOWN_XID_ERROR_MSG;

/**
 * Manages {@link Branch} objects related to transactions.
 */
@ThreadSafe
public class Registry {

    private static final Logger LOGGER = LoggerFactory.getLogger(Registry.class);

    private static final String ASSOCIATED_XID_ERROR_MSG = "Branch still has associated active sessions for xid ";

    private static final String TIMED_OUT_ERROR_MSG = "Transaction timed out for xid ";

    /**
     * {@link Xid} to branch mapping of branches in memory.
     */
    private final Map<Xid, Branch> branchMap;

    private final ScheduledExecutorService branchTimeoutExecutorService;

    private final BranchFactory branchFactory;

    /**
     * {@link Xid}s of already prepared branches that are currently not in memory. This can be due to a node fail-over
     * or a node restart.
     */
    private final Set<Xid> storedXidSet;

    Registry(BranchFactory branchFactory) {
        this.branchFactory = branchFactory;
        branchMap = new ConcurrentHashMap<>();
        storedXidSet = ConcurrentHashMap.newKeySet();
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("DtxBranchTimeoutExecutor-%d").build();
        this.branchTimeoutExecutorService = Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    public void register(Branch branch) throws ValidationException {
        if (Objects.nonNull(branchMap.putIfAbsent(branch.getXid(), branch))) {
            throw new ValidationException("Branch with the same xid " + branch.getXid() + " is already registered.");
        }
    }

    public void unregister(Xid xid) {
        if (Objects.isNull(branchMap.remove(xid))) {
            storedXidSet.remove(xid);
        }
    }

    public Branch getBranch(Xid xid) throws ValidationException {
        if (storedXidSet.contains(xid)) {
            throw new ValidationException("Branch is in prepared stage. Branch can be only be "
                                                  + "committed or rollbacked.");
        }
        return branchMap.get(xid);
    }

    public synchronized void prepare(Xid xid) throws ValidationException, BrokerException {
        if (storedXidSet.contains(xid)) {
            throw new DtxStateTransitionException(xid, Branch.State.PREPARED, Branch.State.PREPARED);
        }

        Branch branch = branchMap.get(xid);
        if (Objects.isNull(branch)) {
            throw new ValidationException(UNKNOWN_XID_ERROR_MSG + xid);
        }

        if (branch.hasAssociatedActiveSessions()) {
            throw new ValidationException(ASSOCIATED_XID_ERROR_MSG + xid);
        }

        checkForBranchExpiration(branch);

        branch.clearAssociations();

        if (branch.getState() == Branch.State.ROLLBACK_ONLY) {
            throw new ValidationException("Transaction can only be rollbacked");
        } else if (branch.getState() != Branch.State.ACTIVE) {
            throw new ValidationException("Cannot prepare a branch in state " + branch.getState());
        }
        branch.prepare();
    }

    private void checkForBranchExpiration(Branch branch) throws ValidationException {
        if (branch.isExpired() || !cancelTimeoutTask(branch)) {
            unregister(branch.getXid());
            throw new ValidationException(TIMED_OUT_ERROR_MSG + branch.getXid());
        }
    }

    public synchronized void commit(Xid xid, boolean onePhase) throws ValidationException, BrokerException {
        Branch branch = branchMap.get(xid);
        if (Objects.isNull(branch)) {
            branch = checkForBranchRecovery(xid);
        } else {
            if (branch.hasAssociatedActiveSessions()) {
                throw new ValidationException(ASSOCIATED_XID_ERROR_MSG + xid);
            }
            checkForBranchExpiration(branch);
            if (branch.isRollbackOnly()) {
                throw new ValidationException("Branch is set to rollback only. Can't commit with xid " + xid);
            }
            if (!onePhase && !branch.isPrepared()) {
                throw new ValidationException("Cannot call two-phase commit on a non-prepared branch for xid " + xid);
            }
        }

        if (onePhase && branch.isPrepared()) {
            throw new ValidationException("Cannot call one-phase commit on a prepared branch for xid " + xid);
        }

        branch.clearAssociations();
        branch.commit(onePhase);
        branch.setState(Branch.State.FORGOTTEN);
        unregister(xid);
    }

    private Branch checkForBranchRecovery(Xid xid) throws UnknownDtxBranchException {
        Branch branch;
        if (storedXidSet.contains(xid)) {
            branch = branchFactory.createBranch(xid);
            branch.markAsRecoveryBranch();
        } else {
            throw new UnknownDtxBranchException(xid);
        }
        return branch;
    }

    private boolean cancelTimeoutTask(Branch branch) {
        Future timeoutTaskFuture = branch.getTimeoutTaskFuture();
        return Objects.isNull(timeoutTaskFuture)
                || timeoutTaskFuture.isCancelled()
                || timeoutTaskFuture.cancel(false);
    }

    public synchronized void rollback(Xid xid) throws ValidationException, BrokerException {
        Branch branch = branchMap.get(xid);
        if (Objects.isNull(branch)) {
            branch = checkForBranchRecovery(xid);
        } else {
            checkForBranchExpiration(branch);
            if (branch.hasAssociatedActiveSessions()) {
                throw new ValidationException(ASSOCIATED_XID_ERROR_MSG + xid);
            }
            branch.clearAssociations();
        }
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

    public void setTimeout(Xid xid, long timeout, TimeUnit timeUnit) throws ValidationException {
        Branch branch = branchMap.get(xid);
        if (Objects.isNull(branch)) {
            throw new ValidationException(UNKNOWN_XID_ERROR_MSG + xid);
        }

        if (timeout == 0) {
            return;
        }

        ScheduledFuture<?> future = branchTimeoutExecutorService.schedule(() -> {

            LOGGER.debug("timing out dtx task with xid {}", xid);
            synchronized (branch) {
                if (branch.isPrepared()) {
                    LOGGER.debug("Branch already prepared. Won't be timed out. Xid {}", xid);
                    return;
                }
                try {
                    rollback(xid);
                    branch.setState(Branch.State.TIMED_OUT);
                } catch (ValidationException | BrokerException e) {
                    LOGGER.error("Error occurred while rolling back timed out branch with Xid " + xid, e);
                }
            }

        }, timeout, timeUnit);
        branch.setTimeoutTaskFuture(future);
    }

    void syncWithMessageStore(MessageStore messageStore) throws BrokerException {
        storedXidSet.clear();
        messageStore.retrieveStoredXids(storedXidSet::add);
    }
}
