/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.broker.core.security.sasl;

import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslServerFactory;

/**
 * Class which is required to build Sasl server
 */
public interface SaslServerBuilder {


    /**
     * Provides list of mechanisms supported by Server
     * @return the mechanism's name. e
     * client.
     */
    String getMechanismName();

    /**
     * This is used to handle data to authentication module
     * @return the callback handler .
     */
    CallbackHandler getCallbackHandler();

    /**
     * Get the properties that must be passed in to the Sasl.createSaslServer.
     * @return the properties
     */
    Map<String, ?> getProperties();

    /**
     * Get the class that is the server factory {@link SaslServerFactory} for the JCA registration.
     * @return null if no JCA registration is required, otherwise return the class
     * that will be used in JCA registration
     */
    Class<? extends SaslServerFactory> getServerFactoryClass();
}
