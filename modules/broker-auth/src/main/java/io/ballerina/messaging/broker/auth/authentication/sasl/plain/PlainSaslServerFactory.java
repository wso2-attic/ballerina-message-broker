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

import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;

/**
 * SASL server factory for {@link PlainSaslServer}  which will be registered using
 * {@link io.ballerina.messaging.broker.auth.authentication.sasl.SaslServerBuilder}.
 */
public class PlainSaslServerFactory implements SaslServerFactory {

    @Override
    public SaslServer createSaslServer(String mechanism, String protocol, String serverName, Map<String, ?> props,
            CallbackHandler cbh) throws SaslException {
        Authenticator authenticator = (Authenticator) props.get(BrokerAuthConstants.PROPERTY_AUTHENTICATOR_INSTANCE);
        return (PlainSaslServer.PLAIN_MECHANISM.equals(mechanism)) ?
                new PlainSaslServer(authenticator) :
                null;
    }

    @Override
    public String[] getMechanismNames(Map<String, ?> props) {
        if (props != null && "true".equals(props.get(Sasl.POLICY_NOPLAINTEXT))) {
            return new String[] {};
        } else {
            return new String[] { PlainSaslServer.PLAIN_MECHANISM };
        }
    }
}

