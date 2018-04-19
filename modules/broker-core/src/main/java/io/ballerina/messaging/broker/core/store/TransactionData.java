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

import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Messages to be stored, messages to be deleted and detached from a queue within a single
 * database transaction are stored in this class.
 */
@NotThreadSafe
public class TransactionData {

    private final Map<Long, Message> enqueueMessages;

    private final Map<String, QueueDetachEventList> detachMessageMap;

    private final Map<String, List<DetachableMessage>> preparedDetachEventMap;

    private final List<Long> deleteMessageIdList;

    private int detachOperationsCount;

    private int preparedDetachEventCount;

    TransactionData() {
        enqueueMessages = new HashMap<>();
        detachMessageMap = new HashMap<>();
        deleteMessageIdList = new ArrayList<>();
        preparedDetachEventMap = new HashMap<>();
        detachOperationsCount = 0;
    }

    public void addEnqueueMessage(Message message) {
        enqueueMessages.put(message.getInternalId(), message);
    }


    public void attach(String queueName, long messageInternalId) {
        Message message = enqueueMessages.get(messageInternalId);
        message.addAttachedDurableQueue(queueName);
    }

    public void detach(String queueName, long internalMessageId) {
        QueueDetachEventList detachList = detachMessageMap.computeIfAbsent(queueName, k -> new QueueDetachEventList());
        detachList.add(internalMessageId);
        detachOperationsCount++;
    }

    public void prepareForDetach(String queueName, DetachableMessage message) {
        List<DetachableMessage> preparedDetachList =
                preparedDetachEventMap.computeIfAbsent(queueName, k -> new ArrayList<>());
        preparedDetachList.add(message);
        preparedDetachEventCount++;
    }

    public void addDeletableMessage(long internalMessageId) {
        deleteMessageIdList.add(internalMessageId);
    }

    public Collection<Message> getEnqueueMessages() {
        return enqueueMessages.values();
    }

    public Map<String, QueueDetachEventList> getDetachMessageMap() {
        return detachMessageMap;
    }

    public Map<String, List<DetachableMessage>> getPreparedDetachEventMap() {
        return preparedDetachEventMap;
    }

    public Collection<Long> getDeletableMessage() {
        return deleteMessageIdList;
    }

    public void clear() {
        enqueueMessages.clear();
        deleteMessageIdList.clear();
        detachMessageMap.clear();
        preparedDetachEventMap.clear();
        detachOperationsCount = 0;
        preparedDetachEventCount = 0;
    }

    public void releaseEnqueueMessages() {
        for (Message message: enqueueMessages.values()) {
            message.release();
        }
        enqueueMessages.clear();
    }

    public boolean isEmpty() {
        return deleteMessageIdList.isEmpty() && enqueueMessages.isEmpty() &&
                detachMessageMap.isEmpty() && preparedDetachEventMap.isEmpty();
    }

    public int size() {
        return detachOperationsCount + preparedDetachEventCount + enqueueMessages.size() + deleteMessageIdList.size();
    }

}
