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

package io.ballerina.messaging.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;

/**
 * AMQP Method frame.
 */
public abstract class MethodFrame extends GeneralFrame {
    private final short classId;
    private final short methodId;

    public MethodFrame(int channel, short classId, short methodId) {
        super((byte) 1, channel);
        this.classId = classId;
        this.methodId = methodId;
    }

    protected abstract long getMethodBodySize();

    protected abstract void writeMethod(ByteBuf buf);

    public long getPayloadSize() {
        return getMethodBodySize() + 4;
    }

    public void writePayload(ByteBuf buf) {
        buf.writeShort(classId);
        buf.writeShort(methodId);

        writeMethod(buf);
    }
}
