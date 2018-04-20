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
    private EmbeddedBrokerConfiguration configuration;

    public EmbeddedBroker() {
        System.setProperty("message.broker.home", System.getProperty("user.dir"));
        this.configuration = new EmbeddedBrokerConfiguration();
    }

    public EmbeddedBroker(EmbeddedBrokerConfiguration configuration) {
        System.setProperty("message.broker.home", System.getProperty("user.dir"));
        this.configuration = configuration;
    }

    /**
     * Start broker with configuration that has been set on the instance.
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
     * Stop broker instance.
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
     * Setup broker commond configuration.
     *
     * @param startupContext startup context
     * @param configProvider to register {@link BrokerCoreConfiguration} object
     */
    private void prepareCommonConfig(StartupContext startupContext, ConfigProvider configProvider) {
        BrokerCommonConfiguration commonConfig = new BrokerCommonConfiguration();
        if (configuration.isInMemoryMode()) {
            commonConfig.setEnableInMemoryMode(true);
        } else {
            prepareDatasource(startupContext);
        }
        configProvider.registerConfigurationObject(BrokerCommonConfiguration.NAMESPACE, commonConfig);
    }

    /**
     * Setup broker core configuration.
     *
     * @param configProvider to register {@link BrokerCoreConfiguration} object
     */
    private void prepareCoreConfig(ConfigProvider configProvider) {
        BrokerCoreConfiguration brokerCoreConfiguration = new BrokerCoreConfiguration();
        configProvider.registerConfigurationObject(BrokerCoreConfiguration.NAMESPACE, brokerCoreConfiguration);
    }

    /**
     * Setup amqp server configuration such as port and ssl configuration.
     *
     * @param configProvider to register {@link AmqpServerConfiguration} object
     */
    private void prepareServerConfig(ConfigProvider configProvider) {
        AmqpServerConfiguration serverConfiguration = new AmqpServerConfiguration();
        serverConfiguration.getPlain().setPort(configuration.getPort());
        if (configuration.isSslEnabled()) {
            serverConfiguration.getSsl().setEnabled(configuration.isSslEnabled());
            serverConfiguration.getSsl().setPort(configuration.getSslPort());
            serverConfiguration.getSsl().getKeyStore().setLocation(configuration.getKeyStore());
            serverConfiguration.getSsl().getKeyStore().setPassword(configuration.getKeyStorePassword());
            serverConfiguration.getSsl().getTrustStore().setLocation(configuration.getTrustStore());
            serverConfiguration.getSsl().getTrustStore().setPassword(configuration.getKeyStorePassword());
        }
        configProvider.registerConfigurationObject(AmqpServerConfiguration.NAMESPACE, serverConfiguration);
    }

    /**
     * Setup authentication and authorization configuration in the broker.
     *
     * @param configProvider to register {@link BrokerAuthConfiguration} object
     */
    private void prepareAuthConfig(ConfigProvider configProvider) throws BrokerException {
        BrokerAuthConfiguration brokerAuthConfiguration = new BrokerAuthConfiguration();
        BrokerAuthConfiguration.AuthenticationConfiguration authenticationConfiguration =
                new BrokerAuthConfiguration.AuthenticationConfiguration();
        authenticationConfiguration.setEnabled(configuration.isAuthenticationEnabled());
        BrokerAuthConfiguration.AuthorizationConfiguration authorizationConfiguration =
                new BrokerAuthConfiguration.AuthorizationConfiguration();
        authorizationConfiguration.setEnabled(configuration.isAuthorizationEnabled());

        if (configuration.isAuthenticationEnabled()) {
            BrokerAuthConfiguration.AuthenticatorConfiguration authenticatorConfiguration =
                    new BrokerAuthConfiguration.AuthenticatorConfiguration();
            //set Jaas authenticator
            setAuthenticationConfiguration(authenticationConfiguration,
                    authenticatorConfiguration);

            if (configuration.isAuthorizationEnabled()) {
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
     * Setup datasource with default parameters.
     *
     * @param startupContext to register configured {@link DataSource} object
     */
    private void prepareDatasource(StartupContext startupContext) {
        BrokerCommonConfiguration.DataSourceConfiguration dataSourceConfiguration =
                new BrokerCommonConfiguration.DataSourceConfiguration();
        dataSourceConfiguration.setDatabaseDriver(configuration.getDriver());
        dataSourceConfiguration.setUrl(configuration.getUrl());
        if (Objects.nonNull(configuration.getUsername())) {
            dataSourceConfiguration.setUser(configuration.getUsername());
        }
        if (Objects.nonNull(configuration.getPassword())) {
            dataSourceConfiguration.setPassword(configuration.getPassword());
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
        if (Objects.isNull(configuration.getDataSource())) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dataSourceConfig.getUrl());
            config.setUsername(dataSourceConfig.getUser());
            config.setPassword(dataSourceConfig.getPassword());
            config.setAutoCommit(false);
            return new HikariDataSource(config);
        } else {
            return configuration.getDataSource();
        }
    }

    /**
     * Setup default authenticator or custom authenticator.
     *
     * @param authenticationConfiguration to set authenticator
     * @param authenticatorConfiguration  to set authenticator implementation
     */
    private void setAuthenticationConfiguration(BrokerAuthConfiguration.AuthenticationConfiguration
                                                        authenticationConfiguration,
                                                BrokerAuthConfiguration.AuthenticatorConfiguration
                                                        authenticatorConfiguration) throws BrokerException {
        if (Objects.isNull(configuration.getAuthenticator())) {
            throw new BrokerException("Authenticator implementation is null.");
        } else {
            authenticatorConfiguration.setClassName(configuration.getAuthenticator().getClass().getCanonicalName());
            authenticatorConfiguration.setProperties(configuration.getAuthenticatorProperties());
        }
        authenticationConfiguration.setAuthenticator(authenticatorConfiguration);
    }

    /**
     * Setup default mac handler or custom mac handler.
     *
     * @param authorizationConfiguration to set mac handler
     */
    private void setMacConfiguration(BrokerAuthConfiguration.AuthorizationConfiguration authorizationConfiguration)
            throws BrokerException {
        BrokerAuthConfiguration.MacConfigurations macConfigurations =
                new BrokerAuthConfiguration.MacConfigurations();
        if (Objects.isNull(configuration.getMandatoryAccessController())) {
            throw new BrokerException("MandatoryAccessController implementation is null.");
        } else {
            macConfigurations.setClassName(configuration.getMandatoryAccessController().getClass().getCanonicalName());
        }
        authorizationConfiguration.setMandatoryAccessController(macConfigurations);
    }

    /**
     * Setup default userstore or custom userstore implementation.
     *
     * @param authorizationConfiguration to set userstore
     */
    private void setUserStoreConfiguration(BrokerAuthConfiguration.AuthorizationConfiguration
                                                   authorizationConfiguration) throws BrokerException {
        BrokerAuthConfiguration.UserStoreConfiguration userStoreConfiguration =
                new BrokerAuthConfiguration.UserStoreConfiguration();
        if (Objects.isNull(configuration.getUserStore())) {
            throw new BrokerException("UserStore implementation is null.");
        } else {
            userStoreConfiguration.setClassName(configuration.getUserStore().getClass().getCanonicalName());
        }
        authorizationConfiguration.setUserStore(userStoreConfiguration);
    }

    /**
     * Setup default dac handler or custom dac handler.
     *
     * @param authorizationConfiguration to set dac handler
     */
    private void setDacConfiguration(BrokerAuthConfiguration.AuthorizationConfiguration authorizationConfiguration)
            throws BrokerException {
        BrokerAuthConfiguration.DacConfigurations dacConfigurations =
                new BrokerAuthConfiguration.DacConfigurations();
        if (Objects.isNull(configuration.getDiscretionaryAccessController())) {
            throw new BrokerException("DiscretionaryAccessController implementation is null.");
        } else {
            dacConfigurations.setClassName(configuration.getDiscretionaryAccessController().getClass()
                    .getCanonicalName());
        }
        authorizationConfiguration.setDiscretionaryAccessController(dacConfigurations);
    }

    /**
     * setup authorization cache configuration.
     *
     * @param authorizationConfiguration to set timeout and size parameter
     */
    private void setCacheConfiguration(BrokerAuthConfiguration.AuthorizationConfiguration
                                               authorizationConfiguration) {
        BrokerAuthConfiguration.CacheConfiguration cacheConfiguration =
                new BrokerAuthConfiguration.CacheConfiguration();
        cacheConfiguration.setTimeout(configuration.getAuthorizationCacheTimeout());
        cacheConfiguration.setSize(configuration.getAuthorizationCacheSize());
        authorizationConfiguration.setCache(cacheConfiguration);
    }

    /**
     * Register broker configuration.
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
