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

package org.wso2.broker.common.data.types;

import io.netty.buffer.ByteBuf;

/**
 * AMQP Field value data.
 * <p>
 * field-value = 't' boolean
 * 'b' short-short-int
 * 'B' short-short-uint
 * 'U' short-int
 * 'u' short-uint
 * 'I' long-int
 * 'i' long-uint
 * 'L' long-long-int
 * 'l' long-long-uint
 * 'f' float
 * 'd' double
 * 'D' decimal-value
 * 's' short-string
 * 'S' long-string
 * 'A' field-array
 * 'T' timestamp
 * 'F' field-table
 * 'V'               ; no field
 */
public class FieldValue implements EncodableData {

    private final Type type;

    private final EncodableData value;

    /**
     * Denotes the {@link FieldValue} data type
     */
    public enum Type {
        BOOLEAN('t'),
        SHORT_SHORT_INT('b'),
        SHORT_SHORT_UINT('B'),
        SHORT_INT('U'),
        SHORT_UINT('u'),
        SHORT_STRING('s'),
        LONG_STRING('S'),
        LONG_INT('I');

        private final char type;

        Type(char type) {
            this.type = type;
        }

        public char getChar() {
            return type;
        }

        public static Type valueOf(char value) throws Exception {
            switch (value) {
                case 't':
                    return BOOLEAN;
                case 'b':
                    return SHORT_SHORT_INT;
                case 'B':
                    return SHORT_SHORT_UINT;
                case 'U':
                    return SHORT_INT;
                case 'u':
                    return SHORT_UINT;
                case 'S':
                    return LONG_STRING;
                case 'I':
                    return LONG_INT;
                default:
                    throw new Exception("Unknown data type. Char value: '" + value + "'");
            }
        }
    }

    private FieldValue(Type type, EncodableData value) {
        this.type = type;
        this.value = value;
    }

    public long getSize() {
        return 1 + value.getSize();
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(type.getChar());
        value.write(buf);
    }

    public static FieldValue parse(ByteBuf buf) throws Exception {
        Type type = Type.valueOf((char) buf.readByte());
        switch (type) {
            case LONG_STRING:
                return new FieldValue(Type.LONG_STRING, LongString.parse(buf));
            case LONG_INT:
                return new FieldValue(Type.LONG_INT, LongInt.parse(buf));
            default:
                throw new Exception("Invalid AMQP Field value type");
        }
    }

    public Type getType() {
        return type;
    }

    public EncodableData getValue() {
        return value;
    }

    public static FieldValue parseLongInt(int value) {
        return new FieldValue(Type.LONG_INT, LongInt.parse(value));
    }

    public static FieldValue parseLongString(String value) {
        return new FieldValue(Type.LONG_STRING, LongString.parseString(value));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof FieldValue)
                && type == ((FieldValue) obj).type && (value.equals(((FieldValue) obj).value));
    }

    @Override
    public int hashCode() {
        int hash = value.hashCode();
        hash += type.getChar();
        return hash;
    }
}
