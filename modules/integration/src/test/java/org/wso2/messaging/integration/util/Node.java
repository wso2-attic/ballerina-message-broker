/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.messaging.integration.util;

import org.wso2.broker.amqp.AmqpServerConfiguration;
import org.wso2.broker.amqp.Server;
import org.wso2.broker.common.BrokerConfigProvider;
import org.wso2.broker.common.StartupContext;
import org.wso2.broker.coordination.BrokerHaConfiguration;
import org.wso2.broker.coordination.HaStrategy;
import org.wso2.broker.coordination.HaStrategyFactory;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.configuration.BrokerConfiguration;
import org.wso2.broker.core.security.authentication.jaas.BrokerLoginModule;
import org.wso2.broker.core.security.authentication.user.User;
import org.wso2.broker.core.security.authentication.user.UserStoreManager;
import org.wso2.broker.core.security.authentication.user.UsersFile;
import org.wso2.broker.rest.BrokerRestServer;
import org.wso2.broker.rest.config.RestServerConfiguration;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;
import javax.sql.DataSource;

/**
 * Representation of a single MB node.
 */
public class Node {

    private Broker broker;

    private Server server;

    private BrokerRestServer brokerRestServer;

    private HaStrategy haStrategy;

    private String hostname;

    private String port;

    public Node(String hostname, String port, String sslPort, String restPort, String adminUsername,
                String adminPassword, StartupContext startupContext, TestConfigProvider configProvider)
            throws Exception {

        this.hostname = hostname;
        this.port = port;

        BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
        BrokerConfiguration.AuthenticationConfiguration authenticationConfiguration = new BrokerConfiguration
                .AuthenticationConfiguration();
        authenticationConfiguration.setLoginModule(BrokerLoginModule.class.getCanonicalName());
        brokerConfiguration.setAuthenticator(authenticationConfiguration);
        configProvider.registerConfigurationObject(BrokerConfiguration.NAMESPACE, brokerConfiguration);

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
                throw new BrokerException("Error initializing HA Strategy: ", e);
            }
            startupContext.registerService(HaStrategy.class, haStrategy);
        }

        UsersFile usersFile = new UsersFile();
        User testUser = new User();
        testUser.setUsername(adminUsername);
        testUser.setPassword(adminPassword);
        List<User> userList = new LinkedList<>();
        userList.add(testUser);
        usersFile.setUsers(userList);
        UserStoreManager.addUser(usersFile.getUsers().get(0));

        brokerRestServer = new BrokerRestServer(startupContext);
        broker = new Broker(startupContext);
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
