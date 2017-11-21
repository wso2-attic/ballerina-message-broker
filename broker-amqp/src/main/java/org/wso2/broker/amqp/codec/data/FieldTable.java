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
    public static final FieldTable EMPTY_TABLE = new FieldTable(new HashMap<ShortString, FieldValue>());
    private final Map<ShortString, FieldValue> properties;

    public FieldTable(Map<ShortString, FieldValue> properties) {
        this.properties = properties;
    }

    public int getSize() {
        int tableEntrySize = 0;
        for (Map.Entry<ShortString, FieldValue> fieldEntry : properties.entrySet()) {
            tableEntrySize = tableEntrySize + fieldEntry.getKey().getSize() + fieldEntry.getValue().getSize();
        }
        return 4 + tableEntrySize;
    }

    public void write(ByteBuf buf) {
        int sizeIndex = buf.writerIndex();
        buf.writerIndex(sizeIndex + 4);

        int tableEntrySize = 0;
        for (Map.Entry<ShortString, FieldValue> fieldEntry : properties.entrySet()) {
            ShortString key = fieldEntry.getKey();
            FieldValue value = fieldEntry.getValue();

            tableEntrySize = tableEntrySize + key.getSize() + value.getSize();
            key.write(buf);
        }

        buf.setInt(sizeIndex, tableEntrySize);
    }
}
