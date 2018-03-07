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
package io.ballerina.messaging.broker.auth.authentication.sasl;

import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslServerFactory;

/**
 * Interface for build custom @{@link javax.security.sasl.SaslServer} for register security mechanisms.
 */
public interface SaslServerBuilder {

    /**
     * Provides list of mechanisms supported by server.
     *
     * @return the mechanism's name
     */
    String getMechanismName();

    /**
     * This is used to store and provide security information to authenticator.
     *
     * @return the callback handler
     */
    CallbackHandler getCallbackHandler();

    /**
     * Provides map of properties which needs to be passed in to the Sasl.createSaslServer.
     *
     * @return the properties
     */
    Map<String, ?> getProperties();

    /**
     * Provides server factory {@link SaslServerFactory} for the Java Cryptography Architecture
     * (JCA) registration.
     *
     * @return Null if no JCA registration is required, otherwise return the class that will be used in JCA registration
     */
    Class<? extends SaslServerFactory> getServerFactoryClass();
}
