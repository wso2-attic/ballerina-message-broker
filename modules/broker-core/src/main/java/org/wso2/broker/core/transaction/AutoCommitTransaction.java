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
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Message;

/**
 * Non transactional enqueue or dequeue operation handle in this implementation. No effect on commit or rollback
 * operation.
 */
public class AutoCommitTransaction implements BrokerTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoCommitTransaction.class);
    private final Broker broker;

    public AutoCommitTransaction(Broker broker) {
        this.broker = broker;
    }

    @Override
    public void dequeue(String queue, Message message, Action postTransactionAction) throws BrokerException {
        try {
            broker.acknowledge(queue, message);
        } finally {
            postTransactionAction.postCommit();
        }
    }

    @Override
    public void enqueue(Message message, Action postTransactionAction) throws BrokerException {
        broker.publish(message);
    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }

    @Override
    public boolean isTransactional() {
        return false;
    }
}
