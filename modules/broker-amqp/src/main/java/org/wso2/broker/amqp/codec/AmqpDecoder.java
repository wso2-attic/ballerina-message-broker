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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.frames.AmqpBadMessage;
import org.wso2.broker.amqp.codec.frames.ProtocolInitFrame;

import java.util.List;

/**
 * Netty based AMQP frame decoder.
 */
public class AmqpDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpDecoder.class);
    private static final CharSequence AMQP_PROTOCOL_IDENTIFIER = "AMQP";

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
                AmqpMethodDecoder.parse(buffer, out);
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
}
