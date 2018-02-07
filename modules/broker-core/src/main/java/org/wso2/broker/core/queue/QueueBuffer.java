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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used to track messages for the queue.
 */
public class QueueBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueBuffer.class);

    private AtomicInteger size = new AtomicInteger(0);

    /**
     * Pointer to first node.
     * Invariant: (first == null && last == null) ||
     *            (first.prev == null && first.item != null)
     */
    private Node first;

    /**
     * Pointer to first deliverable node.
     * Invariant: (first == null && last == null) ||
     *            (first.prev == null && first.item != null)
     */
    private Node firstDeliverable;

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
        final Node previousLast = last;
        final Node newNode = new Node(previousLast, newMessage, null);
        last = newNode;
        keyMap.put(newMessage.getMetadata().getInternalId(), newNode);

        if (previousLast == null) {
            first = newNode;
            firstDeliverable = newNode;
        } else {
            previousLast.next = newNode;
        }

        size.incrementAndGet();
    }

    public synchronized void remove(Message message) {
        long messageId = message.getMetadata().getInternalId();
        Node node = keyMap.remove(messageId);
        if (node == null) {
            LOGGER.warn("Asked to remove a non existing message with ID {}", messageId);
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
    }

    public int size() {
        return size.get();
    }

    public synchronized Message getFirstDeliverable() {
        Node deliverableNode = firstDeliverable;

        if (deliverableNode != firstUndeliverable) {
            firstDeliverable = deliverableNode.next;
            return deliverableNode.item;
        } else  {
            return null;
        }
    }

    public void addAll(Collection<Message> messages) {
        for (Message message : messages) {
            add(message);
        }
    }

    private static class Node {
        Message item;
        Node next;
        Node prev;

        Node(Node prev, Message element, Node next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
}
