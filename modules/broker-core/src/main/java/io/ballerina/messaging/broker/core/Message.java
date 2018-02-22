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

import java.util.ArrayList;
import java.util.Collection;
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

    private Metadata metadata;

    private final ArrayList<ContentChunk> contentChunks;

    private boolean redelivered = false;

    private int redeliveryCount;

    private final Set<String> queueSet;

    /**
     * Unique id of the message.
     */
    private final long internalId;

    public Message(long internalId, Metadata metadata) {
        this(internalId, metadata, ConcurrentHashMap.newKeySet());
    }

    private Message(long internalId, Metadata metadata, Set<String> queueSet) {
        this.internalId = internalId;
        this.metadata = metadata;
        this.contentChunks = new ArrayList<>();
        this.queueSet = queueSet;
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
        for (ContentChunk contentChunk : contentChunks) {
            contentChunk.release();
        }
    }

    public Message shallowCopy() {
        Message message = new Message(internalId, metadata.shallowCopy(), queueSet);
        message.redelivered = redelivered;
        message.redeliveryCount = redeliveryCount;
        shallowCopyContent(message);
        return message;
    }

    public Message shallowCopyWith(long newMessageId, String routingKey, String exchangeName) {
        Message message = new Message(newMessageId, metadata.shallowCopyWith(routingKey, exchangeName));
        shallowCopyContent(message);
        return message;
    }

    private void shallowCopyContent(Message message) {
        contentChunks.stream().map(ContentChunk::shallowCopy).forEach(message::addChunk);
    }

    public void addOwnedQueue(String queueName) {
        queueSet.add(queueName);
    }

    public boolean hasAttachedQueues() {
        return !queueSet.isEmpty();
    }

    public void removeAttachedQueue(String queueName) {
        queueSet.remove(queueName);
    }

    public Collection<String> getAttachedQueues() {
        return queueSet;
    }

    public long getInternalId() {
        return internalId;
    }

    /**
     * Set redelivery flag.
     */
    public int setRedeliver() {
        redelivered = true;
        return ++redeliveryCount;
    }

    /**
     * Getter for redeliveryCount.
     */
    public int getRedeliveryCount() {
        return redeliveryCount;
    }

    /**
     * Check if redelivery flag is set.
     */
    public boolean isRedelivered() {
        return redelivered;
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
}
