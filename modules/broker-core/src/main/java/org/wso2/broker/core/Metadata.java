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

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of a message metadata.
 */
public class Metadata {

    /**
     * Unique id of the message.
     */
    private final long messageId;

    /**
     * Key value used by the router (exchange) to identify the relevant queue(s) for this message.
     */
    private final String routingKey;

    /**
     * Exchange this message arrived to.
     */
    private final String exchangeName;

    /**
     * Byte length of the content.
     */
    private final long contentLength;

    private final List<String> queueList;

    private ByteBuf rawMetadata;

    public Metadata(long messageId, String routingKey, String exchangeName, long contentLength) {
        this.messageId = messageId;
        this.routingKey = routingKey;
        this.exchangeName = exchangeName;
        this.contentLength = contentLength;
        this.queueList = new ArrayList<>();
    }

    public long getMessageId() {
        return messageId;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void addOwnedQueue(String queueName) {
        queueList.add(queueName);
    }

    public List<String> getOwnedQueues() {
        return queueList;
    }
    
    public ByteBuf getRawMetadata() {
        return rawMetadata;
    }

    public void setRawMetadata(ByteBuf rawMetadata) {
        this.rawMetadata = rawMetadata;
    }
}
