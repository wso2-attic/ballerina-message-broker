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

import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.QueueHandler;
import io.ballerina.messaging.broker.core.store.MessageStore;

import java.util.HashSet;
import java.util.Set;
import javax.transaction.xa.Xid;

/**
 * XA transaction information hold within the broker
 */
public class Branch {

    private Xid xid;

    private final MessageStore messageStore;

    private final Set<QueueHandler> affectedQueueHandlers;

    private final Broker broker;

    public Branch(Xid xid, MessageStore messageStore, Broker broker) {
        this.xid = xid;
        this.messageStore = messageStore;
        this.broker = broker;
        messageStore.branch(xid);
        this.affectedQueueHandlers = new HashSet<>();
    }

    public void enqueue(Message message) throws BrokerException {
        Set<QueueHandler> queueHandlers = broker.prepareEnqueue(xid, message);
        affectedQueueHandlers.addAll(queueHandlers);
    }

    public void dequeue(String queueName, Message message) throws BrokerException {
        QueueHandler queueHandler = broker.prepareDequeue(xid, queueName, message);
        affectedQueueHandlers.add(queueHandler);
    }

    public void commit() throws BrokerException {
        messageStore.flush(xid);
        for (QueueHandler queueHandler: affectedQueueHandlers) {
            queueHandler.commit(xid);
        }
    }

    public void rollback() {
        messageStore.clear(xid);
        for (QueueHandler queueHandler: affectedQueueHandlers) {
            queueHandler.rollback(xid);
        }
    }

    public Xid getXid() {
        return xid;
    }
}
