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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.ballerina.messaging.broker.amqp.AmqpServerConfiguration;
import io.ballerina.messaging.broker.amqp.Server;
import io.ballerina.messaging.broker.auth.AuthManager;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authentication.Authenticator;
import io.ballerina.messaging.broker.auth.authorization.DiscretionaryAccessController;
import io.ballerina.messaging.broker.auth.authorization.MandatoryAccessController;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerCommonConfiguration;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.BrokerImpl;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;

/**
 * This class provides APIs to start broker programmatically.
 */
public class EmbeddedBroker {

    private BrokerImpl broker = null;
    private Server amqpServer = null;
    private AuthManager authManager = null;
    private boolean inMemoryMode;
    private String port = "5672";
    private String sslPort = "8672";
    private boolean sslEnabled;
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

    public EmbeddedBroker() {
        System.setProperty("message.broker.home", System.getProperty("user.dir"));
    }

    /**
     * Start broker with configuration that has been set on the instance
     *
     * @throws Exception if issue occurred while starting
     */
    public void start() throws Exception {
        StartupContext startupContext = new StartupContext();
        EmbeddedBroker.ConfigProvider configProvider = new EmbeddedBroker.ConfigProvider();

        prepareCommonConfig(startupContext, configProvider);
        prepareCoreConfig(configProvider);
        prepareServerConfig(configProvider);
        prepareAuthConfig(configProvider);
        startupContext.registerService(BrokerConfigProvider.class, configProvider);

        broker = new BrokerImpl(startupContext);
        broker.startMessageDelivery();
        authManager = new AuthManager(startupContext);
        authManager.start();
        amqpServer = new Server(startupContext);
        amqpServer.start();
    }

    /**
     * Stop broker instance
     *
     * @throws Exception if issue occurred while stopping
     */
    public void stop() throws Exception {
        amqpServer.stop();
        amqpServer.awaitServerClose();
        authManager.stop();
        broker.shutdown();
    }

    /**
     * Run broker in non persistent mode
     *
     * @param inMemoryMode true to enable and default is false
     */
    public void setInMemoryMode(boolean inMemoryMode) {
        this.inMemoryMode = inMemoryMode;
    }

    /**
     * Set AMQP port
     *
     * @param port amqp port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Set AMQP SSL port
     *
     * @param sslPort amqp ssl port
     */
    public void setSslPort(String sslPort) {
        this.sslPort = sslPort;
    }

    /**
     * Enable AMQP SSL transport
     *
     * @param sslEnabled true to enable and default is false
     */
    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    /**
     * Java key store location to use in SSL transport
     *
     * @param keyStore fully qualified path of jks
     */
    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * Password of the java key store
     *
     * @param keyStorePassword jks password
     */
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * Java trust store location to use in  SSL transport
     *
     * @param trustStore fully qualified path of trust store
     */
    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    /**
     * Password of java trust store
     *
     * @param trustStorePassword trust store password
     */
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * Database driver name to build the default datasource
     *
     * @param driver driver name
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * Database connection url to build the default datasource
     *
     * @param url connection url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Database username to build the default datasource
     *
     * @param username database username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Database password to build the default datasource
     *
     * @param password database password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * External datasource to connect with configured database
     *
     * @param dataSource an implementation of {@link DataSource}
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Authentication enable in the broker instance
     *
     * @param authenticationEnabled true to enable and default is false
     */
    public void setAuthenticationEnabled(boolean authenticationEnabled) {
        this.authenticationEnabled = authenticationEnabled;
    }

    /**
     * Custom authenticator for broker authentication
     *
     * @param authenticator an implementation of {@link Authenticator}
     */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Add additional properties required for custom authenticator
     *
     * @param name     property name
     * @param property property object
     */
    public void putAuthenticatorProperty(String name, Object property) {
        authenticatorProperties.put(name, property);
    }

    /**
     * Authorization enable in the broker instance
     *
     * @param authorizationEnabled true to enable and default is false
     */
    public void setAuthorizationEnabled(boolean authorizationEnabled) {
        this.authorizationEnabled = authorizationEnabled;
    }

    /**
     * Custom mandatory access controller to handle static resource permission
     *
     * @param mandatoryAccessController an implementation of {@link MandatoryAccessController}
     */
    public void setMandatoryAccessController(MandatoryAccessController mandatoryAccessController) {
        this.mandatoryAccessController = mandatoryAccessController;
    }

    /**
     * Custom discretionary access controller to handle dynamic resource permission
     *
     * @param discretionaryAccessController an implementation of {@link DiscretionaryAccessController}
     */
    public void setDiscretionaryAccessController(DiscretionaryAccessController discretionaryAccessController) {
        this.discretionaryAccessController = discretionaryAccessController;
    }

    /**
     * Custom user store to retrieve user data
     *
     * @param userStore an implementation of {@link UserStore}
     */
    public void setUserStore(UserStore userStore) {
        this.userStore = userStore;
    }

    /**
     * Authorization cache timeout
     *
     * @param authorizationCacheTimeout timeout in minutes
     */
    public void setAuthorizationCacheTimeout(int authorizationCacheTimeout) {
        this.authorizationCacheTimeout = authorizationCacheTimeout;
    }

    /**
     * Cache size to hold user id
     *
     * @param authorizationCacheSize maximum cache records
     */
    public void setAuthorizationCacheSize(int authorizationCacheSize) {
        this.authorizationCacheSize = authorizationCacheSize;
    }

    /**
     * Setup datasource with default parameters
     *
     * @param startupContext to register configured {@link DataSource} object
     */
    private void prepareDatasource(StartupContext startupContext) {
        BrokerCommonConfiguration.DataSourceConfiguration dataSourceConfiguration =
                new BrokerCommonConfiguration.DataSourceConfiguration();
        dataSourceConfiguration.setDatabaseDriver(driver);
        dataSourceConfiguration.setUrl(url);
        if (Objects.nonNull(username)) {
            dataSourceConfiguration.setUser(username);
        }
        if (Objects.nonNull(password)) {
            dataSourceConfiguration.setPassword(password);
        }
        DataSource dataSource = getDataSource(dataSourceConfiguration);
        startupContext.registerService(DataSource.class, dataSource);
    }

    /**
     * Use Hikari as default datasource implementation and configure with parameters given
     * in {@link BrokerCommonConfiguration.DataSourceConfiguration}
     * If user has provide an external datasource, then ignore the default datasource.
     *
     * @param dataSourceConfig configuration data set in this object
     * @return {@link DataSource} object configure with parameters
     */
    private DataSource getDataSource(BrokerCommonConfiguration.DataSourceConfiguration dataSourceConfig) {
        if (Objects.isNull(dataSource)) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dataSourceConfig.getUrl());
            config.setUsername(dataSourceConfig.getUser());
            config.setPassword(dataSourceConfig.getPassword());
            config.setAutoCommit(false);
            return new HikariDataSource(config);
        } else {
            return dataSource;
        }
    }

    /**
     * Setup broker core configuration
     *
     * @param configProvider to register {@link BrokerCoreConfiguration} object
     */
    private void prepareCoreConfig(ConfigProvider configProvider) {
        BrokerCoreConfiguration brokerCoreConfiguration = new BrokerCoreConfiguration();
        configProvider.registerConfigurationObject(BrokerCoreConfiguration.NAMESPACE, brokerCoreConfiguration);
    }

    private void prepareCommonConfig(StartupContext startupContext, ConfigProvider configProvider) {
        BrokerCommonConfiguration commonConfig = new BrokerCommonConfiguration();
        if (inMemoryMode) {
            commonConfig.setEnableInMemoryMode(true);
        } else {
            prepareDatasource(startupContext);
        }
        configProvider.registerConfigurationObject(BrokerCommonConfiguration.NAMESPACE, commonConfig);
    }

    /**
     * Setup amqp server configuration such as port and ssl configuration
     *
     * @param configProvider to register {@link AmqpServerConfiguration} object
     */
    private void prepareServerConfig(ConfigProvider configProvider) throws BrokerException {
        AmqpServerConfiguration serverConfiguration = new AmqpServerConfiguration();
        serverConfiguration.getPlain().setPort(port);
        if (sslEnabled) {
            if (Objects.isNull(keyStore) || Objects.isNull(keyStorePassword) || Objects.isNull(trustStore)
                    || Objects.isNull(trustStorePassword)) {
                throw new BrokerException("Either keystore location or keystore password or truststore location or "
                        + "truststore password is null.");
            } else {
                serverConfiguration.getSsl().setEnabled(sslEnabled);
                serverConfiguration.getSsl().setPort(sslPort);
                serverConfiguration.getSsl().getKeyStore().setLocation(keyStore);
                serverConfiguration.getSsl().getKeyStore().setPassword(keyStorePassword);
                serverConfiguration.getSsl().getTrustStore().setLocation(trustStore);
                serverConfiguration.getSsl().getTrustStore().setPassword(trustStorePassword);
            }
        }
        configProvider.registerConfigurationObject(AmqpServerConfiguration.NAMESPACE, serverConfiguration);
    }

    /**
     * Setup authentication and authorization configuration in the broker
     *
     * @param configProvider to register {@link BrokerAuthConfiguration} object
     */
    private void prepareAuthConfig(ConfigProvider configProvider) throws BrokerException {
        BrokerAuthConfiguration brokerAuthConfiguration = new BrokerAuthConfiguration();
        BrokerAuthConfiguration.AuthenticationConfiguration authenticationConfiguration =
                new BrokerAuthConfiguration.AuthenticationConfiguration();
        authenticationConfiguration.setEnabled(authenticationEnabled);
        BrokerAuthConfiguration.AuthorizationConfiguration authorizationConfiguration =
                new BrokerAuthConfiguration.AuthorizationConfiguration();
        authorizationConfiguration.setEnabled(authorizationEnabled);

        if (authenticationEnabled) {
            BrokerAuthConfiguration.AuthenticatorConfiguration authenticatorConfiguration =
                    new BrokerAuthConfiguration.AuthenticatorConfiguration();
            //set Jaas authenticator
            setAuthenticationConfiguration(authenticationConfiguration,
                    authenticatorConfiguration);

            if (authorizationEnabled) {
                //set MAC configuration
                setMacConfiguration(authorizationConfiguration);
                //set DAC configuration
                setDacConfiguration(authorizationConfiguration);
                //set UserStore configuration
                setUserStoreConfiguration(authorizationConfiguration);
                //set Cache configuration
                setCacheConfiguration(authorizationConfiguration);
            }
        }

        brokerAuthConfiguration.setAuthentication(authenticationConfiguration);
        brokerAuthConfiguration.setAuthorization(authorizationConfiguration);
        configProvider.registerConfigurationObject(BrokerAuthConfiguration.NAMESPACE, brokerAuthConfiguration);
    }

    /**
     * setup authorization cache configuration
     *
     * @param authorizationConfiguration to set timeout and size parameter
     */
    private void setCacheConfiguration(BrokerAuthConfiguration.AuthorizationConfiguration
                                               authorizationConfiguration) {
        BrokerAuthConfiguration.CacheConfiguration cacheConfiguration =
                new BrokerAuthConfiguration.CacheConfiguration();
        cacheConfiguration.setSize(authorizationCacheSize);
        cacheConfiguration.setTimeout(authorizationCacheTimeout);
        authorizationConfiguration.setCache(cacheConfiguration);
    }

    /**
     * Setup default userstore or custom userstore implementation
     *
     * @param authorizationConfiguration to set userstore
     */
    private void setUserStoreConfiguration(BrokerAuthConfiguration.AuthorizationConfiguration
                                                   authorizationConfiguration) throws BrokerException {
        BrokerAuthConfiguration.UserStoreConfiguration userStoreConfiguration =
                new BrokerAuthConfiguration.UserStoreConfiguration();
        if (Objects.isNull(userStore)) {
            throw new BrokerException("UserStore implementation is null.");
        } else {
            userStoreConfiguration.setClassName(userStore.getClass().getCanonicalName());
        }
        authorizationConfiguration.setUserStore(userStoreConfiguration);
    }

    /**
     * Setup default dac handler or custom dac handler
     *
     * @param authorizationConfiguration to set dac handler
     */
    private void setDacConfiguration(BrokerAuthConfiguration.AuthorizationConfiguration authorizationConfiguration)
            throws BrokerException {
        BrokerAuthConfiguration.DacConfigurations dacConfigurations =
                new BrokerAuthConfiguration.DacConfigurations();
        if (Objects.isNull(discretionaryAccessController)) {
            throw new BrokerException("DiscretionaryAccessController implementation is null.");
        } else {
            dacConfigurations.setClassName(discretionaryAccessController.getClass().getCanonicalName());
        }
        authorizationConfiguration.setDiscretionaryAccessController(dacConfigurations);
    }

    /**
     * Setup default mac handler or custom mac handler
     *
     * @param authorizationConfiguration to set mac handler
     */
    private void setMacConfiguration(BrokerAuthConfiguration.AuthorizationConfiguration authorizationConfiguration)
            throws BrokerException {
        BrokerAuthConfiguration.MacConfigurations macConfigurations =
                new BrokerAuthConfiguration.MacConfigurations();
        if (Objects.isNull(mandatoryAccessController)) {
            throw new BrokerException("MandatoryAccessController implementation is null.");
        } else {
            macConfigurations.setClassName(mandatoryAccessController.getClass().getCanonicalName());
        }
        authorizationConfiguration.setMandatoryAccessController(macConfigurations);
    }

    /**
     * Setup default authenticator or custom authenticator
     *
     * @param authenticationConfiguration to set authenticator
     * @param authenticatorConfiguration  to set authenticator implementation
     */
    private void setAuthenticationConfiguration(BrokerAuthConfiguration.AuthenticationConfiguration
                                                        authenticationConfiguration,
                                                BrokerAuthConfiguration.AuthenticatorConfiguration
                                                        authenticatorConfiguration) throws BrokerException {
        if (Objects.isNull(authenticator)) {
            throw new BrokerException("Authenticator implementation is null.");
        } else {
            authenticatorConfiguration.setClassName(authenticator.getClass().getCanonicalName());
            authenticatorConfiguration.setProperties(authenticatorProperties);
        }
        authenticationConfiguration.setAuthenticator(authenticatorConfiguration);
    }

    /**
     * Register broker configuration
     */
    static class ConfigProvider implements BrokerConfigProvider {

        private Map<String, Object> configMap = new HashMap<>();

        @Override
        public <T> T getConfigurationObject(String namespace, Class<T> configurationClass) {
            return configurationClass.cast(configMap.get(namespace));
        }

        void registerConfigurationObject(String namespace, Object configObject) {
            configMap.put(namespace, configObject);
        }
    }
}
