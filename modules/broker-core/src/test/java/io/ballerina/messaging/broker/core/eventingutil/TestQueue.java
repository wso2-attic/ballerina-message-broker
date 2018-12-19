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

package io.ballerina.messaging.broker.core.eventingutil;

import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Queue;

import javax.transaction.xa.Xid;

public class TestQueue extends Queue {

    private int size;

    public TestQueue(String queueName, boolean durable, boolean autoDelete, int size) {
        super(queueName, durable, autoDelete);
        this.size = size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int capacity() {

        return 10000;
    }

    @Override
    public int size() {

        return size;
    }

    @Override
    public boolean enqueue(Message message) {
        return true;
    }

    @Override
    public void prepareEnqueue(Xid xid, Message message) {
        //No implementation
    }

    @Override
    public void commit(Xid xid) {
        //No implementation
    }

    @Override
    public void rollback(Xid xid) {
        //No implementation
    }

    @Override
    public Message dequeue() {

        return null;
    }

    @Override
    public void detach(DetachableMessage detachableMessage) {
        //No implementation
    }

    @Override
    public void prepareDetach(Xid xid, DetachableMessage detachableMessage) {
        //No implementation
    }

    @Override
    public int clear() {
        return 0;
    }
}
