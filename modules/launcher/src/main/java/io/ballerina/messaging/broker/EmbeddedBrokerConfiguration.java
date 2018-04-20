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

package io.ballerina.messaging.broker;

import io.ballerina.messaging.broker.auth.authentication.Authenticator;
import io.ballerina.messaging.broker.auth.authorization.DiscretionaryAccessController;
import io.ballerina.messaging.broker.auth.authorization.MandatoryAccessController;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.core.BrokerException;

import java.util.HashMap;
import java.util.Objects;
import javax.sql.DataSource;

/**
 * This class used to build configurations required for embedded broker.
 */
public class EmbeddedBrokerConfiguration {

    private boolean inMemoryMode = false;
    private String port = "5672";
    private String sslPort = "8672";
    private boolean sslEnabled = false;
    private String keyStore = null;
    private String keyStorePassword = null;
    private String trustStore = null;
    private String trustStorePassword = null;
    private String driver = "org.h2.Driver";
    private String url = "jdbc:h2:mem:MB_DB;INIT=RUNSCRIPT FROM 'classpath:dbscripts/h2-mb.sql';DB_CLOSE_DELAY=-1";
    private String username = null;
    private String password = null;
    private DataSource dataSource = null;
    private boolean authenticationEnabled = false;
    private Authenticator authenticator = null;
    private HashMap<String, Object> authenticatorProperties = new HashMap<>();
    private boolean authorizationEnabled = false;
    private MandatoryAccessController mandatoryAccessController = null;
    private DiscretionaryAccessController discretionaryAccessController = null;
    private UserStore userStore = null;
    private int authorizationCacheTimeout = 15;
    private int authorizationCacheSize = 5000;

    /**
     * Set broker run non persistent mode.
     *
     * @param inMemoryMode true to enable and default is false
     * @return builder object
     */
    public EmbeddedBrokerConfiguration setInMemoryMode(boolean inMemoryMode) {
        this.inMemoryMode = inMemoryMode;
        return this;
    }

    /**
     * Set AMQP listener port.
     *
     * @param port amqp port
     * @return builder object
     */
    public EmbeddedBrokerConfiguration setPort(String port) {
        this.port = port;
        return this;
    }

    /**
     * Set AMQP SSL listener port.
     *
     * @param sslPort amqp ssl port
     * @return builder object
     */
    public EmbeddedBrokerConfiguration setSslPort(String sslPort) {
        this.sslPort = sslPort;
        return this;
    }

    /**
     * Set SSL transport resources.
     *
     * @param keyStore           fully qualified path of jks
     * @param keyStorePassword   jks password
     * @param trustStore         fully qualified path of trust store
     * @param trustStorePassword trust store password
     * @return builder object
     * @throws BrokerException any of parameter set to null
     */
    public EmbeddedBrokerConfiguration setSslResources(String keyStore,
                                                       String keyStorePassword,
                                                       String trustStore,
                                                       String trustStorePassword) throws BrokerException {

        if (Objects.isNull(keyStore) || Objects.isNull(keyStorePassword) || Objects.isNull(trustStore)
                || Objects.isNull(trustStorePassword)) {
            throw new BrokerException("Either keystore location or keystore password or truststore location or "
                    + "truststore password is null.");
        }
        this.sslEnabled = true;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
        return this;
    }

    /**
     * Setup default Hikari datasource with given parameters.
     *
     * @param driver   driver name
     * @param url      connection url
     * @param username database username
     * @param password database password
     * @return builder object
     */
    public EmbeddedBrokerConfiguration setDefaultDatasource(String driver,
                                                            String url,
                                                            String username,
                                                            String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * Set external datasource to connect with configured database.
     *
     * @param datasource an implementation of {@link DataSource}
     * @return builder object
     */
    public EmbeddedBrokerConfiguration setExternalDatasource(DataSource datasource) {
        this.dataSource = datasource;
        return this;
    }

    /**
     * Set custom implementation for broker authentication.
     *
     * @param authenticator an implementation of {@link Authenticator}
     * @return builder object
     */
    public EmbeddedBrokerConfiguration setAuthentication(Authenticator authenticator) {
        this.authenticationEnabled = true;
        this.authenticator = authenticator;
        return this;
    }

    /**
     * Set additional properties required for custom authenticator.
     *
     * @param authenticatorProperties property map consist of name and object
     * @return builder object
     */
    public EmbeddedBrokerConfiguration setAuthenticatorProperties(HashMap<String, Object> authenticatorProperties) {
        this.authenticatorProperties = authenticatorProperties;
        return this;
    }

    /**
     * Set custom implementation for broker authorization.
     *
     * @param mandatoryAccessController     an implementation of {@link MandatoryAccessController}
     * @param discretionaryAccessController an implementation of {@link DiscretionaryAccessController}
     * @param userStore                     an implementation of {@link UserStore}
     * @param authorizationCacheTimeout     cache timeout in minutes
     * @param authorizationCacheSize        maximum cache records
     * @return builder object
     */
    public EmbeddedBrokerConfiguration setAuthorization(MandatoryAccessController mandatoryAccessController,
                                                        DiscretionaryAccessController discretionaryAccessController,
                                                        UserStore userStore,
                                                        int authorizationCacheTimeout,
                                                        int authorizationCacheSize) {
        this.authorizationEnabled = true;
        this.mandatoryAccessController = mandatoryAccessController;
        this.discretionaryAccessController = discretionaryAccessController;
        this.userStore = userStore;
        this.authorizationCacheTimeout = authorizationCacheTimeout;
        this.authorizationCacheSize = authorizationCacheSize;
        return this;
    }

    public boolean isInMemoryMode() {
        return inMemoryMode;
    }

    public String getPort() {
        return port;
    }

    public String getSslPort() {
        return sslPort;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public HashMap<String, Object> getAuthenticatorProperties() {
        return authenticatorProperties;
    }

    public boolean isAuthorizationEnabled() {
        return authorizationEnabled;
    }

    public MandatoryAccessController getMandatoryAccessController() {
        return mandatoryAccessController;
    }

    public DiscretionaryAccessController getDiscretionaryAccessController() {
        return discretionaryAccessController;
    }

    public UserStore getUserStore() {
        return userStore;
    }

    public int getAuthorizationCacheTimeout() {
        return authorizationCacheTimeout;
    }

    public int getAuthorizationCacheSize() {
        return authorizationCacheSize;
    }
}
