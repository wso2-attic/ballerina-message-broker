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

package io.ballerina.messaging.broker.integration.util;

import io.ballerina.messaging.broker.amqp.AmqpServerConfiguration;
import io.ballerina.messaging.broker.amqp.Server;
import io.ballerina.messaging.broker.auth.AuthManager;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.BrokerAuthConstants;
import io.ballerina.messaging.broker.auth.authentication.authenticator.JaasAuthenticator;
import io.ballerina.messaging.broker.auth.authentication.jaas.UserStoreLoginModule;
import io.ballerina.messaging.broker.auth.authorization.provider.DefaultMacHandler;
import io.ballerina.messaging.broker.auth.authorization.provider.RdbmsDacHandler;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerCommonConfiguration;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.coordination.BrokerHaConfiguration;
import io.ballerina.messaging.broker.coordination.CoordinationException;
import io.ballerina.messaging.broker.coordination.HaStrategy;
import io.ballerina.messaging.broker.coordination.HaStrategyFactory;
import io.ballerina.messaging.broker.coordination.rdbms.RdbmsHaStrategy;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerFactory;
import io.ballerina.messaging.broker.core.BrokerImpl;
import io.ballerina.messaging.broker.core.DefaultBrokerFactory;
import io.ballerina.messaging.broker.core.SecureBrokerFactory;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.rest.BrokerRestServer;
import io.ballerina.messaging.broker.rest.config.RestServerConfiguration;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import javax.sql.DataSource;

/**
 * Representation of a single MB node.
 */
public class BrokerNode {

    private Broker broker;

    private Server server;

    private BrokerRestServer brokerRestServer;

    private HaStrategy haStrategy;

    private String hostname;

    private String port;

    public BrokerNode(String hostname, String port, String sslPort, String restPort, String adminUsername,
                      String adminPassword, StartupContext startupContext, TestConfigProvider configProvider)
            throws Exception {

        this.hostname = hostname;
        this.port = port;

        BrokerCoreConfiguration brokerCoreConfiguration = new BrokerCoreConfiguration();
        configProvider.registerConfigurationObject(BrokerCoreConfiguration.NAMESPACE, brokerCoreConfiguration);

        AmqpServerConfiguration serverConfiguration = new AmqpServerConfiguration();
        serverConfiguration.setHostName(hostname);
        serverConfiguration.getPlain().setPort(port);
        serverConfiguration.getSsl().setEnabled(true);
        serverConfiguration.getSsl().setPort(sslPort);
        serverConfiguration.getSsl().getKeyStore().setLocation(TestConstants.KEYSTORE_LOCATION);
        serverConfiguration.getSsl().getKeyStore().setPassword(TestConstants.KEYSTORE_PASSWORD);
        serverConfiguration.getSsl().getTrustStore().setLocation(TestConstants.TRUST_STORE_LOCATION);
        serverConfiguration.getSsl().getTrustStore().setPassword(TestConstants.TRUST_STORE_PASSWORD);
        configProvider.registerConfigurationObject(AmqpServerConfiguration.NAMESPACE, serverConfiguration);

        RestServerConfiguration restConfig = new RestServerConfiguration();
        restConfig.getPlain().setPort(restPort);
        configProvider.registerConfigurationObject(RestServerConfiguration.NAMESPACE, restConfig);

        BrokerHaConfiguration haConfiguration = configProvider.getConfigurationObject(
                BrokerHaConfiguration.NAMESPACE, BrokerHaConfiguration.class);

        startupContext.registerService(BrokerHaConfiguration.class, haConfiguration);
        startupContext.registerService(DataSource.class, DbUtils.getDataSource());

        startupContext.registerService(BrokerConfigProvider.class, configProvider);

        if (haConfiguration.isEnabled()) {
            //Initializing an HaStrategy implementation only if HA is enabled
            try {
                haStrategy = HaStrategyFactory.getHaStrategy(startupContext);
            } catch (Exception e) {
                throw new CoordinationException("Error initializing HA Strategy: ", e);
            }
        }

        // auth configurations
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(BrokerAuthConstants.USERS_FILE_NAME);
        if (resource != null) {
            System.setProperty(BrokerAuthConstants.SYSTEM_PARAM_USERS_CONFIG, resource.getFile());
        }

        BrokerAuthConfiguration brokerAuthConfiguration = new BrokerAuthConfiguration();
        BrokerAuthConfiguration.AuthenticationConfiguration authenticationConfiguration =
                new BrokerAuthConfiguration.AuthenticationConfiguration();
        BrokerAuthConfiguration.AuthenticatorConfiguration authenticatorConfiguration =
                new BrokerAuthConfiguration.AuthenticatorConfiguration();
        HashMap<String, Object> properties = new HashMap<>();
        properties.put(BrokerAuthConstants.CONFIG_PROPERTY_JAAS_LOGIN_MODULE,
                       UserStoreLoginModule.class.getCanonicalName());
        authenticatorConfiguration.setClassName(JaasAuthenticator.class.getCanonicalName());
        authenticatorConfiguration.setProperties(properties);
        authenticationConfiguration.setAuthenticator(authenticatorConfiguration);
        brokerAuthConfiguration.setAuthentication(authenticationConfiguration);
        brokerAuthConfiguration.getAuthorization()
                               .getMandatoryAccessController()
                               .setClassName(DefaultMacHandler.class.getName());
        brokerAuthConfiguration.getAuthorization()
                               .getDiscretionaryAccessController()
                               .setClassName(RdbmsDacHandler.class.getName());
        BrokerCommonConfiguration brokerCommonConfiguration = new BrokerCommonConfiguration();
        configProvider.registerConfigurationObject(BrokerAuthConfiguration.NAMESPACE, brokerAuthConfiguration);
        configProvider.registerConfigurationObject(BrokerCommonConfiguration.NAMESPACE, brokerCommonConfiguration);
        AuthManager authManager = new AuthManager(startupContext);

        authManager.start();
        brokerRestServer = new BrokerRestServer(startupContext);
        broker = new BrokerImpl(startupContext);
        BrokerFactory brokerFactory;
        if (authManager.isAuthorizationEnabled()) {
            brokerFactory = new SecureBrokerFactory(startupContext);
        } else {
            brokerFactory = new DefaultBrokerFactory(startupContext);
        }
        startupContext.registerService(BrokerFactory.class, brokerFactory);
        server = new Server(startupContext);
    }

    public void startUp() throws CertificateException, InterruptedException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        if (haStrategy != null) {
            haStrategy.start();
        }
        broker.startMessageDelivery();
        server.start();
        brokerRestServer.start();
    }

    public void shutdown() throws InterruptedException {
        if (haStrategy != null) {
            haStrategy.stop();
        }
        brokerRestServer.stop();
        server.stop();
        server.awaitServerClose();
        broker.stopMessageDelivery();
    }

    public void pause() throws CoordinationException {
        if (haStrategy instanceof RdbmsHaStrategy) {
            ((RdbmsHaStrategy) haStrategy).pause();
        } else {
            throw new CoordinationException("Pause functionality not available for " + haStrategy.getClass());
        }
    }

    public void resume() throws CoordinationException {
        if (haStrategy instanceof RdbmsHaStrategy) {
            ((RdbmsHaStrategy) haStrategy).resume();
        } else {
            throw new CoordinationException("Resume functionality not available for " + haStrategy.getClass());
        }
    }

    public boolean isActiveNode() {
        return haStrategy.isActiveNode();
    }

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
    }
}
