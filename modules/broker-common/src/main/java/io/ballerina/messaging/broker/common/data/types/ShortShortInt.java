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
 * AMQP Short-Short-Int.
 */
public class ShortShortInt implements EncodableData {

    private byte value;

    private ShortShortInt(byte value) {
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

    public static ShortShortInt parse(ByteBuf buf) {
        return new ShortShortInt(buf.readByte());
    }

    public static ShortShortInt parse(byte value) {
        return new ShortShortInt(value);
    }

    public byte getByte() {
        return value;
    }

    public static ShortShortInt parseByte(byte value) {
        return new ShortShortInt(value);
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
        return (obj instanceof ShortShortInt) && (value == ((ShortShortInt) obj).value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
