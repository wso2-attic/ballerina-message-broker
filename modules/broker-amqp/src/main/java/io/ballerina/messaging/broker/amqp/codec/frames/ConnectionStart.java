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

import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.LongString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP connection.start frame.
 */
public class ConnectionStart extends MethodFrame {
    public static final ConnectionStart DEFAULT_FRAME = new ConnectionStart((short) 0,
                                                                            (byte) 0,
                                                                            (byte) 9,
                                                                            FieldTable.EMPTY_TABLE,
                                                                            LongString.parseString("PLAIN"),
                                                                            LongString.parseString("en_US"));
    private final byte majorVersion;
    private final byte minorVersion;
    private final FieldTable serverProperties;
    private final LongString mechanisms;
    private final LongString locales;

    public ConnectionStart(short channel, byte majorVersion, byte minorVersion,
            FieldTable serverProperties, LongString mechanisms, LongString locales) {
        super(channel, (short) 10, (short) 10);
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.serverProperties = serverProperties;
        this.mechanisms = mechanisms;
        this.locales = locales;
    }

    @Override
    protected long getMethodBodySize() {
        return 1L + 1L + serverProperties.getSize() + mechanisms.getSize() + locales.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeByte(majorVersion);
        buf.writeByte(minorVersion);
        serverProperties.write(buf);
        mechanisms.write(buf);
        locales.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // Server does not normally receive this message
    }
}
