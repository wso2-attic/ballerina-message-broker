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

package io.ballerina.messaging.broker.integration.standalone;

import io.ballerina.messaging.broker.amqp.Server;
import io.ballerina.messaging.broker.auth.AuthManager;
import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.auth.authorization.provider.DefaultMacHandler;
import io.ballerina.messaging.broker.auth.authorization.provider.RdbmsDacHandler;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerImpl;
import io.ballerina.messaging.broker.integration.util.DbUtils;
import io.ballerina.messaging.broker.integration.util.TestUtils;
import io.ballerina.messaging.broker.rest.BrokerRestServer;
import io.netty.util.ResourceLeakDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

import javax.sql.DataSource;

public class DefaultSuiteInitializer {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSuiteInitializer.class);

    private Broker broker;

    private Server server;

    private BrokerRestServer restServer;

    @Parameters({ "broker-port", "broker-ssl-port", "broker-hostname", "admin-username", "admin-password" ,
                  "broker-rest-port"})
    @BeforeSuite
    public void beforeSuite(String port, String sslPort, String hostname, String adminUsername, String adminPassword,
            String restPort, ITestContext context)
            throws Exception {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        LOGGER.info("Starting broker on " + port + " for suite " + context.getSuite().getName());
        StartupContext startupContext = TestUtils.initStartupContext(port, sslPort, hostname, restPort);

        BrokerConfigProvider configProvider = startupContext.getService(BrokerConfigProvider.class);
        BrokerAuthConfiguration brokerAuthConfiguration
                = configProvider.getConfigurationObject(BrokerAuthConfiguration.NAMESPACE,
                                                        BrokerAuthConfiguration.class);

        brokerAuthConfiguration.getAuthorization()
                               .setEnabled(true);
        brokerAuthConfiguration.getAuthorization()
                               .getDiscretionaryAccessController()
                               .setClassName(RdbmsDacHandler.class.getCanonicalName());
        brokerAuthConfiguration.getAuthorization()
                               .getMandatoryAccessController()
                               .setClassName(DefaultMacHandler.class.getCanonicalName());

        DbUtils.setupDB();
        DataSource dataSource = DbUtils.getDataSource();
        startupContext.registerService(DataSource.class, dataSource);

        AuthManager authManager = new AuthManager(startupContext);

        authManager.start();
        restServer = new BrokerRestServer(startupContext);
        broker = new BrokerImpl(startupContext);
        broker.startMessageDelivery();
        server = new Server(startupContext);
        server.start();
        restServer.start();
    }

    @AfterSuite
    public void afterSuite(ITestContext context) {
        System.gc(); // Hint JVM to clear unreference objects. Just to help detect ByteBuf object leaks.
        restServer.stop();
        server.stop();
        broker.stopMessageDelivery();
        LOGGER.info("Stopped broker for suite " + context.getSuite().getName());
    }

}
