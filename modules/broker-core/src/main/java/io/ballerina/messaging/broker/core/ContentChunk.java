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

package io.ballerina.messaging.broker.core;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;

/**
 * Represents part of the content of a message.
 */
public class ContentChunk {

    private final long offset;

    private final ByteBuf content;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Data holder class for content chunks.")
    public ContentChunk(long offset, ByteBuf content) {
        this.offset = offset;
        this.content = content;
    }

    public long getOffset() {
        return offset;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Data holder class for content chunks.")
    public ByteBuf getByteBuf() {
        return content;
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[content.readableBytes()];
        content.getBytes(0, bytes);
        return bytes;
    }

    public void release() {
        content.release();
    }

    ContentChunk shallowCopy() {
        return new ContentChunk(offset, content.retainedSlice());
    }
}
