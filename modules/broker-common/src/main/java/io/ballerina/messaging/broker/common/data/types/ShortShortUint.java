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
 * AMQP short-short-uint
 */
public class ShortShortUint implements EncodableData {

    private final short value;

    private ShortShortUint(short value) {
        this.value = value;
    }

    @Override
    public long getSize() {
        return 1L;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(value);
    }

    public static ShortShortUint parse(ByteBuf buf) {
        return new ShortShortUint(buf.readUnsignedByte());
    }

    public static ShortShortUint parse(short value) {
        return new ShortShortUint(value);
    }

    public short getByte() {
        return value;
    }

    @Override
    public int hashCode() {
        return (int) value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof ShortShortUint) && (value == ((ShortShortUint) obj).value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
