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

package io.ballerina.messaging.broker.core.queue;

import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Used to track messages for the queue.
 */
public class QueueBuffer {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueBuffer.class);

    /**
     * Maximum number of messages held in memory.
     */
    private final int inMemoryLimit;

    /**
     * Maximum number of indelible messages kept in the queue buffer.
     */
    private final int indelibleMessageLimit;
    /**
     * Used to submit read requests for a message.
     */
    private final MessageReader messageReader;

    /**
     * Size of the queue. i.e. in memory messages + DB messages.
     */
    private AtomicInteger size = new AtomicInteger(0);

    /**
     * Number of messages that are retrieved for delivery but not yet removed.
     */
    private AtomicInteger messagesInFlight = new AtomicInteger(0);

    /**
     * Number of in memory messages.
     */
    private AtomicInteger deliverableMessageCount = new AtomicInteger(0);

    /**
     * Total Number of undelivered messages in the buffer.
     */
    private AtomicInteger undeliveredMessageCount = new AtomicInteger(0);

    /**
     * Total Number of undelivered messages in the buffer.
     */
    private AtomicInteger indelibleMessageCount = new AtomicInteger(0);

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
     */
    private Node last;

    /**
     * Used to fast lookup the node for a message ID.
     */
    private Map<Long, Node> keyMap = new ConcurrentHashMap<>();

    QueueBuffer(int inMemoryLimit, int indelibleMessageLimit, MessageReader messageReader) {
        this.inMemoryLimit = inMemoryLimit;
        this.indelibleMessageLimit = indelibleMessageLimit;
        this.messageReader = messageReader;
    }

    /**
     * Appends the specified message to the end of this list.
     *
     * @param message message to be appended to this list
     */
    public synchronized void add(Message message) {
        linkLast(message);
        postProcessDeliverableNode();
    }

    /**
     * Add messages as bare messages to the queue buffer. This means that broker has to fetch message data for each
     * and every message in this list.
     *
     * @param messages list of messages
     */
    public synchronized void addAllBareMessages(Collection<Message> messages) {
        for (Message message : messages) {
            addBareMessage(message);
        }
    }

    /**
     * Add message as a bare messages to the queue buffer. This means that broker has to fetch message data for this
     * message before giving it out.
     *
     * @param message bare message
     */
    public synchronized void addBareMessage(Message message) {
        linkLast(message);
        postProcessBareMessage();
    }

    /**
     * Add a message whose content is never deleted irrespective of the queue size.
     *
     * @param message message
     */
    public synchronized boolean addIndelibleMessage(Message message) {
        int newIndelibleMessageCount = indelibleMessageCount.get() + 1;
        if (newIndelibleMessageCount > indelibleMessageLimit) {
            return false;
        }

        linkLast(message);
        postProcessIndelibleMessage();
        return true;
    }

    /**
     * Links newMessage as last element.
     */
    private void linkLast(Message newMessage) {
        size.incrementAndGet();
        undeliveredMessageCount.incrementAndGet();

        final Node previousLast = last;
        final Node newNode = new Node(previousLast, newMessage, null);

        last = newNode;
        keyMap.put(newMessage.getInternalId(), newNode);

        if (Objects.nonNull(previousLast)) {
            previousLast.next = newNode;
        }
    }

    /**
     * Post process the added deliverable message looking at the queue size and the in memory limit. Message data
     * will be cleared if we have deliverable messages than in-memory limit.
     */
    private void postProcessDeliverableNode() {
        Node newNode = last;
        if ((size.get() - indelibleMessageCount.get()) > inMemoryLimit) {

            if (Objects.isNull(firstUndeliverable)) {
                firstUndeliverable = newNode;
            }

            newNode.item.clearData();
        } else {
            newNode.state.set(Node.FULL_MESSAGE);
            deliverableMessageCount.incrementAndGet();

            if (Objects.isNull(firstDeliverableCandidate)) {
                firstDeliverableCandidate = newNode;
            }
        }
    }

    /**
     * Post process after adding a bare message.
     */
    private void postProcessBareMessage() {
        Node newNode = last;
        if (Objects.isNull(firstUndeliverable)) {
            firstUndeliverable = newNode;
        }
        if (Objects.isNull(firstDeliverableCandidate)) {
            firstDeliverableCandidate = newNode;
        }
    }

    private void postProcessIndelibleMessage() {
        Node newNode = last;
        newNode.state.set(Node.INDELIBLE_MESSAGE);
        indelibleMessageCount.incrementAndGet();

        if (Objects.isNull(firstUndeliverable)) {
            firstUndeliverable = newNode;
        }

        if (Objects.isNull(firstDeliverableCandidate)) {
            firstDeliverableCandidate = newNode;
        }
    }

    /**
     * Remove a message from the buffer.
     *
     * @param messageId internal id of the message to be removed.
     */
    public synchronized void remove(long messageId) {
        Node node = keyMap.remove(messageId);
        if (Objects.nonNull(node)) {
            unlink(node);
        }
    }

    public synchronized void removeAll(Collection<DetachableMessage> messages) {
        for (DetachableMessage message : messages) {
            remove(message.getInternalId());
        }
    }

    /**
     * Unlinks a non-null node.
     */
    private void unlink(Node node) {
        final Node next = node.next;
        final Node prev = node.prev;

        // if prev is null we are removing the first element
        if (Objects.nonNull(prev)) {
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

        // We need to move all cursors pointed to the deleting node
        if (node == firstDeliverableCandidate) {
            firstDeliverableCandidate = next;
        }
        if (node == firstUndeliverable) {
            firstUndeliverable = next;
        }  

        node.item = null;
        size.decrementAndGet();
        if (node.state.get() != Node.INDELIBLE_MESSAGE) {
            deliverableMessageCount.decrementAndGet();
        } else {
            indelibleMessageCount.decrementAndGet();
        }

        messagesInFlight.decrementAndGet();
        submitMessageReads();
    }

    /**
     * Size of the queue.
     *
     * @return total number of messages tracked in queue buffer
     */
    public int size() {
        return size.get();
    }

    /**
     * Total number of messages given out for delivery but has not removed from the buffer yet.
     *
     * @return number of messages in flight
     */
    public int getNumberOfInflightMessages() {
        return messagesInFlight.get();
    }

    /**
     * Total number of messages that are yet to be retrieved for delivery.
     *
     * @return number of undelivered messages
     */
    public int getNumberOfUndeliveredMessages() {
        return undeliveredMessageCount.get();
    }

    /**
     * Return the first deliverable message if one is available.
     *
     * @return the next deliverable message in queue
     */
    public synchronized Message getFirstDeliverable() {

        submitMessageReads();
        Node deliverableCandidate = firstDeliverableCandidate;

        if (deliverableCandidate != firstUndeliverable) {

            if (!deliverableCandidate.hasContent()) {
                return null;
            }

            firstDeliverableCandidate = deliverableCandidate.next;

            recordRemovingMessageForDelivery();
            return deliverableCandidate.item;
        } else if (firstUndeliverable != null && firstUndeliverable.hasContent()) {
            Node newDeliverable = firstUndeliverable;
            firstDeliverableCandidate = firstUndeliverable.next;
            pushFirstUndeliverableCursor();

            recordRemovingMessageForDelivery();
            return newDeliverable.item;
        } else {
            return null;
        }
    }

    /**
     * Update corresponding counts when message is removed from the queue for delivery.
     */
    private void recordRemovingMessageForDelivery() {
        messagesInFlight.incrementAndGet();
        undeliveredMessageCount.decrementAndGet();
    }

    private void pushFirstUndeliverableCursor() {
        firstUndeliverable = firstUndeliverable.next;

        while (firstUndeliverable != null && firstUndeliverable.hasContent()) {
            firstUndeliverable = firstUndeliverable.next;
        }
    }

    private void submitMessageReads() {
        int fillableMessageCount = inMemoryLimit - deliverableMessageCount.get();

        Node undeliverableNode = this.firstUndeliverable;
        while (fillableMessageCount > 0 && undeliverableNode != null) {
            if (undeliverableNode.state.compareAndSet(Node.BARE_MESSAGE, Node.SUBMITTED_FOR_FILLING)) {
                Message message = undeliverableNode.item;
                messageReader.fill(this, message);
                fillableMessageCount--;
            } else {
                break;
            }

            undeliverableNode = undeliverableNode.next;
        }
    }

    public void markMessageFilled(Message message) {
        long messageId = message.getInternalId();
        Node node = keyMap.get(messageId);
        if (Objects.nonNull(node)) {
            node.state.set(Node.FULL_MESSAGE);
            deliverableMessageCount.incrementAndGet();
        } else {
            LOGGER.warn("Could not find message {} for marking content filling", messageId);
        }
    }

    public void markMessageFillFailed(Message message) {
        long messageId = message.getInternalId();
        Node node = keyMap.get(messageId);
        if (Objects.nonNull(node)) {
            node.state.set(Node.BARE_MESSAGE);
        } else {
            LOGGER.warn("Could not find message {} for marking content filling failure", messageId);
        }
    }

    synchronized void addAll(List<Message> messages) {
        for (Message message: messages) {
            add(message);
        }
    }

    public synchronized void peekExpiredMessages(Set<Message> messages, int capacity) {
        int messageCounter = 0;

        Node deliverableCandidate = firstDeliverableCandidate;

        if (deliverableCandidate != firstUndeliverable) {
            if (!deliverableCandidate.hasContent()) {
                return;
            }
        } else if (firstUndeliverable != null && firstUndeliverable.hasContent()) {
            deliverableCandidate = firstUndeliverable;
        } else {
            return;
        }
        if (deliverableCandidate.item.checkIfExpired()) {
            messages.add(deliverableCandidate.item);
            messageCounter = messageCounter + 1;
        }
        while (messageCounter <= capacity) {
            deliverableCandidate = deliverableCandidate.next;
            if (deliverableCandidate == null) {
                break;
            }
            if (deliverableCandidate.item.checkIfExpired()) {
                messages.add(deliverableCandidate.item);
                messageCounter = messageCounter + 1;
            }
        }
    }

    /**
     * Remove all messages in the buffer.
     *
     * @return number of messages removed
     */
    public synchronized int clear(Consumer<Message> postDeleteAction) {
        Collection<Node> values = new ArrayList<>(keyMap.values());
        int bufferSize = values.size();
        for (Node node : values) {
            Message message = node.item;
            message.clearData();
            unlink(node);
            postDeleteAction.accept(message);
        }
        return bufferSize;
    }

    private static class Node {
        private static final int BARE_MESSAGE = 0;
        private static final int SUBMITTED_FOR_FILLING = 1;
        private static final int FULL_MESSAGE = 2;
        private static final int INDELIBLE_MESSAGE = 3;
        private Message item;
        private Node next;
        private Node prev;
        private AtomicInteger state = new AtomicInteger(BARE_MESSAGE);

        Node(Node prev, Message element, Node next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }

        boolean hasContent() {
            int stateValue = state.get();
            return stateValue == FULL_MESSAGE || stateValue == INDELIBLE_MESSAGE;
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
