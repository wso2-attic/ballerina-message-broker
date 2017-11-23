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

package org.wso2.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.wso2.broker.amqp.codec.AmqpConnectionHandler;
import org.wso2.broker.amqp.codec.data.FieldTable;
import org.wso2.broker.amqp.codec.data.LongString;
import org.wso2.broker.amqp.codec.data.ShortString;

/**
 * AMQP connection.start frame.
 */
public class ConnectionStartOk extends MethodFrame {
    private final FieldTable clientProperties;
    private final ShortString mechanisms;
    private final ShortString locales;
    private final LongString response;

    public ConnectionStartOk(int channel, FieldTable clientProperties, ShortString mechanisms,
            ShortString locales, LongString response) {
        super(channel, (short) 10, (short) 11);
        this.clientProperties = clientProperties;
        this.mechanisms = mechanisms;
        this.locales = locales;
        this.response = response;
    }

    @Override
    protected long getMethodBodySize() {
        return clientProperties.getSize() + mechanisms.getSize() + locales.getSize() + response.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        clientProperties.write(buf);
        mechanisms.write(buf);
        locales.write(buf);
        response.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        ctx.writeAndFlush(new ConnectionTune(256, 65535, 0));
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            FieldTable clientProperties = FieldTable.parse(buf);
            ShortString mechanisms = ShortString.parse(buf);
            LongString response = LongString.parse(buf);
            ShortString locale = ShortString.parse(buf);
            return new ConnectionStartOk(channel, clientProperties, mechanisms, locale, response);
        };
    }
}
