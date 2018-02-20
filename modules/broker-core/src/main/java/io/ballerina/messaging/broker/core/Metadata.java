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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object representation of a message metadata.
 */
public class Metadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(Metadata.class);

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
        this.properties = FieldTable.EMPTY_TABLE;
        this.headers = FieldTable.EMPTY_TABLE;
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

    public Metadata shallowCopy() {
        Metadata metadata = shallowCopyWith(routingKey, exchangeName);
        return metadata;
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
                + ", contentLength=" + contentLength
                + ", messageId= " + properties.getValue(MESSAGE_ID) + '\''
                + ", deliveryMode=" + properties.getValue(DELIVERY_MODE) + '\''
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
}
