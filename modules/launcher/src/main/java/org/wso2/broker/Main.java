/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.Server;
import org.wso2.broker.common.BrokerConfigProvider;
import org.wso2.broker.common.StartupContext;
import org.wso2.broker.coordination.HaStrategy;
import org.wso2.broker.coordination.HaStrategyFactory;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.configuration.BrokerConfiguration;
import org.wso2.broker.core.security.authentication.user.User;
import org.wso2.broker.core.security.authentication.user.UserStoreManager;
import org.wso2.broker.core.security.authentication.user.UsersFile;
import org.wso2.broker.core.security.authentication.util.BrokerSecurityConstants;
import org.wso2.broker.metrics.BrokerMetricService;
import org.wso2.broker.rest.BrokerRestServer;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.sql.DataSource;

/**
 * Starting point of the broker.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final Object LOCK = new Object();
    private static volatile boolean shutdownHookTriggered = false;

    public static void main(String[] args) throws Exception {

        try {
            StartupContext startupContext = new StartupContext();

            initConfigProvider(startupContext);
            loadUsers();
            BrokerConfigProvider service = startupContext.getService(BrokerConfigProvider.class);
            BrokerConfiguration brokerConfiguration =
                    service.getConfigurationObject(BrokerConfiguration.NAMESPACE, BrokerConfiguration.class);
            DataSource dataSource = getDataSource(brokerConfiguration.getDataSource());
            startupContext.registerService(DataSource.class, dataSource);

            HaStrategy haStrategy;
            //Initializing an HaStrategy implementation only if HA is enabled
            try {
                haStrategy = HaStrategyFactory.getHaStrategy(startupContext);
                if (haStrategy != null) {
                    startupContext.registerService(HaStrategy.class, haStrategy);
                }
            } catch (Exception e) {
                throw new BrokerException("Error initializing HA Strategy: ", e);
            }

            BrokerMetricService metricService = new BrokerMetricService(startupContext);
            BrokerRestServer restServer = new BrokerRestServer(startupContext);
            Broker broker = new Broker(startupContext);
            Server amqpServer = new Server(startupContext);
            registerShutdownHook(broker, amqpServer, restServer, haStrategy, metricService);

            if (haStrategy != null) {
                //Start the HA strategy after all listeners have been registered, and before the listeners are started
                haStrategy.start();
            }

            metricService.start();
            broker.startMessageDelivery();
            amqpServer.start();
            restServer.start();

            synchronized (LOCK) {
                while (!shutdownHookTriggered) {
                    LOCK.wait();
                }
            }
        } catch (Throwable e) {
            LOGGER.error("Error while starting broker", e);
            throw e;
        }
    }

    private static DataSource getDataSource(BrokerConfiguration.DataSourceConfiguration dataSourceConfiguration) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSourceConfiguration.getUrl());
        config.setUsername(dataSourceConfiguration.getUser());
        config.setPassword(dataSourceConfiguration.getPassword());
        config.setAutoCommit(false);

        return new HikariDataSource(config);
    }

    /**
     * Loads configurations during the broker start up.
     * method will try to <br/>
     *  (1) Load the configuration file specified in 'broker.file' (e.g. -Dbroker.file=<FilePath>). <br/>
     *  (2) If -Dbroker.file is not specified, the broker.yaml file exists in current directory and load it. <br/>
     *
     *  <b>Note: </b> if provided configuration file cannot be read broker will not start.
     * @param startupContext startup context of the broker
     */
    private static void initConfigProvider(StartupContext startupContext) throws ConfigurationException {
        Path brokerYamlFile;
        String brokerFilePath = System.getProperty(BrokerConfiguration.SYSTEM_PARAM_BROKER_CONFIG_FILE);
        if (brokerFilePath == null || brokerFilePath.trim().isEmpty()) {
            // use current path.
            brokerYamlFile = Paths.get("", BrokerConfiguration.BROKER_FILE_NAME).toAbsolutePath();
        } else {
            brokerYamlFile = Paths.get(brokerFilePath).toAbsolutePath();
        }

        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(brokerYamlFile, null);
        startupContext.registerService(BrokerConfigProvider.class,
                                       (BrokerConfigProvider) configProvider::getConfigurationObject);
    }

    /**
     * Loads the users from users.yaml during broker startup
     */
    private static void loadUsers() throws ConfigurationException {
        Path usersYamlFile;
        String usersFilePath = System.getProperty(BrokerSecurityConstants.SYSTEM_PARAM_USERS_CONFIG);
        if (usersFilePath == null || usersFilePath.trim().isEmpty()) {
            // use current path.
            usersYamlFile = Paths.get("", BrokerSecurityConstants.USERS_FILE_NAME).toAbsolutePath();
        } else {
            usersYamlFile = Paths.get(usersFilePath).toAbsolutePath();
        }
        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(usersYamlFile, null);
        UsersFile usersFile = configProvider
                .getConfigurationObject(BrokerSecurityConstants.USERS_CONFIG_NAMESPACE, UsersFile.class);
        if (usersFile != null) {
            List<User> users = usersFile.getUsers();
            for (User user : users) {
                UserStoreManager.addUser(user);
            }
        }
    }

    /**
     * Method to register a shutdown hook to ensure proper cleaning up.
     */
    private static void registerShutdownHook(Broker broker,
                                             Server server,
                                             BrokerRestServer brokerRestServer,
                                             HaStrategy haStrategy,
                                             BrokerMetricService metricService) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (LOCK) {
                shutdownHookTriggered = true;
                brokerRestServer.shutdown();
                try {
                    server.shutdown();
                    server.awaitServerClose();
                } catch (InterruptedException e) {
                    LOGGER.warn("Error stopping transport on shut down {}", e);
                }
                broker.shutdown();
                metricService.stop();
                if (haStrategy != null) {
                    haStrategy.stop();
                }
                LOCK.notifyAll();
            }
        }));
    }

}
