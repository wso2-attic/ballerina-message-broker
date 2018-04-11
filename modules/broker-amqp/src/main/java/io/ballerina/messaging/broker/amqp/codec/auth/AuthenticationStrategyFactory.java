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

import io.ballerina.messaging.broker.auth.AuthManager;
import io.ballerina.messaging.broker.core.BrokerFactory;

import java.util.Objects;

/**
 * Factory Class for provide @{@link AuthenticationStrategy} based on given @{@link AuthManager}.
 */
public class AuthenticationStrategyFactory {

    private AuthenticationStrategyFactory() {}

    public static AuthenticationStrategy getStrategy(AuthManager authManager, BrokerFactory brokerFactory) {
        if (Objects.nonNull(authManager) && authManager.isAuthenticationEnabled()) {
            return new SaslAuthenticationStrategy(authManager, brokerFactory);
        } else {
            return new NoAuthenticationStrategy(brokerFactory);
        }

    }
}
