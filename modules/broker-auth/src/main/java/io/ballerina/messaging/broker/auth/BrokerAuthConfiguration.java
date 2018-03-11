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

import io.ballerina.messaging.broker.auth.authentication.authenticator.DefaultAuthenticator;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.RdbmsAuthorizer;
import io.ballerina.messaging.broker.auth.authorization.provider.FileBasedUserStore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents authentication configuration for broker.
 */
public class BrokerAuthConfiguration {

    /**
     * Namespace used in the config file.
     */
    public static final String NAMESPACE = "ballerina.broker.auth";

    private AuthenticationConfiguration authentication = new AuthenticationConfiguration();

    private AuthorizationConfiguration authorization = new AuthorizationConfiguration();

    public AuthenticationConfiguration getAuthentication() {
        return authentication;
    }

    public void setAuthentication(AuthenticationConfiguration authentication) {
        this.authentication = authentication;
    }

    public AuthorizationConfiguration getAuthorization() {
        return authorization;
    }

    public void setAuthorization(AuthorizationConfiguration authorization) {
        this.authorization = authorization;
    }

    /**
     * Represents authentication configuration for broker.
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
     * Represents authorization configuration for broker.
     */
    public static class AuthorizationConfiguration {

        private boolean enabled = true;

        private AuthorizerConfiguration authorizer = new AuthorizerConfiguration();

        private CacheConfiguration cache = new CacheConfiguration();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public AuthorizerConfiguration getAuthorizer() {
            return authorizer;
        }

        public void setAuthorizer(AuthorizerConfiguration authorizer) {
            this.authorizer = authorizer;
        }

        public CacheConfiguration getCache() {
            return cache;
        }

        public void setCache(CacheConfiguration cache) {
            this.cache = cache;
        }
    }

    /**
     * Represents authenticator configuration for broker.
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

    /**
     * Represents authorizer configuration for broker.
     */
    public static class AuthorizerConfiguration {

        private String className = RdbmsAuthorizer.class.getCanonicalName();

        private Map<String, String> properties = new HashMap<>();

        public AuthorizerConfiguration() {
            properties.put(RdbmsAuthorizer.USER_STORE_CLASS_PROPERTY_NAME,
                           FileBasedUserStore.class.getCanonicalName());
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Map<String, String> getProperties() {
            if (Objects.isNull(properties)) {
                return Collections.emptyMap();
            } else {
                return properties;
            }
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }
    }

    /**
     * Represents permission cache configuration required for authorization.
     */
    public static class CacheConfiguration {

        /**
         * Cache timeout in minutes.
         */
        private int timeout = 15;
        /**
         * Maximum cache size.
         */
        private int size = 5000;

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
