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
import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.store.MessageStore;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.transaction.xa.Xid;

/**
 * Creates transactional branches for the broker.
 */
public class BranchFactory {

    private final Broker broker;
    private final MessageStore messageStore;
    private final EnqueueDequeueStrategy enqueueDequeueStrategy;


    BranchFactory(Broker broker, MessageStore messageStore) {
        this.broker = broker;
        this.messageStore = messageStore;
        this.enqueueDequeueStrategy = new DirectEnqueueDequeueStrategy(broker);
    }

    public Branch createBranch() {
        // TODO: improve Xid generation logic
        Xid xid = new XidImpl(0,
                              UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8),
                              "".getBytes(StandardCharsets.UTF_8));
        return createBranch(xid);
    }

    public Branch createBranch(Xid xid) {
        return new Branch(xid, messageStore, broker);
    }

    public EnqueueDequeueStrategy getDirectEnqueueDequeueStrategy() {
        return enqueueDequeueStrategy;
    }

    /**
     * Strategy to directly publish to the broker without going through the transactional
     * enqueue dequeue message flow.
     */
    private static class DirectEnqueueDequeueStrategy implements EnqueueDequeueStrategy {

        final Broker broker;

        private DirectEnqueueDequeueStrategy(Broker broker) {
            this.broker = broker;
        }

        @Override
        public void enqueue(Message message) throws BrokerException {
            broker.publish(message);
        }

        @Override
        public void dequeue(String queueName, DetachableMessage detachableMessage) throws BrokerException {
            broker.acknowledge(queueName, detachableMessage);
        }
    }
}
