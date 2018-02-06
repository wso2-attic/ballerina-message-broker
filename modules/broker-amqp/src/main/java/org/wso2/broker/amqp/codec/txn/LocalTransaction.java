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

package org.wso2.broker.amqp.codec.txn;

import org.wso2.broker.core.Message;
import org.wso2.broker.core.queue.Queue;

import java.util.ArrayList;
import java.util.List;

/**
 * Transactional enqueue or dequeue operation handle in this implementation. The caller responsible for invoking
 * commit or rollback.
 */
public class LocalTransaction implements BrokerTransaction {

    private final List<Action> postTransactionActions = new ArrayList<>();

    @Override
    public void dequeue(List<Message> messageList, Action postTransactionAction) {
        postTransactionActions.add(postTransactionAction);
        //get database transaction
        //delete messages from the table
    }

    @Override
    public void enqueue(Queue queue, Message message, Action postTransactionAction) {
        //get database transaction
        //insert message into the table
    }

    @Override
    public void commit() {
        //get database transaction
        //commit transaction
        //do the post commit if any
        doPostCommit();
    }

    @Override
    public void rollback() {
        //get database transaction
        //rollback transaction
        //od the on rollback if any
        doOnRollback();
    }

    /**
     * Actions to be perform after commit
     */
    private void doPostCommit() {
        for (Action postTransactionAction : postTransactionActions) {
            postTransactionAction.postCommit();
        }
    }

    /**
     * Actions to be perform after rollback
     */
    private void doOnRollback() {
        for (Action postTransactionAction : postTransactionActions) {
            postTransactionAction.onRollback();
        }
    }
}
