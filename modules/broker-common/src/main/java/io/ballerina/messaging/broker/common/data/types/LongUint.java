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

import java.util.Objects;

/**
 * AMQP long-uint.
 */
public class LongUint implements EncodableData {

    private final long value;

    private LongUint(long value) {
        this.value = value;
    }

    @Override
    public long getSize() {
        return 4L;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt((int) value);
    }

    public long getInt() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof LongUint) && (value == ((LongUint) obj).value);
    }

    public static LongUint parse(ByteBuf buf) {
        return new LongUint(buf.readUnsignedInt());
    }

    public static LongUint parse(long value) {
        return new LongUint(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
