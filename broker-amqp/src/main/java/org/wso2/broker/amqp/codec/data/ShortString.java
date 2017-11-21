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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * AMQP Short String
 *
 * short-string = OCTET *string-char ; length + content
 */
public class ShortString implements EncodableData {
    private static final int MAX_LENGTH = 0xFF;
    private final byte length;
    private final byte[] content;

    public ShortString(byte length, char[] content) {
        this.length = length;
        this.content = new String(content).getBytes(StandardCharsets.UTF_8);
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public ShortString(byte length, byte[] content) {
        this.length = length;
        this.content = content;
    }

    public int getSize() {
        return length;
    }

    public void write(ByteBuf buf) {
        buf.writeByte(length);
        buf.writeBytes(content);
    }
}
