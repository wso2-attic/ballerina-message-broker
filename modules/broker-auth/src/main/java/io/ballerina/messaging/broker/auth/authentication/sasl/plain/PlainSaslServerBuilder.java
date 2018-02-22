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
package io.ballerina.messaging.broker.auth.authentication.sasl.plain;

import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
import io.ballerina.messaging.broker.auth.authentication.Authenticator;
import io.ballerina.messaging.broker.auth.authentication.sasl.SaslServerBuilder;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslServerFactory;

/**
 * Class implements {@link SaslServerBuilder} to build the PLAIN SASL provider mechanism.
 */
public class PlainSaslServerBuilder implements SaslServerBuilder {

    private final Map<String, Object> properties = new HashMap<>();

    public PlainSaslServerBuilder(Authenticator authenticator) {
        properties.put(BrokerAuthConstants.PROPERTY_AUTHENTICATOR_INSTANCE, authenticator);
    }

    public String getMechanismName() {
        return PlainSaslServer.PLAIN_MECHANISM;
    }

    @Override
    public CallbackHandler getCallbackHandler() {
        return null;
    }

    @Override
    public Map<String, ?> getProperties() {
        return properties;
    }

    @Override
    public Class<? extends SaslServerFactory> getServerFactoryClass() {
        return PlainSaslServerFactory.class;
    }
}
