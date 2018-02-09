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

package org.wso2.broker.core.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.core.Message;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used to track messages for the queue.
 */
public class QueueBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueBuffer.class);
    private final int inMemoryLimit;
    private final MessageReader messageReader;

    private AtomicInteger size = new AtomicInteger(0);
    private AtomicInteger fullMessageCount = new AtomicInteger(0);

    /**
     * Pointer to first node.
     * Invariant: (first == null && last == null) ||
     *            (first.prev == null && first.item != null)
     */
    private Node first;

    /**
     * Pointer to first deliverable candidate node.
     */
    private Node firstDeliverableCandidate;

    /**
     * Pointer to last deliverable node.
     */
    private Node firstUndeliverable;

    /**
     * Pointer to last node.
     * Invariant: (first == null && last == null) ||
     *            (last.next == null && last.item != null)
     */
    private Node last;

    /**
     * Used to fast lookup the node for a message ID
     */
    private Map<Long, Node> keyMap = new HashMap<>();

    public QueueBuffer(int inMemoryLimit, MessageReader messageReader) {
        this.inMemoryLimit = inMemoryLimit;
        this.messageReader = messageReader;
    }

    /**
     * Appends the specified message to the end of this list.
     *
     * @param message message to be appended to this list
     */
    public synchronized void add(Message message) {
        linkLast(message);
    }

    /**
     * Links newMessage as last element.
     */
    private void linkLast(Message newMessage) {
        int queueSize = size.incrementAndGet();

        final Node previousLast = last;
        final Node newNode = new Node(previousLast, newMessage, null);

        if (queueSize > inMemoryLimit) {

            // We can take 'inMemoryLimit + 1' since we are incrementing  size only at one place. If we add multiple
            // places we will have to use queueSize > inMemoryLimit and a boolean to only ser firstUndeliverable once.
            if (Objects.isNull(firstUndeliverable) && queueSize == inMemoryLimit + 1) {
                firstUndeliverable = newNode;
            }

            newMessage.clearData();
        } else {
            newNode.state.set(Node.FULL_MESSAGE);
            fullMessageCount.incrementAndGet();

            if (Objects.isNull(firstDeliverableCandidate)) {
                firstDeliverableCandidate = newNode;
            }
        }

        last = newNode;
        keyMap.put(newMessage.getInternalId(), newNode);

        if (previousLast == null) {
            first = newNode;
        } else {
            previousLast.next = newNode;
        }

    }

    public synchronized void remove(Message message) {
        long messageId = message.getInternalId();
        Node node = keyMap.remove(messageId);
        if (node == null) {
            return;
        }

        unlink(node);
    }


    /**
     * Unlinks non-null node.
     */
    private void unlink(Node node) {
        final Node next = node.next;
        final Node prev = node.prev;

        // if prev is null we are removing the first element
        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        // if next is null we are removing the last element
        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }

        node.item = null;
        size.decrementAndGet();
        fullMessageCount.decrementAndGet();
        submitMessageReads();
    }

    public int size() {
        return size.get();
    }

    public synchronized Message getFirstDeliverable() {

        submitMessageReads();
        Node deliverableCandidate = firstDeliverableCandidate;

        if (deliverableCandidate != firstUndeliverable) {

            if (deliverableCandidate.state.get() != Node.FULL_MESSAGE) {
                return null;
            }


            firstDeliverableCandidate = deliverableCandidate.next;
            Message item = deliverableCandidate.item;
            return item;
        } else  if (firstUndeliverable != null && firstUndeliverable.state.get() == Node.FULL_MESSAGE) {
            Node newDeliverable = firstUndeliverable;
            firstDeliverableCandidate = firstUndeliverable.next;
            pushFirstUndeliverableCursor();

            Message item = newDeliverable.item;

            return item;
        } else {
            return null;
        }
    }

    private void pushFirstUndeliverableCursor() {

        firstUndeliverable = firstUndeliverable.next;

        while (firstUndeliverable != null && firstUndeliverable.state.get() == Node.FULL_MESSAGE) {
            firstUndeliverable = firstUndeliverable.next;
        }
    }

    private void submitMessageReads() {
        int fillableMessageCount = inMemoryLimit - fullMessageCount.get();

        Node undeliverableNode = this.firstUndeliverable;
        while (fillableMessageCount-- > 0 && undeliverableNode != null) {
            if (undeliverableNode.state.compareAndSet(Node.BARE_MESSAGE, Node.SUBMITTED_FOR_FILLING)) {
                Message message = undeliverableNode.item;
                messageReader.fill(this, message);
            } else {
                break;
            }

            undeliverableNode = undeliverableNode.next;
        }
    }

    public void addAll(Collection<Message> messages) {
        for (Message message : messages) {
            add(message);
        }
    }

    public void markMessageFilled(Message message) {
        Node node = keyMap.get(message.getInternalId());
        if (Objects.nonNull(node)) {
           node.state.set(Node.FULL_MESSAGE);
           fullMessageCount.incrementAndGet();
        }
    }

    private static class Node {
        private static final int BARE_MESSAGE = 0;
        private static final int SUBMITTED_FOR_FILLING = 1;
        private static final int FULL_MESSAGE = 2;
        private Message item;
        private Node next;
        private Node prev;
        private AtomicInteger state = new AtomicInteger(BARE_MESSAGE);

        Node(Node prev, Message element, Node next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }


    /**
     * Interface used to fill message date.
     */
    @FunctionalInterface
    public interface MessageReader {

        void fill(QueueBuffer buffer, Message message);
    }
}
