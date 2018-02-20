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
import io.ballerina.messaging.broker.amqp.codec.auth.AuthenticationStrategy;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.LongString;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.BrokerException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP connection.start frame.
 */
public class ConnectionStartOk extends MethodFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionStartOk.class);

    private static final short CLASS_ID = 10;

    private static final short METHOD_ID = 11;

    private final FieldTable clientProperties;
    private final ShortString mechanism;
    private final ShortString locale;
    private final LongString response;
    private final AuthenticationStrategy authenticationStrategy;

    public ConnectionStartOk(int channel, FieldTable clientProperties, ShortString mechanisms, ShortString locale,
                             LongString response, AuthenticationStrategy authenticationStrategy) {
        super(channel, CLASS_ID, METHOD_ID);
        this.clientProperties = clientProperties;
        this.mechanism = mechanisms;
        this.locale = locale;
        this.response = response;
        this.authenticationStrategy = authenticationStrategy;
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
            try {
                authenticationStrategy.handle(getChannel(), ctx, connectionHandler, mechanism, response);
            } catch (BrokerException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception occurred while authenticating incoming connection. ", e);
                }
                ctx.writeAndFlush(new ConnectionClose(ConnectionException.NOT_ALLOWED,
                                                      ShortString.parseString(e.getMessage()),
                                                      CLASS_ID, METHOD_ID));
            }
        });
    }


    public static AmqMethodBodyFactory getFactory(AuthenticationStrategy authenticationStrategy) {
        return (buf, channel, size) -> {
            FieldTable clientProperties = FieldTable.parse(buf);
            ShortString mechanism = ShortString.parse(buf);
            LongString response = LongString.parse(buf);
            ShortString locale = ShortString.parse(buf);
            return new ConnectionStartOk(channel, clientProperties, mechanism, locale, response,
                                         authenticationStrategy);
        };
    }
}
