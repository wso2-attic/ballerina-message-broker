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

import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Message;

/**
 * Provide standard interface to handle enqueue/dequeue/commit/rollback operation based on underlying
 * transaction object.
 */
public interface BrokerTransaction {

    /**
     * An action to be perform on transaction commit or rollback
     */
    interface Action {

        /**
         * Execute actions after commit operation
         */
        void postCommit();

        /**
         * Execute actions after rollback operation
         */
        void onRollback();
    }

    /**
     * Dequeue a message from queue by post transaction action
     */
    void dequeue(String queue, Message message, Action postTransactionAction) throws BrokerException;

    /**
     * Enqueue a message from queue by post transaction action
     */
    void enqueue(Message message, Action postTransactionAction) throws BrokerException;

    /**
     * Commit the transaction represent by this object
     */
    void commit();

    /**
     * Rollback the transaction represent by this object
     */
    void rollback();

    /**
     * Return implementation support transaction
     *
     * @return transaction supported or not
     */
    boolean isTransactional();
}
