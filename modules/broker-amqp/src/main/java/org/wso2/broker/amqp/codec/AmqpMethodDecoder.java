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

package org.wso2.broker.amqp.codec;

import io.netty.buffer.ByteBuf;
import org.wso2.broker.amqp.codec.frames.AmqMethodBodyFactory;
import org.wso2.broker.amqp.codec.frames.AmqMethodRegistry;

import java.util.List;

/**
 * Decode AMQP frame looking at type, class ID and method ID.
 */
public class AmqpMethodDecoder {
    private static final int SIZE_WITHOUT_PAYLOAD = 8;
    private static final AmqMethodRegistry methodRegistry = new AmqMethodRegistry();

    public static void parse(ByteBuf buffer, List<Object> out) throws Exception {
        buffer.markReaderIndex();
        if (buffer.readableBytes() > SIZE_WITHOUT_PAYLOAD) {
            byte type = buffer.readByte();
            int channel = buffer.readShort();
            long payloadSize = buffer.readInt();

            long estimatedRemainingSize = payloadSize + 1;
            if (buffer.readableBytes() < estimatedRemainingSize) {
                buffer.resetReaderIndex();
                return;
            }

            switch (type) {

                case 1: // Method
                    short amqpClass = buffer.readShort();
                    short amqpMethod = buffer.readShort();
                    AmqMethodBodyFactory factory = methodRegistry.getFactory(amqpClass, amqpMethod);
                    out.add(factory.newInstance(buffer, channel, payloadSize));
                    // TODO: check if end frame is correct
                    buffer.readByte();
                    return;
                case 2: // Header
                    throw new Exception("Method Not implemented");
                case 3: // Body
                    throw new Exception("Method Not implemented");
                case 4: // Heartbeat
                    throw new Exception("Method Not implemented");
            }

        }
    }

}

