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

package io.ballerina.messaging.broker.amqp.codec;

import io.ballerina.messaging.broker.amqp.AmqpException;
import io.ballerina.messaging.broker.auth.AuthException;
import io.ballerina.messaging.broker.auth.AuthNotFoundException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.ContentChunk;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Metadata;
import io.ballerina.messaging.broker.core.transaction.BrokerTransaction;
import io.ballerina.messaging.broker.core.util.MessageTracer;
import io.ballerina.messaging.broker.core.util.TraceField;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handles incoming AMQP message frames and creates {@link Message}.
 */
public class InMemoryMessageAggregator {

    private static final String CORRELATION_ID_FIELD_NAME = "correlationId";

    private static final String INCOMING_MESSAGE_MAPPED = "Incoming message to AMQP transport.";

    private static final String PUBLISH_MESSAGE = "Publishing message from AMQP transport.";

    private Message message;

    private BrokerTransaction transaction;

    private String routingKey;

    private String exchangeName;

    private long receivedPayloadSize;

    InMemoryMessageAggregator(BrokerTransaction transaction) {
        this.transaction = transaction;
    }

    public void basicPublishReceived(ShortString routingKey, ShortString exchangeName) {
        this.routingKey = routingKey.toString();
        this.exchangeName = exchangeName.toString();
    }

    /**
     * Add the header frame that gives the relevant metadata for the given message.
     *
     * @param headers protocol specific headers
     * @param properties properties of the message
     * @param payloadSize total message content length in bytes
     */
    public void headerFrameReceived(FieldTable headers, FieldTable properties, long payloadSize) {
        long messageId = Broker.getNextMessageId();
        Metadata metadata = new Metadata(routingKey, exchangeName, payloadSize);
        metadata.setProperties(properties);
        metadata.setHeaders(headers);
        message = new Message(messageId, metadata);
        trace(message);
    }

    private void trace(Message message) {
        if (MessageTracer.isTraceEnabled()) {
            List<TraceField> traceFields = new ArrayList<>();
            FieldValue fieldValue = message.getMetadata().getProperty(Metadata.CORRELATION_ID);
            if (Objects.nonNull(fieldValue)) {
                TraceField field = new TraceField(CORRELATION_ID_FIELD_NAME, fieldValue.getValue());
                traceFields.add(field);
            }
            MessageTracer.trace(message, INCOMING_MESSAGE_MAPPED, traceFields);
        }
    }

    private void clear() {
        message = null;
        routingKey = null;
        exchangeName = null;
        receivedPayloadSize = 0;
    }

    public void publish(Message message) throws BrokerException, AuthNotFoundException, AuthException {
        if (MessageTracer.isTraceEnabled()) {
            MessageTracer.trace(message, PUBLISH_MESSAGE);
        }
        transaction.enqueue(message);
    }

    public boolean contentBodyReceived(long length, ByteBuf payload) throws AmqpException {
        ContentChunk contentChunk = new ContentChunk(receivedPayloadSize, payload);
        message.addChunk(contentChunk);
        receivedPayloadSize += length;
        long contentLength = message.getMetadata().getContentLength();

        if (contentLength == receivedPayloadSize) {
            return true;
        } else if (contentLength < receivedPayloadSize) {
            clear();
            message.release();
            throw new AmqpException("Content length mismatch. Received content more than the expected size");
        }

        return false;
    }

    public Message popMessage() {
        Message message = this.message;
        clear();

        return message;
    }

    public void setTransaction(BrokerTransaction transaction) {
        this.transaction = transaction;
    }
}
