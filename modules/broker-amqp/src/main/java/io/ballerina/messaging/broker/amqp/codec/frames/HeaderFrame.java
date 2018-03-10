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

package io.ballerina.messaging.broker.amqp.codec.frames;

import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.amqp.codec.InMemoryMessageAggregator;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.data.types.EncodableData;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.common.data.types.LongInt;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Metadata;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;

/**
 * AMQP header frame.
 */
public class HeaderFrame extends GeneralFrame {

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

    private static final ShortString REPLY_TO = ShortString.parseString("replyTo");
    private static final ShortString TIMESTAMP = ShortString.parseString("timestamp");
    private static final ShortString TYPE = ShortString.parseString("type");
    private static final ShortString USER_ID = ShortString.parseString("userId");
    private static final ShortString APPLICATION_ID = ShortString.parseString("applicationId");
    private static final ShortString PROPERTY_FLAGS = ShortString.parseString("propertyFlags");

    private final long bodySize;
    private final int classId;

    // Header properties
    private FieldTable headers;
    private FieldTable properties;

    public HeaderFrame(int channel, int classId, long bodySize) {
        super((byte) 2, channel);
        this.classId = classId;
        this.bodySize = bodySize;
        properties = new FieldTable(new HashMap<>());
        headers = FieldTable.EMPTY_TABLE;
    }

    @Override
    public long getPayloadSize() {
        long propertyListSize = 0;

        propertyListSize += getPropertySize(properties.getValue(Metadata.CONTENT_TYPE));
        propertyListSize += getPropertySize(properties.getValue(Metadata.CONTENT_ENCODING));
        propertyListSize += getPropertySize(headers);
        propertyListSize += getPropertySize(properties.getValue(Metadata.DELIVERY_MODE));
        propertyListSize += getPropertySize(properties.getValue(Metadata.PRIORITY));
        propertyListSize += getPropertySize(properties.getValue(Metadata.CORRELATION_ID));
        propertyListSize += getPropertySize(properties.getValue(REPLY_TO));
        propertyListSize += getPropertySize(properties.getValue(Metadata.EXPIRATION));
        propertyListSize += getPropertySize(properties.getValue(Metadata.MESSAGE_ID));
        propertyListSize += getPropertySize(properties.getValue(TIMESTAMP));
        propertyListSize += getPropertySize(properties.getValue(TYPE));
        propertyListSize += getPropertySize(properties.getValue(USER_ID));
        propertyListSize += getPropertySize(properties.getValue(APPLICATION_ID));

        return 2L     // classID
                + 2L // weight
                + 8L // body size
                + 2L // property flag
                + propertyListSize;
    }

    private long getPropertySize(FieldValue fieldValue) {
        if (fieldValue != null) {
            return fieldValue.getValueSize();
        } else {
            return 0L;
        }
    }

    private long getPropertySize(FieldTable fieldTable) {
        if (fieldTable != null) {
            return fieldTable.getSize();
        } else {
            return 0L;
        }
    }

    @Override
    public void writePayload(ByteBuf buf) {
        buf.writeShort(classId);
        buf.writeShort(0); // Write 0 for weight
        buf.writeLong(bodySize);

        int propertyFlags = getPropertyFlagsValue(properties.getValue(PROPERTY_FLAGS));

        buf.writeShort(propertyFlags);
        writeProperty(buf, properties.getValue(Metadata.CONTENT_TYPE));
        writeProperty(buf, properties.getValue(Metadata.CONTENT_ENCODING));
        writeFieldTable(buf, headers);
        writeProperty(buf, properties.getValue(Metadata.DELIVERY_MODE));
        writeProperty(buf, properties.getValue(Metadata.PRIORITY));
        writeProperty(buf, properties.getValue(Metadata.CORRELATION_ID));
        writeProperty(buf, properties.getValue(REPLY_TO));
        writeProperty(buf, properties.getValue(Metadata.EXPIRATION));
        writeProperty(buf, properties.getValue(Metadata.MESSAGE_ID));
        writeProperty(buf, properties.getValue(TIMESTAMP));
        writeProperty(buf, properties.getValue(TYPE));
        writeProperty(buf, properties.getValue(USER_ID));
        writeProperty(buf, properties.getValue(APPLICATION_ID));
    }

    private int getPropertyFlagsValue(FieldValue value) {
        if (value == null) {
            return updatePropertyFlags();
        } else {
            return ((LongInt) value.getValue()).getInt();
        }
    }

    private int updatePropertyFlags() {

        int flags = 0;
        if (properties.getValue(Metadata.CONTENT_TYPE) != null) {
            flags |= CONTENT_TYPE_MASK;
        }
        if (properties.getValue(Metadata.CONTENT_ENCODING) != null) {
            flags |= ENCODING_MASK;
        }
        if (headers != null) {
            flags |= HEADERS_MASK;
        }
        if (properties.getValue(Metadata.DELIVERY_MODE) != null) {
            flags |= DELIVERY_MODE_MASK;
        }
        if (properties.getValue(Metadata.PRIORITY) != null) {
            flags |= PRIORITY_MASK;
        }
        if (properties.getValue(Metadata.CORRELATION_ID) != null) {
            flags |= CORRELATION_ID_MASK;
        }
        if (properties.getValue(REPLY_TO) != null) {
            flags |= REPLY_TO_MASK;
        }
        if (properties.getValue(Metadata.EXPIRATION) != null) {
            flags |= EXPIRATION_MASK;
        }
        if (properties.getValue(Metadata.MESSAGE_ID) != null) {
            flags |= MESSAGE_ID_MASK;
        }
        if (properties.getValue(TIMESTAMP) != null) {
            flags |= TIMESTAMP_MASK;
        }
        if (properties.getValue(TYPE) != null) {
            flags |= TYPE_MASK;
        }
        if (properties.getValue(USER_ID) != null) {
            flags |= USER_ID_MASK;
        }
        if (properties.getValue(APPLICATION_ID) != null) {
            flags |= APPLICATION_ID_MASK;
        }
        return flags;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        AmqpChannel channel = connectionHandler.getChannel(getChannel());

        InMemoryMessageAggregator inMemoryMessageAggregator = channel.getMessageAggregator();
        inMemoryMessageAggregator.headerFrameReceived(headers, properties, bodySize);
    }

    private void writeProperty(ByteBuf buf, FieldValue fieldValue) {
        EncodableData data;
        if (fieldValue != null && (data = fieldValue.getValue()) != null) {
            data.write(buf);
        }
    }

    private void writeFieldTable(ByteBuf buf, FieldTable fieldTable) {
        if (fieldTable != null) {
            fieldTable.write(buf);
        }
    }

    public static HeaderFrame parse(ByteBuf buf, int channel) throws Exception {
        int classId = buf.readUnsignedShort();
        // ignore weight
        buf.skipBytes(2);
        long bodySize = buf.readLong();
        HeaderFrame headerFrame = new HeaderFrame(channel, classId, bodySize);

        int propertyFlags = buf.readUnsignedShort();

        // Skip other property flags if exists
        boolean hasMoreProperties = (propertyFlags & LAST_BIT_MASK) != 0;
        while (hasMoreProperties) {
            int otherPropertyFlags = buf.readUnsignedShort();
            hasMoreProperties = (otherPropertyFlags & LAST_BIT_MASK) != 0;
        }

        // read known properties
        if ((propertyFlags & CONTENT_TYPE_MASK) != 0) {
            headerFrame.setContentType(ShortString.parse(buf));
        }

        if ((propertyFlags & ENCODING_MASK) != 0) {
            headerFrame.setContentEncoding(ShortString.parse(buf));
        }

        if ((propertyFlags & HEADERS_MASK) != 0) {
            headerFrame.setHeaders(FieldTable.parse(buf));
        }

        if ((propertyFlags & DELIVERY_MODE_MASK) != 0) {
            headerFrame.setDeliveryMode(buf.readUnsignedByte());
        }

        if ((propertyFlags & PRIORITY_MASK) != 0) {
            headerFrame.setPriority(buf.readUnsignedByte());
        }

        if ((propertyFlags & CORRELATION_ID_MASK) != 0) {
            headerFrame.setCorrelationId(ShortString.parse(buf));
        }

        if ((propertyFlags & REPLY_TO_MASK) != 0) {
            headerFrame.setReplyTo(ShortString.parse(buf));
        }

        if ((propertyFlags & EXPIRATION_MASK) != 0) {
            headerFrame.setExpiration(ShortString.parse(buf));
        }

        if ((propertyFlags & MESSAGE_ID_MASK) != 0) {
            headerFrame.setMessageId(ShortString.parse(buf));
        }

        if ((propertyFlags & TIMESTAMP_MASK) != 0) {
            headerFrame.setTimestamp(buf.readLong());
        }

        if ((propertyFlags & TYPE_MASK) != 0) {
            headerFrame.setType(ShortString.parse(buf));
        }

        if ((propertyFlags & USER_ID_MASK) != 0) {
            headerFrame.setUserId(ShortString.parse(buf));
        }

        if ((propertyFlags & APPLICATION_ID_MASK) != 0) {
            headerFrame.setAppId(ShortString.parse(buf));
        }

        headerFrame.properties.add(PROPERTY_FLAGS, FieldValue.parseLongInt(propertyFlags));
        return headerFrame;
    }

    public void setContentType(ShortString contentType) {
        properties.add(Metadata.CONTENT_TYPE, FieldValue.parseShortString(contentType));
    }

    public void setContentEncoding(ShortString contentEncoding) {
        properties.add(Metadata.CONTENT_ENCODING, FieldValue.parseShortString(contentEncoding));
    }

    public void setHeaders(FieldTable headers) {
        this.headers = headers;
    }

    public void setDeliveryMode(short deliveryMode) {
        properties.add(Metadata.DELIVERY_MODE, FieldValue.parseShortShortInt((byte) deliveryMode));
    }

    public void setPriority(short priority) {
        properties.add(Metadata.PRIORITY, FieldValue.parseShortShortInt((byte) priority));
    }

    public void setCorrelationId(ShortString correlationId) {
        properties.add(Metadata.CORRELATION_ID, FieldValue.parseShortString(correlationId));
    }

    public void setReplyTo(ShortString replyTo) {
        properties.add(REPLY_TO, FieldValue.parseShortString(replyTo));
    }

    public void setExpiration(ShortString expiration) {
        properties.add(Metadata.EXPIRATION, FieldValue.parseShortString(expiration));
    }

    public void setMessageId(ShortString messageId) {
        properties.add(Metadata.MESSAGE_ID, FieldValue.parseShortString(messageId));
    }

    public void setTimestamp(long timestamp) {
        properties.add(TIMESTAMP, FieldValue.parseLongLongInt(timestamp));
    }

    public void setType(ShortString type) {
        properties.add(TYPE, FieldValue.parseShortString(type));
    }

    public void setUserId(ShortString userId) {
        properties.add(USER_ID, FieldValue.parseShortString(userId));
    }

    public void setAppId(ShortString appId) {
        properties.add(APPLICATION_ID, FieldValue.parseShortString(appId));
    }

    public void setProperties(FieldTable properties) {
        this.properties = properties;
    }
}
