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
import io.ballerina.messaging.broker.auth.authorization.provider.FileBasedUserStore;
import io.ballerina.messaging.broker.auth.authorization.provider.MemoryDacHandler;
import io.ballerina.messaging.broker.auth.authorization.provider.NoOpMacHandler;

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

        private boolean enabled = false;

        private UserStoreConfiguration userStore = new UserStoreConfiguration();

        private MacConfigurations mandatoryAccessController = new MacConfigurations();

        private DacConfigurations discretionaryAccessController = new DacConfigurations();

        private CacheConfiguration cache = new CacheConfiguration();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public UserStoreConfiguration getUserStore() {
            return userStore;
        }

        public void setUserStore(UserStoreConfiguration userStore) {
            this.userStore = userStore;
        }

        /**
         * Getter for mandatoryAccessController.
         */
        public MacConfigurations getMandatoryAccessController() {
            return mandatoryAccessController;
        }

        public void setMandatoryAccessController(MacConfigurations mandatoryAccessController) {
            this.mandatoryAccessController = mandatoryAccessController;
        }

        /**
         * Getter for discretionaryAccessController.
         */
        public DacConfigurations getDiscretionaryAccessController() {
            return discretionaryAccessController;
        }

        public void setDiscretionaryAccessController(DacConfigurations discretionaryAccessController) {
            this.discretionaryAccessController = discretionaryAccessController;
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

        private LdapConfiguration ldap = new LdapConfiguration();

        private Map<String, Object> properties = new HashMap<>();

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public LdapConfiguration getLdap() {
            return ldap;
        }

        public void setLdap(LdapConfiguration ldap) {
            this.ldap = ldap;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }
    }

    /**
     * Represents userStore configuration for broker.
     */
    public static class UserStoreConfiguration {

        private String className = FileBasedUserStore.class.getCanonicalName();

        private Map<String, String> properties = new HashMap<>();

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
     * Represents userStore configuration for broker.
     */
    public static class MacConfigurations {

        private String className = NoOpMacHandler.class.getCanonicalName();

        private Map<String, String> properties = new HashMap<>();

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
     * Represents userStore configuration for broker.
     */
    public static class DacConfigurations {

        private String className = MemoryDacHandler.class.getCanonicalName();

        private Map<String, String> properties = new HashMap<>();

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

    /**
     * Represents Ldap configuration.
     */
    public static class LdapConfiguration {

        private String hostName = "localhost";

        private String baseDN = "dc=example,dc=com";

        private String usernameSearchFilter = "(uid=?)";

        private LdapPlainConfiguration plain = new LdapPlainConfiguration();

        private LdapSslConfiguration ssl = new LdapSslConfiguration();

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getBaseDN() {
            return baseDN;
        }

        public void setBaseDN(String baseDN) {
            this.baseDN = baseDN;
        }

        public String getUsernameSearchFilter() {
            return usernameSearchFilter;
        }

        public void setUsernameSearchFilter(String usernameSearchFilter) {
            this.usernameSearchFilter = usernameSearchFilter;
        }

        public LdapPlainConfiguration getPlain() {
            return plain;
        }

        public void setPlain(LdapPlainConfiguration plain) {
            this.plain = plain;
        }

        public LdapSslConfiguration getSsl() {
            return ssl;
        }

        public void setSsl(LdapSslConfiguration ssl) {
            this.ssl = ssl;
        }
    }

    /**
     * Represents Ldap configuration for plain connections (ssl disabled).
     */
    public static class LdapPlainConfiguration {

        private int port = 10389;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    /**
     * Represents Ldap ssl configuration.
     */
    public static class LdapSslConfiguration {

        private boolean enabled = false;

        private int port = 10636;

        private String protocol = "TLS";

        private LdapSslTrustStoreConfiguration trustStore = new LdapSslTrustStoreConfiguration();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public LdapSslTrustStoreConfiguration getTrustStore() {
            return trustStore;
        }

        public void setTrustStore(LdapSslTrustStoreConfiguration trustStore) {
            this.trustStore = trustStore;
        }
    }

    /**
     * Represents ssl trust store configuration for Ldap.
     */
    public static class LdapSslTrustStoreConfiguration {

        private String type = "JKS";

        private String location = "resources/security/ldap-truststore.jks";

        private String password = "ballerina";

        private String certType = "SunX509";

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getCertType() {
            return certType;
        }

        public void setCertType(String certType) {
            this.certType = certType;
        }
    }
}
