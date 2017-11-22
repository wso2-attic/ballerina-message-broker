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

package org.wso2.broker.amqp.codec.data;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * AMQP FieldTable data
 * <p>
 * field-table = long-uint *field-value-pair
 */
public class FieldTable implements EncodableData {
    public static final FieldTable EMPTY_TABLE = new FieldTable(new HashMap<>());
    private final Map<ShortString, FieldValue> properties;

    /**
     * Used to cache size to avoid recalculating size
     */
    private long size = -1L;

    public FieldTable(Map<ShortString, FieldValue> properties) {
        this.properties = properties;
    }

    public long getSize() {
        long tableEntrySize = 0L;
        for (Map.Entry<ShortString, FieldValue> fieldEntry : properties.entrySet()) {
            tableEntrySize = tableEntrySize + fieldEntry.getKey().getSize() + fieldEntry.getValue().getSize();
        }
        size = tableEntrySize;
        return 4L + tableEntrySize;
    }

    public void write(ByteBuf buf) {
        if (size != -1L) {
            writeWithoutCalculatingSize(buf);
        } else {
            writeWithCalculatedSize(buf);
        }
    }

    private void writeWithCalculatedSize(ByteBuf buf) {
        int sizeIndex = buf.writerIndex();
        buf.writerIndex(sizeIndex + 4);

        long tableEntrySize = 0L;
        for (Map.Entry<ShortString, FieldValue> fieldEntry : properties.entrySet()) {
            ShortString key = fieldEntry.getKey();
            FieldValue value = fieldEntry.getValue();

            tableEntrySize = tableEntrySize + key.getSize() + value.getSize();
            key.write(buf);
            value.write(buf);
        }

        buf.setInt(sizeIndex, (int) tableEntrySize);
    }

    private void writeWithoutCalculatingSize(ByteBuf buf) {
        buf.writeInt((int) size);

        for (Map.Entry<ShortString, FieldValue> fieldEntry : properties.entrySet()) {
            fieldEntry.getKey().write(buf);
            fieldEntry.getValue().write(buf);
        }
    }

    public static FieldTable parse(ByteBuf buf) throws Exception {
        long size = buf.readUnsignedInt();
        long readBytes = 0L;
        Map<ShortString, FieldValue> properties = new HashMap<>();

        while (readBytes < size) {
            ShortString key = ShortString.parse(buf);
            FieldValue value = FieldValue.parse(buf);
            properties.put(key, value);

            readBytes = readBytes + key.getSize() + value.getSize();
        }

        return new FieldTable(properties);
    }
}
