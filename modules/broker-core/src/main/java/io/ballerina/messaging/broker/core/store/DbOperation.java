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

package io.ballerina.messaging.broker.core.store;

import com.lmax.disruptor.EventFactory;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.queue.QueueBuffer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Disruptor related place holder class.
 */
public class DbOperation {

    private static final Factory factory = new Factory();

    /**
     * This field will be not null when an exception is thrown from a handler.
     */
    private Throwable exceptionObject;

    /**
     * Database operation types related to messages.
     */
    public enum DbOpType {
        INSERT_MESSAGE,
        DETACH_MSG_FROM_QUEUE,
        DELETE_MESSAGE,
        READ_MSG_DATA,
        NO_OP;
    }
    /**
     * Denotes the current state in terms of processing the event.
     */
    private final AtomicInteger state;

    /**
     * Denotes a fresh event.
     */
    private static final int AVAILABLE = 0;

    /**
     * {@link DbOperation} can be taken from AVAILABLE state or already PROCESSED state to PROCESSING state.
     * Once in processing state no other event handler.
     */
    private static final int PROCESSING = 1;

    private static final int PROCESSED = 2;

    /**
     * Event is processed by either {@link DbEventMatcher} or {@link DbAccessHandler}.
     */
    private static final int PERSIST = 3;

    private DbOpType type;

    private String queueName;

    private long messageId;

    private Message message;

    private QueueBuffer queueBuffer;

    private Message bareMessage;

    private DbOperation() {
        type = DbOpType.NO_OP;
        this.state = new AtomicInteger(AVAILABLE);
    }

    public void insertMessage(Message message) {
        type = DbOpType.INSERT_MESSAGE;
        this.message = message;
        this.messageId = message.getInternalId();
    }

    public boolean acquireToProcess() {
        return state.compareAndSet(PROCESSED, PROCESSING);
    }

    public void completeProcessing() {
        state.set(PROCESSED);
    }

    public boolean acquireForPersisting() {
        return state.compareAndSet(PROCESSED, PERSIST);
    }

    public void deleteMessage(long messageId) {
        type = DbOpType.DELETE_MESSAGE;
        this.messageId = messageId;
    }

    public void detachFromQueue(String queueName, Long messageId) {
        type = DbOpType.DETACH_MSG_FROM_QUEUE;
        this.messageId = messageId;
        this.queueName = queueName;
    }

    public void readMessageData(QueueBuffer queueBuffer, Message message) {
        type = DbOpType.READ_MSG_DATA;
        this.bareMessage = message;
        this.queueBuffer = queueBuffer;
    }

    public void setExceptionObject(Throwable throwable) {
        exceptionObject = throwable;
    }

    public Throwable getExceptionObject() {
        return exceptionObject;
    }

    /**
     * Getter for bareMessage.
     */
    public Message getBareMessage() {
        return bareMessage;
    }

    /**
     * Getter for queueBuffer.
     */
    public QueueBuffer getQueueBuffer() {
        return queueBuffer;
    }

    public DbOpType getType() {
        return type;
    }

    public String getQueueName() {
        return queueName;
    }

    public long getMessageId() {
        return messageId;
    }

    public Message getMessage() {
        return message;
    }

    public void clear() {
        if (Objects.nonNull(message)) {
            message.release();
            message = null;
        }
        bareMessage = null;
        queueBuffer = null;
        messageId = -1;
        queueName = null;
        exceptionObject = null;
        type = DbOpType.NO_OP;
        state.set(AVAILABLE);
    }

    /**
     * Factory class to create {@link DbOperation} objects.
     */
    public static final class Factory implements EventFactory<DbOperation> {

        @Override
        public DbOperation newInstance() {
            return new DbOperation();
        }
    }

    public static Factory getFactory() {
        return factory;
    }
}
