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
package io.ballerina.messaging.broker.auth;

import io.ballerina.messaging.broker.auth.authentication.authenticator.impl.DefaultAuthenticator;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents authentication configuration for broker.
 */
public class BrokerAuthConfiguration {

    /**
     * Namespace used in the config file.
     */
    public static final String NAMESPACE = "ballerina.broker.auth";

    private AuthenticationConfiguration authentication = new AuthenticationConfiguration();

    public AuthenticationConfiguration getAuthentication() {
        return authentication;
    }

    public void setAuthentication(AuthenticationConfiguration authentication) {
        this.authentication = authentication;
    }

    /**
     * Represents authentication configuration for broker
     */
    public static class AuthenticationConfiguration {

        private boolean enabled = true;

        private AuthenticatorConfiguration authenticator = new AuthenticatorConfiguration();

        public AuthenticatorConfiguration getAuthenticator() {
            return authenticator;
        }

        public void setAuthenticator(AuthenticatorConfiguration authenticator) {
            this.authenticator = authenticator;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Represents authenticator configuration for broker
     */
    public static class AuthenticatorConfiguration {

        private String className = DefaultAuthenticator.class.getCanonicalName();

        private Map<String, Object> properties = new HashMap<>();

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }
    }
}


