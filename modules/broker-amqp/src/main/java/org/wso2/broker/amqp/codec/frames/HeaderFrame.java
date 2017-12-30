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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqpChannel;
import org.wso2.broker.amqp.codec.InMemoryMessageAggregator;
import org.wso2.broker.amqp.codec.handlers.AmqpConnectionHandler;
import org.wso2.broker.common.data.types.EncodableData;
import org.wso2.broker.common.data.types.FieldTable;
import org.wso2.broker.common.data.types.ShortString;
import org.wso2.broker.core.Metadata;

/**
 * AMQP header frame
 */
public class HeaderFrame extends GeneralFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeaderFrame.class);

    private static final short BYTE_DEFAULT = -1;
    private static final long LONG_DEFAULT = -1L;

    private static final int CONTENT_TYPE_MASK = 1 << 15;
    private static final int ENCODING_MASK = 1 << 14;
    private static final int HEADERS_MASK = 1 << 13;
    private static final int DELIVERY_MODE_MASK = 1 << 12;
    private static final int PRIORITY_MASK = 1 << 11;
    private static final int CORRELATION_ID_MASK = 1 << 10;
    private static final int REPLY_TO_MASK = 1 << 9;
    private static final int EXPIRATION_MASK = 1 << 8;
    private static final int MESSAGE_ID_MASK = 1 << 7;
    private static final int TIMESTAMP_MASK = 1 << 6;
    private static final int TYPE_MASK = 1 << 5;
    private static final int USER_ID_MASK = 1 << 4;
    private static final int APPLICATION_ID_MASK = 1 << 3;
    private static final int LAST_BIT_MASK = 1;

    private final long bodySize;
    private final int classId;

    /*
     * Header properties
     */

    private ShortString contentType;
    private ShortString contentEncoding;
    private FieldTable headers;
    private short deliveryMode = BYTE_DEFAULT;
    private short priority = BYTE_DEFAULT;
    private ShortString correlationId;
    private ShortString replyTo;
    private ShortString expiration;
    private ShortString messageId;
    private long timestamp = LONG_DEFAULT;
    private ShortString type;
    private ShortString userId;
    private ShortString appId;
    private int propertyFlags = 0;
    private ByteBuf rawMetadata;

    public HeaderFrame(int channel, int classId, long bodySize) {
        super((byte) 2, channel);
        this.classId = classId;
        this.bodySize = bodySize;
        this.rawMetadata = null;
    }

    @Override
    public long getPayloadSize() {
        if (rawMetadata != null) {
            return rawMetadata.capacity();
        } else {
            long propertyListSize = 0;

            propertyListSize += getPropertySize(contentType);
            propertyListSize += getPropertySize(contentEncoding);
            propertyListSize += getPropertySize(headers);
            propertyListSize += getPropertySize(deliveryMode);
            propertyListSize += getPropertySize(priority);
            propertyListSize += getPropertySize(correlationId);
            propertyListSize += getPropertySize(replyTo);
            propertyListSize += getPropertySize(expiration);
            propertyListSize += getPropertySize(messageId);
            propertyListSize += getPropertySize(timestamp);
            propertyListSize += getPropertySize(type);
            propertyListSize += getPropertySize(userId);
            propertyListSize += getPropertySize(appId);

            return 2L     // classID
                    + 2L // weight
                    + 8L // body size
                    + 2L // property flag
                    + propertyListSize;
        }
    }

    private long getPropertySize(long property) {
        if (property != LONG_DEFAULT) {
            return 8L;
        } else {
            return 0L;
        }
    }

    private long getPropertySize(short property) {
        if (property != BYTE_DEFAULT) {
            return 1L;
        } else {
            return 0L;
        }
    }

    private long getPropertySize(EncodableData property) {
        if (property != null) {
            return property.getSize();
        } else {
            return 0L;
        }
    }

    @Override
    public void writePayload(ByteBuf buf) {
        if (rawMetadata == null) {
            buf.writeShort(classId);
            buf.writeShort(0); // Write 0 for weight
            buf.writeLong(bodySize);
            buf.writeShort(propertyFlags);
            writeProperty(buf, contentType);
            writeProperty(buf, contentEncoding);
            writeProperty(buf, headers);
            writeProperty(buf, deliveryMode);
            writeProperty(buf, priority);
            writeProperty(buf, correlationId);
            writeProperty(buf, replyTo);
            writeProperty(buf, expiration);
            writeProperty(buf, messageId);
            writeProperty(buf, timestamp);
            writeProperty(buf, type);
            writeProperty(buf, userId);
            writeProperty(buf, appId);
        } else {
            try {
                buf.writeBytes(rawMetadata);
            } finally {
                rawMetadata.release();
            }
        }
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        AmqpChannel channel = connectionHandler.getChannel(getChannel());
        InMemoryMessageAggregator inMemoryMessageAggregator = channel.getMessageAggregator();
        inMemoryMessageAggregator.headerFrameReceived(rawMetadata, bodySize, (byteBuf, metadata) -> {
            try {
                parse(byteBuf, metadata);
                return true;
            } catch (Exception e) {
                LOGGER.error("Error occurred while parsing metadata headers", e);
            }
            return false;
        });
    }

    private void writeProperty(ByteBuf buf, long property) {
        if (property != LONG_DEFAULT) {
            buf.writeLong(property);
        }
    }

    private void writeProperty(ByteBuf buf, EncodableData property) {
        if (property != null) {
            property.write(buf);
        }
    }

    private void writeProperty(ByteBuf buf, int property) {
        if (property != BYTE_DEFAULT) {
            buf.writeByte(property);
        }
    }

    public Metadata parse(ByteBuf buf, Metadata metadata) throws Exception {
        buf.markReaderIndex();
        // skip class id (2), weight (2) and body size (8) bytes
        buf.skipBytes(12);

        int propertyFlags = buf.readUnsignedShort();

        // Skip other property flags if exists
        boolean hasMoreProperties = (propertyFlags & LAST_BIT_MASK) != 0;
        while (hasMoreProperties) {
            int otherPropertyFlags = buf.readUnsignedShort();
            hasMoreProperties = (otherPropertyFlags & LAST_BIT_MASK) != 0;
        }

        // read known properties
        if ((propertyFlags & CONTENT_TYPE_MASK) != 0) {
            metadata.setContentType(ShortString.parse(buf));
        }
        if ((propertyFlags & ENCODING_MASK) != 0) {
            metadata.setContentEncoding(ShortString.parse(buf));
        }
        if ((propertyFlags & HEADERS_MASK) != 0) {
            metadata.setHeaders(FieldTable.parse(buf));
        }
        if ((propertyFlags & DELIVERY_MODE_MASK) != 0) {
            metadata.setDeliveryMode(buf.readUnsignedByte());
        }
        if ((propertyFlags & PRIORITY_MASK) != 0) {
            metadata.setPriority(buf.readUnsignedByte());
        }
        if ((propertyFlags & CORRELATION_ID_MASK) != 0) {
            metadata.setCorrelationId(ShortString.parse(buf));
        }
        if ((propertyFlags & REPLY_TO_MASK) != 0) {
            metadata.setReplyTo(ShortString.parse(buf));
        }
        if ((propertyFlags & EXPIRATION_MASK) != 0) {
            metadata.setExpiration(ShortString.parse(buf));
        }
        if ((propertyFlags & MESSAGE_ID_MASK) != 0) {
            metadata.setMessageId(ShortString.parse(buf));
        }
        if ((propertyFlags & TIMESTAMP_MASK) != 0) {
            metadata.setTimestamp(buf.readLong());
        }
        if ((propertyFlags & TYPE_MASK) != 0) {
            metadata.setType(ShortString.parse(buf));
        }
        if ((propertyFlags & USER_ID_MASK) != 0) {
            metadata.setUserId(ShortString.parse(buf));
        }
        if ((propertyFlags & APPLICATION_ID_MASK) != 0) {
            metadata.setAppId(ShortString.parse(buf));
        }
        buf.resetReaderIndex();
        return metadata;
    }

    public static HeaderFrame lazyParse(ByteBuf buf, int channelId, long payloadSize) {
        buf.markReaderIndex();
        int classId = buf.readUnsignedShort();
        // ignore weight
        buf.skipBytes(2);
        long bodySize = buf.readLong();
        HeaderFrame headerFrame = new HeaderFrame(channelId, classId, bodySize);
        buf.resetReaderIndex();
        ByteBuf metadata = buf.retainedSlice(buf.readerIndex(), (int) payloadSize);
        buf.skipBytes((int) payloadSize);
        headerFrame.setRawMetadata(metadata);
        return headerFrame;
    }

    public void setContentType(ShortString contentType) {
        propertyFlags |= CONTENT_TYPE_MASK;
        this.contentType = contentType;
    }

    public void setContentEncoding(ShortString contentEncoding) {
        propertyFlags |= ENCODING_MASK;
        this.contentEncoding = contentEncoding;
    }

    public void setHeaders(FieldTable headers) {
        propertyFlags |= HEADERS_MASK;
        this.headers = headers;
    }

    public void setDeliveryMode(short deliveryMode) {
        propertyFlags |= DELIVERY_MODE_MASK;
        this.deliveryMode = deliveryMode;
    }

    public void setPriority(short priority) {
        propertyFlags |= PRIORITY_MASK;
        this.priority = priority;
    }

    public void setCorrelationId(ShortString correlationId) {
        propertyFlags |= CORRELATION_ID_MASK;
        this.correlationId = correlationId;
    }

    public void setReplyTo(ShortString replyTo) {
        propertyFlags |= REPLY_TO_MASK;
        this.replyTo = replyTo;
    }

    public void setExpiration(ShortString expiration) {
        propertyFlags |= EXPIRATION_MASK;
        this.expiration = expiration;
    }

    public void setMessageId(ShortString messageId) {
        propertyFlags |= MESSAGE_ID_MASK;
        this.messageId = messageId;
    }

    public void setTimestamp(long timestamp) {
        propertyFlags |= TIMESTAMP_MASK;
        this.timestamp = timestamp;
    }

    public void setType(ShortString type) {
        propertyFlags |= TYPE_MASK;
        this.type = type;
    }

    public void setUserId(ShortString userId) {
        propertyFlags |= USER_ID_MASK;
        this.userId = userId;
    }

    public void setAppId(ShortString appId) {
        propertyFlags |= APPLICATION_ID_MASK;
        this.appId = appId;
    }

    public void setRawMetadata(ByteBuf rawMetadata) {
        this.rawMetadata = rawMetadata;
    }
}
