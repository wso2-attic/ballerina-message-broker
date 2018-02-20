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

import io.ballerina.messaging.broker.amqp.codec.BlockingTask;
import io.ballerina.messaging.broker.amqp.codec.ConnectionException;
import io.ballerina.messaging.broker.amqp.codec.auth.SaslAuthenticationStrategy;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.data.types.LongString;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

/**
 * AMQP frame for connection.secure.ok
 * Parameter Summary:
 *     1. response (LongString) - security response data
 */
public class ConnectionSecureOk extends MethodFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionSecureOk.class);

    private static final short CLASS_ID = 10;

    private static final short METHOD_ID = 21;

    private final LongString response;

    public ConnectionSecureOk(int channel, LongString response) {
        super(channel, CLASS_ID, METHOD_ID);
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
        ctx.fireChannelRead((BlockingTask) () -> {
            try {
                Attribute<SaslServer> saslServerAttribute =
                        ctx.channel().attr(AttributeKey.valueOf(SaslAuthenticationStrategy.SASL_SERVER_ATTRIBUTE));
                SaslServer saslServer;
                if (saslServerAttribute != null && (saslServer = saslServerAttribute.get()) != null) {
                    byte[] challenge = saslServer.evaluateResponse(response.getBytes());
                    if (saslServer.isComplete()) {
                        ctx.writeAndFlush(new ConnectionTune(256, 65535, 0));
                    } else {
                        ctx.channel().attr(AttributeKey.valueOf(SaslAuthenticationStrategy.SASL_SERVER_ATTRIBUTE))
                            .set(null);
                        ctx.writeAndFlush(new ConnectionSecure(getChannel(), LongString.parse(challenge)));
                    }
                } else {
                    throw new SaslException("Sasl server hasn't been set during connection start");
                }
            } catch (SaslException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception occurred while authenticating incoming connection ", e);
                }
                ctx.writeAndFlush(new ConnectionClose(ConnectionException.NOT_ALLOWED,
                                                      ShortString.parseString(e.getMessage()),
                                                      CLASS_ID, METHOD_ID));
            }
        });
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            LongString response = LongString.parse(buf);
            return new ConnectionSecureOk(channel, response);
        };
    }

}
