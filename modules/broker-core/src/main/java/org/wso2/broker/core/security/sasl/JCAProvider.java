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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.core.security.util.BrokerSecurityConstants;

import java.security.Provider;
import java.util.Map;

/**
 * {@link Provider} implementation for register AMQ SASL server factories in Java Security .
 */
public class JCAProvider extends Provider {

    private static final Logger log = LoggerFactory.getLogger(SaslServerBuilder.class);
    private static final long serialVersionUID = -1576616958900657930L;

    /**
     * Constructs a provider with the specified name and sasl servers map
     *
     * @param name        the provider name.
     * @param providerMap map of SASLServerProviders
     */
    public JCAProvider(String name, Map<String, SaslServerBuilder> providerMap) {
        super(name, BrokerSecurityConstants.JCA_PROVIDER_VERSION, BrokerSecurityConstants.JCA_PROVIDER_INFO);
        register(providerMap);
    }

    private void register(Map<String, SaslServerBuilder> providerMap) {

        for (Map.Entry<String, SaslServerBuilder> saslServerBuilderEntry : providerMap.entrySet()) {
            if (saslServerBuilderEntry.getValue().getServerFactoryClass() != null) {
                put(BrokerSecurityConstants.SASL_SERVER_FACTORY_PREFIX + saslServerBuilderEntry.getKey(),
                        saslServerBuilderEntry.getValue().getServerFactoryClass().getName());
            } else {
                log.error("Cannot find server factory for security mechanism : " + saslServerBuilderEntry.getKey());
            }
        }
    }
}
