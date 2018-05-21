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

package io.ballerina.messaging.broker.core;

import java.util.Set;
import javax.transaction.xa.Xid;

/**
 * Abstract class to represent an underlying queue for the broker.
 */
public abstract class Queue {

    /**
     * Unbounded queue.
     */
    public static final int UNBOUNDED = Integer.MAX_VALUE;

    private final String name;

    private final boolean durable;

    private final boolean autoDelete;

    private QueueHandler queueHandler;

    public Queue(String queueName, boolean durable, boolean autoDelete) {
        this.name = queueName;
        this.durable = durable;
        this.autoDelete = autoDelete;
    }

    public final String getName() {
        return name;
    }

    public final boolean isDurable() {
        return durable;
    }

    public final boolean isAutoDelete() {
        return autoDelete;
    }

    public QueueHandler getQueueHandler() {
        return queueHandler;
    }

    final void setQueueHandler(QueueHandler queueHandler) {
        this.queueHandler = queueHandler;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Queue) {
            return name.equals(((Queue) obj).name);
        }

        return false;
    }

    @Override
    public String toString() {
        return "Queue{"
                + "name='" + name + '\''
                + ", durable=" + durable
                + ", autoDelete=" + autoDelete
                + '}';
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    public abstract int capacity();

    public abstract int size();

    public abstract boolean enqueue(Message message) throws BrokerException;

    public abstract void prepareEnqueue(Xid xid, Message message) throws BrokerException;

    public abstract void commit(Xid xid);

    public abstract void rollback(Xid xid);

    public abstract Message dequeue();

    public abstract void getExpired(Set<Message> expiredMessages, int capacity);

    public abstract void detach(DetachableMessage detachableMessage) throws BrokerException;

    public abstract void prepareDetach(Xid xid, DetachableMessage detachableMessage) throws BrokerException;

    public abstract int clear();
}
