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

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.common.data.types.ShortShortInt;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Object representation of a message metadata.
 */
public class Metadata {

    public static final ShortString DELIVERY_MODE = ShortString.parseString("deliveryMode");

    public static final ShortString PRIORITY = ShortString.parseString("priority");

    public static final ShortString EXPIRATION = ShortString.parseString("expiration");

    public static final ShortString MESSAGE_ID = ShortString.parseString("messageId");

    public static final ShortString CONTENT_TYPE = ShortString.parseString("contentType");

    public static final ShortString CONTENT_ENCODING = ShortString.parseString("contentEncoding");

    public static final ShortString CORRELATION_ID = ShortString.parseString("correlationId");

    public static final int PERSISTENT_MESSAGE = 2;

    public static final int NON_PERSISTENT_MESSAGE = 1;

    /**
     * Key value used by the router (exchange) to identify the relevant queue(s) for this message.
     */
    private final String routingKey;

    /**
     * Exchange this message arrived to.
     */
    private final String exchangeName;

    /**
     * Byte length of the content.
     */
    private final long contentLength;

    private FieldTable properties;

    private FieldTable headers;

    public Metadata(String routingKey, String exchangeName, long contentLength) {
        this.routingKey = routingKey;
        this.exchangeName = exchangeName;
        this.contentLength = contentLength;
        this.properties = new FieldTable();
        this.headers = new FieldTable();
    }

    public Metadata(String routingKey, String exchangeName, long contentLength, byte[] propertyBytes) throws Exception {
        this.routingKey = routingKey;
        this.exchangeName = exchangeName;
        this.contentLength = contentLength;
        this.properties = new FieldTable();
        this.headers = new FieldTable();
        setPropertiesFromBytes(propertyBytes);
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public Metadata shallowCopyWith(String routingKey, String exchangeName) {
        Metadata metadata = new Metadata(routingKey, exchangeName, contentLength);
        metadata.properties = properties;
        metadata.headers = headers;
        return metadata;
    }

    @Override
    public String toString() {
        return "Metadata{"
                + "routingKey='" + routingKey + '\''
                + ", exchangeName='" + exchangeName + '\''
                + ", correlationId='" + properties.getValue(CORRELATION_ID) + '\''
                + ", contentLength='" + contentLength + '\''
                + ", messageId='" + properties.getValue(MESSAGE_ID) + '\''
                + ", deliveryMode='" + properties.getValue(DELIVERY_MODE) + '\''
                + "'}";
    }

    public void setProperties(FieldTable properties) {
        this.properties = properties;
    }

    public void setHeaders(FieldTable headers) {
        this.headers = headers;
    }

    public FieldValue getProperty(ShortString propertyName) {
        return this.properties.getValue(propertyName);
    }

    public byte getByteProperty(ShortString propertyName) {
        FieldValue fieldValue = properties.getValue(propertyName);
        return ((ShortShortInt) fieldValue.getValue()).getByte();
    }

    public boolean isPersistent() {
        return getByteProperty(Metadata.DELIVERY_MODE) == Metadata.PERSISTENT_MESSAGE;
    }

    public FieldValue getHeader(ShortString headerName) {
        return headers.getValue(headerName);
    }

    public FieldTable getProperties() {
        return properties;
    }

    public FieldTable getHeaders() {
        return headers;
    }

    public void addHeader(String name, String value) {
        headers.add(ShortString.parseString(name), FieldValue.parseLongString(value));
    }

    public byte[] getPropertiesAsBytes() {
        long size = properties.getSize() + headers.getSize();
        byte[] bytes = new byte[(int) size];
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        buffer.resetWriterIndex();
        properties.write(buffer);
        headers.write(buffer);
        return bytes;
    }

    private void setPropertiesFromBytes(byte[] bytes) throws Exception {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        properties = FieldTable.parse(buffer);
        headers = FieldTable.parse(buffer);
    }
}
