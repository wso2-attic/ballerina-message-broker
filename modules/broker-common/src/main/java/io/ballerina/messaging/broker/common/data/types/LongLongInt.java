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
 * AMQP long-long-int.
 */
public class LongLongInt implements EncodableData {

    private final long value;

    private LongLongInt(long value) {
        this.value = value;
    }

    @Override
    public long getSize() {
        return 8L;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(value);
    }

    public static LongLongInt parse(long value) {
        return new LongLongInt(value);
    }

    public static LongLongInt parse(ByteBuf buf) {
        return new LongLongInt(buf.readLong());
    }

    public long getLong() {
        return value;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof LongLongInt) && (value == ((LongLongInt) obj).value);
    }

    @Override public String toString() {
        return String.valueOf(value);
    }
}
