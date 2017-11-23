/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Object representation of a message metadata.
 */
public class Metadata {

    /**
     * Unique id of the message.
     */
    private long messageId;

    /**
     * Time the message arrived at the broker.
     */
    private final long arrivalTime;

    /**
     * Key value used by the router (exchange) to identify the relevant queue(s) for this message.
     */
    private final String routingKey;

    /**
     * Exchange this message arrived to.
     */
    private final String exchangeName;

    /**
     * True if the message needs to be persisted, false otherwise.
     */
    private final boolean isPersistent;

    /**
     * Byte length of the content.
     */
    private final int contentLength;

    /**
     * Message reference count.
     */
    private AtomicInteger referenceCount;

    public Metadata(long messageId, long arrivalTime, String routingKey, String exchangeName,
                    boolean isPersistent, int contentLength) {
        this.messageId = messageId;
        this.arrivalTime = arrivalTime;
        this.routingKey = routingKey;
        this.exchangeName = exchangeName;
        this.isPersistent = isPersistent;
        this.contentLength = contentLength;
        this.referenceCount = new AtomicInteger(1);
    }

    public long getMessageId() {
        return messageId;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public int addReference() {
        return referenceCount.incrementAndGet();
    }

    public int removeReference() {
        return referenceCount.decrementAndGet();
    }
}
