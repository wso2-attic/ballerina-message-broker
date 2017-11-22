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

/**
 * AMQP Field value data
 *
 * field-value = 't' boolean
 *               'b' short-short-int
 *               'B' short-short-uint
 *               'U' short-int
 *               'u' short-uint
 *               'I' long-int
 *               'i' long-uint
 *               'L' long-long-int
 *               'l' long-long-uint
 *               'f' float
 *               'd' double
 *               'D' decimal-value
 *               's' short-string
 *               'S' long-string
 *               'A' field-array
 *               'T' timestamp
 *               'F' field-table
 *               'V'               ; no field
 */
public class FieldValue implements EncodableData {
    private final char type;
    private final EncodableData value;

    public FieldValue(char type, EncodableData value) {
        this.type = type;
        this.value = value;
    }

    public long getSize() {
        return 1 + value.getSize();
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeChar(type);
        value.write(buf);
    }

    public static FieldValue parse(ByteBuf buf) throws Exception {
        char type = (char) buf.readByte();

        switch (type) {
            case 'S':
                return new FieldValue(type, LongString.parse(buf));
            default:
                throw new Exception("Invalid AMQP Field value type");
        }
    }
}
