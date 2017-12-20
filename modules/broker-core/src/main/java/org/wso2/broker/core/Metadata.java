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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.common.data.types.FieldTable;
import org.wso2.broker.common.data.types.FieldValue;
import org.wso2.broker.common.data.types.ShortString;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Object representation of a message metadata.
 */
public class Metadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(Metadata.class);

    private static final short BYTE_DEFAULT = -1;

    private static final long LONG_DEFAULT = -1L;

    /**
     * Unique id of the message.
     */
    private final long internalId;

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

    private FieldTable headers;

    private short deliveryMode = BYTE_DEFAULT;

    private short priority = BYTE_DEFAULT;

    private ShortString correlationId;

    private ShortString replyTo;

    private ShortString expiration;

    private ShortString messageId;

    private long timestamp = LONG_DEFAULT;

    private ShortString type;

    private ShortString userId;

    private ShortString appId;

    private ByteBuf rawMetadata;

    private ShortString contentType;

    private ShortString contentEncoding;

    private BiFunction<ByteBuf, Metadata, Boolean> headerParser;

    private boolean headersPassed;

    public Metadata(long internalId, String routingKey, String exchangeName, long contentLength) {
        this.internalId = internalId;
        this.routingKey = routingKey;
        this.exchangeName = exchangeName;
        this.contentLength = contentLength;
        this.queueList = new ArrayList<>();
        this.headers = null;
        headersPassed = false;
        headerParser = null;
    }

    public long getInternalId() {
        return internalId;
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

    public FieldValue getHeader(ShortString propertyName) {
        parseHeaders();
        return headers.getValue(propertyName);
    }

    public ByteBuf getRawMetadata() {
        return rawMetadata;
    }

    public void setRawMetadata(ByteBuf rawMetadata, BiFunction<ByteBuf, Metadata, Boolean> headerParser) {
        this.rawMetadata = rawMetadata;
        this.headerParser = headerParser;
    }

    public short getDeliveryMode() {
        parseHeaders();
        return deliveryMode;
    }

    public short getPriority() {
        parseHeaders();
        return priority;
    }

    public ShortString getCorrelationId() {
        parseHeaders();
        return correlationId;
    }

    public ShortString getReplyTo() {
        parseHeaders();
        return replyTo;
    }

    public ShortString getExpiration() {
        parseHeaders();
        return expiration;
    }

    public ShortString getMessageId() {
        parseHeaders();
        return messageId;
    }

    public long getTimestamp() {
        parseHeaders();
        return timestamp;
    }

    public ShortString getType() {
        parseHeaders();
        return type;
    }

    public ShortString getUserId() {
        parseHeaders();
        return userId;
    }

    public ShortString getAppId() {
        parseHeaders();
        return appId;
    }

    public ShortString getContentType() {
        parseHeaders();
        return contentType;
    }

    public ShortString getContentEncoding() {
        parseHeaders();
        return contentEncoding;
    }

    public void setContentEncoding(ShortString contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public void setContentType(ShortString contentType) {
        this.contentType = contentType;
    }

    public void setHeaders(FieldTable headers) {
        this.headers = headers;
    }

    public void setDeliveryMode(short deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }

    public void setCorrelationId(ShortString correlationId) {
        this.correlationId = correlationId;
    }

    public void setReplyTo(ShortString replyTo) {
        this.replyTo = replyTo;
    }

    public void setExpiration(ShortString expiration) {
        this.expiration = expiration;
    }

    public void setMessageId(ShortString messageId) {
        this.messageId = messageId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(ShortString type) {
        this.type = type;
    }

    public void setUserId(ShortString userId) {
        this.userId = userId;
    }

    public void setAppId(ShortString appId) {
        this.appId = appId;
    }

    private void parseHeaders() {
        if (headerParser != null && !headersPassed) {
            headersPassed = headerParser.apply(rawMetadata, this);
        }
    }

    public Metadata shallowCopy() {
        Metadata metadata = new Metadata(internalId, routingKey, exchangeName, contentLength);
        if (rawMetadata != null) {
            metadata.rawMetadata = rawMetadata.retainedSlice();
            metadata.headerParser = headerParser;
        }

        metadata.queueList.addAll(queueList);
        metadata.headers = headers;
        metadata.deliveryMode = deliveryMode;
        metadata.priority = priority;
        metadata.correlationId = correlationId;
        metadata.replyTo = replyTo;
        metadata.expiration = expiration;
        metadata.messageId = messageId;
        metadata.timestamp = timestamp;
        metadata.type = type;
        metadata.userId = userId;
        metadata.appId = appId;
        metadata.contentType = contentType;
        metadata.contentEncoding = contentEncoding;
        metadata.headersPassed = headersPassed;
        return metadata;
    }

    public void release() {
        if (rawMetadata != null) {
            rawMetadata.release();
        }
    }
}
