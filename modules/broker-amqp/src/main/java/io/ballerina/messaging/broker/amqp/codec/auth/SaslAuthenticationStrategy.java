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
package io.ballerina.messaging.broker.amqp.codec.auth;

import io.ballerina.messaging.broker.amqp.codec.frames.ConnectionSecure;
import io.ballerina.messaging.broker.amqp.codec.frames.ConnectionTune;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.auth.AuthManager;
import io.ballerina.messaging.broker.auth.UsernamePrincipal;
import io.ballerina.messaging.broker.common.data.types.LongString;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.BrokerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import javax.security.auth.Subject;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

/**
 * Sasl based Authentication strategy which implements @{@link AuthenticationStrategy} to support sasl mechanisms based
 * authentication to broker client connections.
 */
public class SaslAuthenticationStrategy implements AuthenticationStrategy {

    private AuthManager authManager;
    private BrokerFactory brokerFactory;

    public static final String SASL_SERVER_ATTRIBUTE = "broker.sasl.server";

    SaslAuthenticationStrategy(AuthManager authManager, BrokerFactory brokerFactory) {
        this.authManager = authManager;
        this.brokerFactory = brokerFactory;
    }

    @Override
    public void handleChallengeResponse(int channel,
                                        ChannelHandlerContext ctx,
                                        AmqpConnectionHandler connectionHandler,
                                        LongString response) throws BrokerException {
        Attribute<SaslServer> saslServerAttribute = ctx.channel().attr(AttributeKey.valueOf(SASL_SERVER_ATTRIBUTE));
        SaslServer saslServer;
        if (saslServerAttribute != null && (saslServer = saslServerAttribute.get()) != null) {
            byte[] challenge = evaluateResponse(response, saslServer);
            if (saslServer.isComplete()) {
                Subject subject = UsernamePrincipal.createSubject(saslServer.getAuthorizationID());
                connectionHandler.attachBroker(brokerFactory.getBroker(subject));
                ctx.writeAndFlush(new ConnectionTune(256, 65535, 0));
                ctx.channel().attr(AttributeKey.valueOf(SASL_SERVER_ATTRIBUTE)).set(null);
            } else {
                ctx.writeAndFlush(new ConnectionSecure(channel, LongString.parse(challenge)));
            }
        } else {
            throw new BrokerException("Sasl server hasn't been set during connection start");
        }
    }

    private byte[] evaluateResponse(LongString response, SaslServer saslServer) throws BrokerException {
        try {
            return saslServer.evaluateResponse(response.getBytes());
        } catch (SaslException e) {
            throw new BrokerException("Exception occurred while handling authentication with Sasl", e);
        }
    }

    @Override
    public void handle(int channel, ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler,
                       ShortString mechanism, LongString response) throws BrokerException {
        try {
            SaslServer saslServer = authManager
                    .createSaslServer(connectionHandler.getConfiguration().getHostName(), mechanism.toString());
            byte[] challenge = saslServer.evaluateResponse(response.getBytes());
            if (saslServer.isComplete()) {
                Subject subject = UsernamePrincipal.createSubject(saslServer.getAuthorizationID());
                connectionHandler.attachBroker(brokerFactory.getBroker(subject));
                ctx.writeAndFlush(new ConnectionTune(256, 65535, 0));
            } else {
                ctx.channel().attr(AttributeKey.valueOf(SASL_SERVER_ATTRIBUTE)).set(saslServer);
                ctx.writeAndFlush(new ConnectionSecure(channel, LongString.parse(challenge)));
            }
        } catch (SaslException e) {
            throw new BrokerException("Exception occurred while handling authentication with Sasl", e);
        }
    }
}
