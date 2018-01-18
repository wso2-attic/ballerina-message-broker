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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqConstant;
import org.wso2.broker.amqp.codec.BlockingTask;
import org.wso2.broker.amqp.codec.handlers.AmqpConnectionHandler;
import org.wso2.broker.common.data.types.FieldTable;
import org.wso2.broker.common.data.types.LongString;
import org.wso2.broker.common.data.types.ShortString;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.security.authentication.sasl.SaslServerBuilder;

import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

/**
 * AMQP connection.start frame.
 */
public class ConnectionStartOk extends MethodFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionStartOk.class);
    private final FieldTable clientProperties;
    private final ShortString mechanism;
    private final ShortString locale;
    private final LongString response;

    public ConnectionStartOk(int channel, FieldTable clientProperties, ShortString mechanisms, ShortString locale,
            LongString response) {
        super(channel, (short) 10, (short) 11);
        this.clientProperties = clientProperties;
        this.mechanism = mechanisms;
        this.locale = locale;
        this.response = response;
    }

    @Override
    protected long getMethodBodySize() {
        return clientProperties.getSize() + mechanism.getSize() + locale.getSize() + response.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        clientProperties.write(buf);
        mechanism.write(buf);
        locale.write(buf);
        response.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        ctx.fireChannelRead((BlockingTask) () -> {
            Broker broker = connectionHandler.getBroker();
            SaslServer saslServer;
            SaslServerBuilder saslServerBuilder = broker.getAuthenticationManager().getSaslMechanisms()
                    .get(mechanism.toString());
            try {
                if (saslServerBuilder != null) {
                    saslServer = Sasl.createSaslServer(mechanism.toString(), AmqConstant.AMQP_PROTOCOL_IDENTIFIER,
                            connectionHandler.getConfiguration().getPlain().getHostName(),
                            saslServerBuilder.getProperties(), saslServerBuilder.getCallbackHandler());
                    connectionHandler.setSaslServer(saslServer);
                } else {
                    throw new SaslException("Server does not support for mechanism: " + mechanism);
                }
                if (saslServer != null) {
                    byte[] challenge = saslServer.evaluateResponse(response.getBytes());
                    if (saslServer.isComplete()) {
                        ctx.writeAndFlush(new ConnectionTune(256, 65535, 0));
                    } else {
                        ctx.writeAndFlush(new ConnectionSecure(getChannel(), LongString.parse(challenge)));
                    }
                } else {
                    throw new SaslException("Sasl server cannot be found for mechanism: " + mechanism);
                }
            } catch (SaslException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception occurred while authenticating incoming connection ", e);
                }
                String replyText = "Authentication Failed";
                ctx.writeAndFlush(new ConnectionClose(403, ShortString.parseString(replyText), 10, 11));
            }
        });
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            FieldTable clientProperties = FieldTable.parse(buf);
            ShortString mechanism = ShortString.parse(buf);
            LongString response = LongString.parse(buf);
            ShortString locale = ShortString.parse(buf);
            return new ConnectionStartOk(channel, clientProperties, mechanism, locale, response);
        };
    }
}
