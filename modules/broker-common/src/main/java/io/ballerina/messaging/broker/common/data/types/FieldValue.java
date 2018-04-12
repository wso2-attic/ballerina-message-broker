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

package io.ballerina.messaging.broker.common.data.types;

import io.netty.buffer.ByteBuf;

/**
 *  AMQP Field value data.
 * +-------+-------------+-----------------+
 * | 0-9-1 | Qpid/Rabbit |      Type       |
 * +-------+-------------+-----------------+
 * | t     | t           | Boolean         |
 * | b     | b           | Signed 8-bit    |
 * | B     | B           | Unsigned 8-bit  |
 * | U     | s           | Signed 16-bit   |
 * | u     | u           | Unsigned 16-bit |
 * | I     | I           | Signed 32-bit   |
 * | i     | i           | Unsigned 32-bit |
 * | L     | l           | Signed 64-bit   |
 * | l     |             | Unsigned 64-bit |
 * | f     | f           | 32-bit float    |
 * | d     | d           | 64-bit float    |
 * | D     | D           | Decimal         |
 * | s     |             | Short string    |
 * | S     | S           | Long string     |
 * | A     | A           | Array           |
 * | T     | T           | Timestamp (u64) |
 * | F     | F           | Nested Table    |
 * | V     | V           | Void            |
 * |       | x           | Byte array      |
 * +-------+-------------+-----------------+
 */
public class FieldValue implements EncodableData {

    private final Type type;

    private final EncodableData value;

    /**
     * Denotes the {@link FieldValue} data type.
     */
    public enum Type {
        BOOLEAN('t'),
        SHORT_SHORT_INT('b'),
        SHORT_SHORT_UINT('B'),
        SHORT_INT('s'),
        SHORT_UINT('u'),
        LONG_INT('I'),
        LONG_UINT('i'),
        LONG_LONG_INT('l'),
        FLOAT('f'),
        DOUBLE('d'),
        DECIMAL('D'),
        LONG_STRING('S'),
        FIELD_TABLE('F'),
        SHORT_STRING('z'); // define internally because HeaderFrame use FieldTable

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
                case 's':
                    return SHORT_INT;
                case 'u':
                    return SHORT_UINT;
                case 'I':
                    return LONG_INT;
                case 'i':
                    return LONG_UINT;
                case 'l':
                    return LONG_LONG_INT;
                case 'f':
                    return FLOAT;
                case 'd':
                    return DOUBLE;
                case 'D':
                    return DECIMAL;
                case 'S':
                    return LONG_STRING;
                case 'F':
                    return FIELD_TABLE;
                case 'z':
                    return SHORT_STRING;
                default:
                    throw new Exception("Unknown field table data type. Char value: '" + value + "'");
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

    /**
     * Retrieve size of the underlying {@link EncodableData}.
     *
     * @return value size in bytes
     */
    public long getValueSize() {
        return value.getSize();
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(type.getChar());
        value.write(buf);
    }

    public static FieldValue parse(ByteBuf buf) throws Exception {
        Type type = Type.valueOf((char) buf.readByte());
        switch (type) {
            case BOOLEAN:
                return FieldValue.parseBoolean(io.ballerina.messaging.broker.common.data.types.Boolean.parse(buf));
            case SHORT_SHORT_INT:
                return FieldValue.parseShortShortInt(ShortShortInt.parse(buf));
            case SHORT_SHORT_UINT:
                return FieldValue.parseShortShortUint(ShortShortUint.parse(buf));
            case SHORT_INT:
                return FieldValue.parseShortInt(ShortInt.parse(buf));
            case SHORT_UINT:
                return FieldValue.parseShortUint(ShortUint.parse(buf));
            case LONG_INT:
                return FieldValue.parseLongInt(LongInt.parse(buf));
            case LONG_UINT:
                return FieldValue.parseLongUint(LongUint.parse(buf));
            case LONG_LONG_INT:
                return FieldValue.parseLongLongInt(LongLongInt.parse(buf));
            case FLOAT:
                return FieldValue.parseFloat(Float.parse(buf));
            case DOUBLE:
                return FieldValue.parseDouble(Double.parse(buf));
            case DECIMAL:
                return FieldValue.parseDecimal(Decimal.parse(buf));
            case LONG_STRING:
                return FieldValue.parseLongString(LongString.parse(buf));
            case FIELD_TABLE:
                return FieldValue.parseFieldTable(FieldTable.parse(buf));
            case SHORT_STRING:
                return FieldValue.parseShortString(ShortString.parse(buf));
            default:
                throw new Exception("Unsupported AMQP field value type " + type);
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

    public static FieldValue parseLongInt(LongInt value) {
        return new FieldValue(Type.LONG_INT, value);
    }

    public static FieldValue parseLongString(String value) {
        return parseLongString(LongString.parseString(value));
    }

    public static FieldValue parseLongString(LongString value) {
        return new FieldValue(Type.LONG_STRING, value);
    }

    public static FieldValue parseShortString(String value) {
        return parseShortString(ShortString.parseString(value));
    }

    public static FieldValue parseShortString(ShortString value) {
        return new FieldValue(Type.SHORT_STRING, value);
    }

    public static FieldValue parseBoolean(io.ballerina.messaging.broker.common.data.types.Boolean amqpBoolean) {
        return new FieldValue(Type.BOOLEAN, amqpBoolean);
    }

    public static FieldValue parseShortShortInt(byte value) {
        return parseShortShortInt(ShortShortInt.parseByte(value));
    }

    public static FieldValue parseShortShortInt(ShortShortInt value) {
        return new FieldValue(Type.SHORT_SHORT_INT, value);
    }

    public static FieldValue parseLongLongInt(long value) {
        return new FieldValue(Type.LONG_LONG_INT, LongLongInt.parse(value));
    }

    public static FieldValue parseLongLongInt(LongLongInt value) {
        return new FieldValue(Type.LONG_LONG_INT, value);
    }

    public static FieldValue parseFieldTable(FieldTable fieldTable) {
        return new FieldValue(Type.FIELD_TABLE, fieldTable);
    }

    public static FieldValue parseShortInt(ShortInt value) {
        return new FieldValue(Type.SHORT_INT, value);
    }

    public static FieldValue parseFloat(Float value) {
        return new FieldValue(Type.FLOAT, value);
    }

    public static FieldValue parseDouble(Double value) {
        return new FieldValue(Type.DOUBLE, value);
    }

    public static FieldValue parseDecimal(Decimal value) {
        return new FieldValue(Type.DECIMAL, value);
    }

    public static FieldValue parseShortShortUint(ShortShortUint value) {
        return new FieldValue(Type.SHORT_SHORT_UINT, value);
    }

    public static FieldValue parseShortUint(ShortUint value) {
        return new FieldValue(Type.SHORT_UINT, value);
    }

    public static FieldValue parseLongUint(LongUint value) {
        return new FieldValue(Type.LONG_UINT, value);
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

    @Override
    public String toString() {
        return value.toString();
    }
}
