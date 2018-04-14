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
 * Provide standard interface to handle enqueue/dequeue/commit/rollback operation based on underlying
 * transaction object.
 */
public interface BrokerTransaction {

    /**
     * An action to be perform on transaction commit or rollback.
     */
    interface Action {

        /**
         * Execute actions after commit operation.
         */
        void postCommit();

        /**
         * Execute actions after rollback operation.
         */
        void onRollback();


    }
    /**
     * Dequeue a message from queue.
     * @param queue Acknowledgment received queue name
     * @param detachableMessage An acknowledgement message
     */
    void dequeue(String queue, DetachableMessage detachableMessage) throws BrokerException;
    /**
     * Enqueue a message to a queue.
     *
     * @param message A message publish to a routing key
     */
    void enqueue(Message message) throws BrokerException;

    /**
     * Commit the transaction represent by this object.
     */
    void commit() throws ValidationException, BrokerException;

    /**
     * Rollback the transaction represent by this object.
     */
    void rollback() throws ValidationException;

    /**
     * Actions to be perform after commit or rollback.
     *
     * @param postTransactionAction action to be perform after commit or rollback
     */
    void addPostTransactionAction(Action postTransactionAction);

    /**
     * Actions to be performed on a transaction close.
     */
    void onClose();

    /**
     * Start a transaction branch.
     *
     * @param xid Start any work associated with transaction branch with Xid
     * @param sessionId unique id for the transaction session
     * @param join Indicate whether this is joining an already associated  Xid
     * @param resume Indicate whether this is resuming a suspended transaction branch
     */
    void start(Xid xid, int sessionId, boolean join, boolean resume) throws ValidationException;

    /**
     * End a transaction branch.
     *
     *  @param xid End any work associated with transaction branch with Xid
     *  @param sessionId unique id for the transaction session
     *  @param fail Indicate whether the portion of work has failed
     *  @param suspend Indicate that the transaction branch is temporarily suspended in an incomplete state
     */
    void end(Xid xid, int sessionId, boolean fail, boolean suspend) throws ValidationException;

    /**
     * Ask to prepare a transaction branch.
     *
     * @param xid Prepare for commitment any work associated with Xid
     */
    void prepare(Xid xid) throws ValidationException, BrokerException;

    /**
     * Commit the work done on behalf a transaction branch.
     *
     * @param xid Commit the work associated with Xid
     * @param onePhase Indicate that one-phase optimization must be used
     */
    void commit(Xid xid, boolean onePhase) throws ValidationException, BrokerException;

    /**
     * Rollback a transaction branch.
     *
     * @param xid Rollback any work associated with Xid
     */
    void rollback(Xid xid) throws ValidationException, BrokerException;

    /**
     * Discard knowledge of a heuristically-completed transaction branch.
     *
     * @param xid Erase RM its knowledge of Xid
     */
    void forget(Xid xid) throws ValidationException;

    /**
     * Set the transaction timeout value.
     *
     * If the transaction is still in the "active" state after this time, it is automatically rolled back.
     * Once the transaction moves on to the prepared state, however, this timeout parameter does not apply.
     *
     * @param xid Xid of the branch to set the timeout value
     * @param timeout The transaction timeout value
     * @param timeUnit {@link TimeUnit} of the provided timeout
     */
    void setTimeout(Xid xid, long timeout, TimeUnit timeUnit) throws ValidationException;

    /**
     * Returns true if the transaction object is within a transaction block.
     *
     * @return true if within a transaction block, false otherwise.
     */
    boolean inTransactionBlock();
}
