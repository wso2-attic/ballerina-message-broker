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

package io.ballerina.messaging.broker.amqp.codec.handlers;

import io.ballerina.messaging.broker.amqp.codec.frames.AmqMethodBodyFactory;
import io.ballerina.messaging.broker.amqp.codec.frames.AmqMethodRegistry;
import io.ballerina.messaging.broker.amqp.codec.frames.AmqpBadMessage;
import io.ballerina.messaging.broker.amqp.codec.frames.ContentFrame;
import io.ballerina.messaging.broker.amqp.codec.frames.GeneralFrame;
import io.ballerina.messaging.broker.amqp.codec.frames.HeaderFrame;
import io.ballerina.messaging.broker.amqp.codec.frames.ProtocolInitFrame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Netty based AMQP frame decoder.
 */
public class AmqpDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpDecoder.class);

    /**
     * Used to lookup AMQP method frames depending on their class ID and method ID. We keep this as a static variable
     * since it is read only
     */
    private final AmqMethodRegistry methodRegistry;
    private static final int FRAME_SIZE_WITHOUT_PAYLOAD = 8;
    private static final CharSequence AMQP_PROTOCOL_IDENTIFIER = "AMQP";

    /**
     * class-id(short) + weight(short) + body-size(long long) + property-flags(short).
     */
    private static final int MIN_HEADER_FRAME_SIZE = 14;

    public AmqpDecoder(AmqMethodRegistry methodRegistry) {
        this.methodRegistry = methodRegistry;
    }

    /**
     * The internal state of {@link AmqpDecoder}.
     */
    private enum State {
        PROTOCOL_INITIALIZATION,
        READ_FRAME,
        BAD_MESSAGE
    }

    private State currentState = State.PROTOCOL_INITIALIZATION;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out)
            throws Exception {
        switch (currentState) {
            case PROTOCOL_INITIALIZATION:
                processProtocolInitFrame(buffer, out);
                currentState = State.READ_FRAME;
                return;
            case READ_FRAME:
                parseFrame(buffer, out);
                return;
            case BAD_MESSAGE:
                // Keep discarding until disconnection.
                buffer.skipBytes(actualReadableBytes());
                break;

            default:
                // Shouldn't reach here.
                throw new Error();
        }

    }

    private void processProtocolInitFrame(ByteBuf buffer, List<Object> out) {
        if (buffer.readableBytes() >= 8) {
            CharSequence protocolName = buffer.readCharSequence(4, CharsetUtil.US_ASCII);
            buffer.skipBytes(1);
            byte majorVersion = buffer.readByte();
            byte minorVersion = buffer.readByte();
            byte revision = buffer.readByte();

            if (!AMQP_PROTOCOL_IDENTIFIER.equals(protocolName)) {
                out.add(new AmqpBadMessage(new IllegalArgumentException("Unknown protocol name " +
                                                                               protocolName.toString())));
                currentState = State.BAD_MESSAGE;
            }

            out.add(new ProtocolInitFrame(majorVersion, minorVersion, revision));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.warn("Exception while handling request", cause);
        currentState = State.BAD_MESSAGE;
        ctx.close();
    }

    private void parseFrame(ByteBuf buffer, List<Object> out) throws Exception {
        buffer.markReaderIndex();
        if (buffer.readableBytes() > FRAME_SIZE_WITHOUT_PAYLOAD) {
            byte type = buffer.readByte();
            int channel = buffer.readShort();
            long payloadSize = buffer.readInt();

            long estimatedRemainingSize = payloadSize + 1;
            if (buffer.readableBytes() < estimatedRemainingSize) {
                buffer.resetReaderIndex();
                return;
            }

            GeneralFrame frame = null;
            switch (type) {
                case 1: // Method
                    short amqpClass = buffer.readShort();
                    short amqpMethod = buffer.readShort();
                    AmqMethodBodyFactory factory = methodRegistry.getFactory(amqpClass, amqpMethod);

                    frame = factory.newInstance(buffer, channel, payloadSize);
                    break;
                case 2: // Header
                    frame = HeaderFrame.parse(buffer, channel);
                    break;
                case 3: // Body
                    frame = ContentFrame.parse(buffer, channel, payloadSize);
                    break;
                case 4: // Heartbeat
                    throw new Exception("Method Not implemented");
            }

            byte frameEnd = buffer.readByte();
            if (frameEnd != (byte) GeneralFrame.FRAME_END) {
                throw new Exception("Invalid AMQP frame");
            }

            out.add(frame);
        }
    }
}
