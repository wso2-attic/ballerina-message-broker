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

package org.wso2.broker.amqp.codec;

import io.netty.buffer.ByteBuf;
import org.wso2.broker.amqp.AmqpException;
import org.wso2.broker.common.data.types.FieldTable;
import org.wso2.broker.common.data.types.FieldValue;
import org.wso2.broker.common.data.types.ShortString;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.ContentChunk;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.Metadata;
import org.wso2.broker.core.transaction.BrokerTransaction;
import org.wso2.broker.core.util.MessageTracer;
import org.wso2.broker.core.util.TraceField;

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

    private final Broker broker;

    private BrokerTransaction transaction;

    private String routingKey;

    private String exchangeName;

    private long receivedPayloadSize;

    InMemoryMessageAggregator(Broker broker, BrokerTransaction transaction) {
        this.broker = broker;
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
        long messageId = broker.getNextMessageId();
        Metadata metadata = new Metadata(messageId, routingKey, exchangeName, payloadSize);
        metadata.setProperties(properties);
        metadata.setHeaders(headers);
        message = new Message(metadata);
        trace(metadata);
    }

    private void trace(Metadata metadata) {
        if (MessageTracer.isTraceEnabled()) {
            List<TraceField> traceFields = new ArrayList<>();
            FieldValue fieldValue = metadata.getProperty(Metadata.CORRELATION_ID);
            if (Objects.nonNull(fieldValue)) {
                TraceField field = new TraceField(CORRELATION_ID_FIELD_NAME, fieldValue.getValue());
                traceFields.add(field);
            }
            MessageTracer.trace(metadata, INCOMING_MESSAGE_MAPPED, traceFields);
        }
    }

    private void clear() {
        message = null;
        routingKey = null;
        exchangeName = null;
        receivedPayloadSize = 0;
    }

    public void publish(Message message) throws BrokerException {
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
