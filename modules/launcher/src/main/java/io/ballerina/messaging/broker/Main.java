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
import io.ballerina.messaging.broker.amqp.Server;
import io.ballerina.messaging.broker.auth.AuthManager;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerCommonConfiguration;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.coordination.CoordinationException;
import io.ballerina.messaging.broker.coordination.HaStrategy;
import io.ballerina.messaging.broker.coordination.HaStrategyFactory;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerImpl;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.metrics.BrokerMetricService;
import io.ballerina.messaging.broker.rest.BrokerRestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
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
            BrokerConfigProvider service = startupContext.getService(BrokerConfigProvider.class);
            BrokerCommonConfiguration commonConfig = service.getConfigurationObject(BrokerCommonConfiguration.NAMESPACE,
                                                                                    BrokerCommonConfiguration.class);
            DataSource dataSource = getDataSource(commonConfig.getDataSource());

            startupContext.registerService(DataSource.class, dataSource);
            HaStrategy haStrategy;
            //Initializing an HaStrategy implementation only if HA is enabled
            try {
                haStrategy = HaStrategyFactory.getHaStrategy(startupContext);
            } catch (Exception e) {
                throw new CoordinationException("Error initializing HA Strategy: ", e);
            }

            AuthManager authManager = new AuthManager(startupContext);
            BrokerMetricService metricService = new BrokerMetricService(startupContext);
            BrokerRestServer restServer = new BrokerRestServer(startupContext);
            Broker broker = new BrokerImpl(startupContext);
            Server amqpServer = new Server(startupContext);
            registerShutdownHook(broker, amqpServer, restServer, haStrategy, authManager, metricService);

            if (haStrategy != null) {
                //Start the HA strategy after all listeners have been registered, and before the listeners are started
                haStrategy.start();
            }

            metricService.start();
            authManager.start();
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

    private static DataSource getDataSource(BrokerCommonConfiguration.DataSourceConfiguration dataSourceConfiguration) {
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
     *  (1) Load the configuration file specified in 'broker.file' (e.g. -Dbroker.file={FilePath}). <br/>
     *  (2) If -Dbroker.file is not specified, the broker.yaml file exists in current directory and load it. <br/>
     *
     * <b>Note: </b> if provided configuration file cannot be read broker will not start.
     * @param startupContext startup context of the broker
     */
    private static void initConfigProvider(StartupContext startupContext) throws ConfigurationException {
        Path brokerYamlFile;
        String brokerFilePath = System.getProperty(BrokerCoreConfiguration.SYSTEM_PARAM_BROKER_CONFIG_FILE);
        if (brokerFilePath == null || brokerFilePath.trim().isEmpty()) {
            // use current path.
            brokerYamlFile = Paths.get("", BrokerCoreConfiguration.BROKER_FILE_NAME).toAbsolutePath();
        } else {
            brokerYamlFile = Paths.get(brokerFilePath).toAbsolutePath();
        }

        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(brokerYamlFile);
        startupContext.registerService(BrokerConfigProvider.class,
                                       (BrokerConfigProvider) configProvider::getConfigurationObject);
    }

    /**
     * Method to register a shutdown hook to ensure proper cleaning up.
     */
    private static void registerShutdownHook(Broker broker,
                                             Server server,
                                             BrokerRestServer brokerRestServer,
                                             HaStrategy haStrategy,
                                             AuthManager authManager,
                                             BrokerMetricService metricService) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (LOCK) {
                shutdownHookTriggered = true;
                brokerRestServer.shutdown();
                authManager.stop();
                try {
                    server.shutdown();
                    server.awaitServerClose();
                } catch (InterruptedException e) {
                    LOGGER.warn("Error stopping transport on shut down {}", e);
                    Thread.currentThread().interrupt();
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
