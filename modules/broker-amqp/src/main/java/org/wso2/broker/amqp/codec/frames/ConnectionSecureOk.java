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

package org.wso2.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqpConnectionHandler;
import org.wso2.broker.common.data.types.LongString;
import org.wso2.broker.common.data.types.ShortString;

import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

/**
 * AMQP connection.secure.ok frame.
 */
public class ConnectionSecureOk extends MethodFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionSecureOk.class);
    private final LongString response;

    public ConnectionSecureOk(int channel, LongString response) {
        super(channel, (short) 10, (short) 21);
        this.response = response;
    }

    @Override
    protected long getMethodBodySize() {
        return response.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        response.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {

        try {
            SaslServer saslServer = connectionHandler.getSaslServer();
            if (saslServer != null) {
                byte[] challenge = saslServer.evaluateResponse(response.getBytes());
                if (saslServer.isComplete()) {
                    ctx.writeAndFlush(new ConnectionTune(256, 65535, 0));
                } else {
                    ctx.writeAndFlush(new ConnectionSecure(getChannel(), LongString.parse(challenge)));
                }
            } else {
                throw new SaslException("Sasl server haven't been set during connection start");
            }
        } catch (SaslException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exception occurred while authenticating incoming connection ", e);
            }
            String replyText = "Authentication Failed";
            ctx.writeAndFlush(new ConnectionClose(403, ShortString.parseString(replyText), 10, 21));
        }

    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            LongString response = LongString.parse(buf);
            return new ConnectionSecureOk(channel, response);
        };
    }

}
