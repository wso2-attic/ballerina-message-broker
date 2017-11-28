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
import org.wso2.broker.amqp.codec.data.EncodableData;
import org.wso2.broker.amqp.codec.data.FieldTable;
import org.wso2.broker.amqp.codec.data.ShortString;

/**
 * AMQP header frame
 */
public class HeaderFrame extends GeneralFrame {

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

    public HeaderFrame(int channel, int classId, long bodySize) {
        super((byte) 2, channel);
        this.classId = classId;
        this.bodySize = bodySize;
    }

    @Override
    public long getPayloadSize() {
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

    private long getPropertySize(long property) {
        if (property != LONG_DEFAULT) {
            return property;
        } else {
            return 0L;
        }
    }

    private long getPropertySize(short property) {
        if (property != BYTE_DEFAULT) {
            return property;
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
            buf.writeShort(property);
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

        return headerFrame;
    }

    private void setContentType(ShortString contentType) {
        propertyFlags |= CONTENT_TYPE_MASK;
        this.contentType = contentType;
    }

    private void setContentEncoding(ShortString contentEncoding) {
        propertyFlags |= ENCODING_MASK;
        this.contentEncoding = contentEncoding;
    }

    private void setHeaders(FieldTable headers) {
        propertyFlags |= HEADERS_MASK;
        this.headers = headers;
    }

    private void setDeliveryMode(short deliveryMode) {
        propertyFlags |= DELIVERY_MODE_MASK;
        this.deliveryMode = deliveryMode;
    }

    private void setPriority(short priority) {
        propertyFlags |= PRIORITY_MASK;
        this.priority = priority;
    }

    private void setCorrelationId(ShortString correlationId) {
        propertyFlags |= CORRELATION_ID_MASK;
        this.correlationId = correlationId;
    }

    private void setReplyTo(ShortString replyTo) {
        propertyFlags |= REPLY_TO_MASK;
        this.replyTo = replyTo;
    }

    private void setExpiration(ShortString expiration) {
        propertyFlags |= EXPIRATION_MASK;
        this.expiration = expiration;
    }

    private void setMessageId(ShortString messageId) {
        propertyFlags |= MESSAGE_ID_MASK;
        this.messageId = messageId;
    }

    private void setTimestamp(long timestamp) {
        propertyFlags |= TIMESTAMP_MASK;
        this.timestamp = timestamp;
    }

    private void setType(ShortString type) {
        propertyFlags |= TYPE_MASK;
        this.type = type;
    }

    private void setUserId(ShortString userId) {
        propertyFlags |= USER_ID_MASK;
        this.userId = userId;
    }

    private void setAppId(ShortString appId) {
        propertyFlags |= APPLICATION_ID_MASK;
        this.appId = appId;
    }
}
