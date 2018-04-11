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

import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.data.types.LongString;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.BrokerException;
import io.netty.channel.ChannelHandlerContext;

/**
 * Authentication Strategy handles the authentication of the broker client connection.
 */
public interface AuthenticationStrategy {

    void handle(int channel, ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler,
                ShortString mechanism, LongString response) throws BrokerException;

    void handleChallengeResponse(int channel,
                                 ChannelHandlerContext ctx,
                                 AmqpConnectionHandler connectionHandler,
                                 LongString response) throws BrokerException;
}
