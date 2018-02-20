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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * AMQP Short String.
 *
 * short-string = OCTET *string-char ; length + content
 */
public class ShortString implements EncodableData {
    private static final int MAX_LENGTH = 0xFF;
    private final long length;

    // TODO maybe we should keep a char sequence instead of a byte array.
    // We should do a perf test before switching to char sequence.
    private final byte[] content;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public ShortString(long length, byte[] content) {
        this.length = length;
        this.content = content;
    }

    public long getSize() {
        return length + 1;
    }

    public void write(ByteBuf buf) {
        buf.writeByte((int) length);
        buf.writeBytes(content);
    }

    public static ShortString parse(ByteBuf buf) {
        int size = buf.readUnsignedByte();
        byte[] data = new byte[size];
        buf.readBytes(data);

        return new ShortString(size, data);
    }

    public static ShortString parseString(String data) {
        return new ShortString(data.length(), data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return new String(content, StandardCharsets.UTF_8);
    }

    /**
     * Compares {@link ShortString} underlying byte array content.
     *
     * @param obj {@link Object} to compare with
     * @return True if underlying content is equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof ShortString) && (Arrays.equals(content, ((ShortString) obj).content));
    }

    /**
     * Hashcode of the underlying byte array content.
     *
     * @return content based hashcode
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }

    public boolean isEmpty() {
        return length == 0;
    }
}
