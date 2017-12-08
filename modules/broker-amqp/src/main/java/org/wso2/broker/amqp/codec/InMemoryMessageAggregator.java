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
import org.wso2.broker.amqp.codec.data.ShortString;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.ContentChunk;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.Metadata;

/**
 * Handles incoming AMQP message frames and creates {@link Message}.
 */
public class InMemoryMessageAggregator {

    private Message message;

    private final Broker broker;

    private String routingKey;

    private String exchangeName;

    private long receivedPayloadSize;

    public InMemoryMessageAggregator(Broker broker) {
        this.broker = broker;
    }

    public void basicPublishReceived(ShortString routingKey, ShortString exchangeName) {
        this.routingKey = routingKey.toString();
        this.exchangeName = exchangeName.toString();
    }

    /**
     * Add the header frame that gives the relevant metadata for the given message.
     *
     * @param rawMetadata unprocessed raw metadata {@link ByteBuf}
     * @param payloadSize total message content length in bytes
     */
    public void headerFrameReceived(ByteBuf rawMetadata, long payloadSize) {
        long messageId = broker.getNextMessageId();
        Metadata metadata = new Metadata(messageId, routingKey, exchangeName, payloadSize);
        metadata.setRawMetadata(rawMetadata);
        message = new Message(metadata);
    }

    private void clear() {
        message = null;
        routingKey = null;
        exchangeName = null;
        receivedPayloadSize = 0;
    }

    public void publish(Message message) throws BrokerException {
        broker.publish(message);
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
}
