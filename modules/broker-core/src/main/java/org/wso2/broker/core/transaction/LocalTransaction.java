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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Transactional enqueue or dequeue operation handle in this implementation. The caller responsible for invoking
 * commit or rollback.
 */
public class LocalTransaction implements BrokerTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalTransaction.class);
    private final List<Action> postTransactionActions = new ArrayList<>();
    private final Broker broker;

    public LocalTransaction(Broker broker) {
        this.broker = broker;
    }


    @Override
    public void dequeue(String queue, Message message) {
        broker.newLocalTransaction();
        //get database connection
        //delete messages from the table
    }

    @Override
    public void enqueue(Message message) {
        //get database connection
        //insert message into the table
    }

    @Override
    public void commit() {
        //get database connection
        //commit transaction
        //do the post commit if any
        doPostCommit();
    }

    @Override
    public void rollback() {
        //get database connection
        //rollback transaction
        //od the on rollback if any
        doOnRollback();
    }

    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public void addPostTransactionAction(Action postTransactionAction) {
        postTransactionActions.add(postTransactionAction);
    }

    /**
     * Execute post transaction action after commit
     */
    private void doPostCommit() {
        for (Action postTransactionAction : postTransactionActions) {
            postTransactionAction.postCommit();
        }
    }

    /**
     * Execute post transaction action after rollback
     */
    private void doOnRollback() {
        for (Action postTransactionAction : postTransactionActions) {
            postTransactionAction.onRollback();
        }
    }
}
