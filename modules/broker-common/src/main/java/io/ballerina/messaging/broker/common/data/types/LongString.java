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
 * AMQP Long String.
 *
 * long-string = long-uint *OCTET ; length + content
 */
public class LongString implements EncodableData {
    private final long length;
    private final byte[] content;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public LongString(long length, byte[] content) {
        this.length = length;
        this.content = content;
    }

    public static LongString parseString(String data) {
        return new LongString(data.length(), data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public long getSize() {
        // We need to add bytes used for size as well
        return 4 + length;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt((int) length);
        buf.writeBytes(content);
    }

    public static LongString parse(ByteBuf buf) throws Exception {
        int size = (int) buf.readUnsignedInt();

        if (size < 0) {
            throw new Exception("Invalid string length");
        }

        byte[] data = new byte[size];
        buf.readBytes(data);

        return new LongString(size, data);
    }

    public static LongString parse(byte[] data) {
        return new LongString(data.length, data);
    }

    /**
     * Compares {@link LongString} underlying byte array content.
     *
     * @param obj {@link Object} to compare with
     * @return True if underlying content is equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof LongString) && (Arrays.equals(content, ((LongString) obj).content));
    }

    public boolean isEmpty() {
        return content.length == 0;
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

    @Override
    public String toString() {
        return new String(content, StandardCharsets.UTF_8);
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public byte[] getBytes() {
        return content;
    }
}
