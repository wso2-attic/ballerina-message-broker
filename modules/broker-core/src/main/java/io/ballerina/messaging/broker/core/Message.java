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

import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents message received from publishers and delivered to subscribers by the broker.
 * This contains the metadata and the content chunks of the message.
 */
public class Message {

    private static final Logger LOGGER = LoggerFactory.getLogger(Message.class);

    private Metadata metadata;

    private final List<ContentChunk> contentChunks;

    private final MessageDataHolder messageDataHolder;

    public Message(long internalId, Metadata metadata) {
        this(internalId, metadata, ConcurrentHashMap.newKeySet(), 0);
    }

    private Message(long internalId, Metadata metadata, Set<String> queueSet, int redeliveryCount) {
        this.metadata = metadata;
        this.contentChunks = new ArrayList<>();
        long expiryTimestamp = extractExpiryTimestamp(metadata);
        messageDataHolder = new MessageDataHolder(internalId, expiryTimestamp, queueSet, redeliveryCount);
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public List<ContentChunk> getContentChunks() {
        return Collections.unmodifiableList(contentChunks);
    }

    public void addChunk(ContentChunk contentChunk) {
        contentChunks.add(contentChunk);
    }

    public void release() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Release message with id: {}", getInternalId(), new Throwable());
        }
        for (ContentChunk contentChunk : contentChunks) {
            contentChunk.release();
        }
    }

    public Message shallowCopy() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Shallow copy message id: {}", getInternalId(), new Throwable());
        }
        Message message = new Message(getInternalId(), metadata, getAttachedDurableQueues(), getRedeliveryCount());
        shallowCopyContent(message);
        return message;
    }

    /**
     * Create a  shallow copy of the message without copying metadata or content. Only the message ID and the
     * attached queue data is copied by reference.
     *
     * @return shallow copy of the message
     */
    public Message bareShallowCopy() {
        return new Message(getInternalId(), null, messageDataHolder.getAttachedQueues(), getRedeliveryCount());
    }

    public Message shallowCopyWith(long newMessageId, String routingKey, String exchangeName) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Shallow copy message with id: {} newId: {}", getInternalId(), newMessageId, new Throwable());
        }
        Message message = new Message(newMessageId, metadata.shallowCopyWith(routingKey, exchangeName));
        shallowCopyContent(message);
        return message;
    }

    private void shallowCopyContent(Message message) {
        contentChunks.stream().map(ContentChunk::shallowCopy).forEach(message::addChunk);
    }

    public void addAttachedDurableQueue(String queueName) {
        messageDataHolder.attachQueue(queueName);
    }

    public boolean hasAttachedDurableQueues() {
        return messageDataHolder.hasAttachedDurableQueues();
    }

    public Set<String> getAttachedDurableQueues() {
        return messageDataHolder.getAttachedQueues();
    }

    public long getInternalId() {
        return messageDataHolder.getInternalId();
    }

    /**
     * Set redelivery flag.
     */
    public int setRedeliver() {
        return messageDataHolder.setRedeliver();
    }

    /**
     * Getter for redeliveryCount.
     */
    public int getRedeliveryCount() {
        return messageDataHolder.getRedeliveryCount();
    }

    /**
     * Check if redelivery flag is set.
     */
    public boolean isRedelivered() {
        return messageDataHolder.isRedelivered();
    }

    public DetachableMessage getDetachableMessage() {
        return messageDataHolder;
    }

    @Override
    public String toString() {

        if (Objects.isNull(metadata)) {
            return "Bare message";
        } else {
            return metadata.toString();
        }
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void clearData() {
        metadata = null;
        release();
        contentChunks.clear();
    }

    /**
     * Check whether message content is not cleared.
     *
     * @return true if message content is available, false otherwise
     */
    public boolean hasContent() {
        return Objects.nonNull(metadata);
    }

    /**
     * Get expire timestamp
     *
     * @return get expire timestamp as a {@link java.lang.Long}
     */
    public long getExpiryTimestamp() {
        return messageDataHolder.getExpiryTimestamp();
    }

    /**
     * Check if message is expired w:r:t current time
     * @return true if expired
     */
    public boolean checkIfExpired() {
        return System.currentTimeMillis() > getExpiryTimestamp();
    }

    private long extractExpiryTimestamp(Metadata metadata) {
        long expiryTimestamp = 0;
        if (metadata != null) {
            FieldValue expiryFieldValue = metadata.getProperties().getValue(Metadata.EXPIRATION);
            if (expiryFieldValue != null) {
                ShortString expiryTime = (ShortString) expiryFieldValue.getValue();
                if (!expiryTime.isEmpty()) {
                    expiryTimestamp = Long.parseLong(expiryTime.toString());
                }
            }
        }
        return expiryTimestamp;
    }

    /**
     * Internal message data holder class. This acts as the detachable message implementation as well.
     */
    private static class MessageDataHolder implements DetachableMessage {

        private final long internalId;

        private final long expiryTimestamp;

        private final Set<String> queueSet;

        private boolean redelivered = false;

        private int redeliveryCount;

        private MessageDataHolder(long internalId, long expiryTimestamp, Set<String> queueSet, int redeliveryCount) {
            this.internalId = internalId;
            this.expiryTimestamp = expiryTimestamp;
            this.queueSet = queueSet;
            this.redeliveryCount = redeliveryCount;
        }

        @Override
        public long getInternalId() {
            return internalId;
        }

        @Override
        public long getExpiryTimestamp() {
            return expiryTimestamp;
        }

        @Override
        public void removeAttachedDurableQueue(String queueName) {
            queueSet.remove(queueName);
        }

        @Override
        public boolean hasAttachedDurableQueues() {
            return !queueSet.isEmpty();
        }

        @Override
        public Set<String> getAttachedQueues() {
            return queueSet;
        }

        void attachQueue(String queueName) {
            queueSet.add(queueName);
        }

        int setRedeliver() {
            redelivered = true;
            return ++redeliveryCount;
        }

        int getRedeliveryCount() {
            return redeliveryCount;
        }

        boolean isRedelivered() {
            return redelivered;
        }
    }
}
