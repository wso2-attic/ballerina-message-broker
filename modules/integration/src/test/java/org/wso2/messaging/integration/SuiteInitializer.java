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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.messaging.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.wso2.broker.amqp.AmqpServerConfiguration;
import org.wso2.broker.amqp.Server;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.configuration.BrokerConfiguration;
import org.wso2.messaging.integration.util.TestConstants;

public class SuiteInitializer {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SuiteInitializer.class);

    private Broker broker;
    private Server server;

    @Parameters({"broker-port", "broker-ssl-port", "broker-hostname"})
    @BeforeSuite
    public void beforeSuite(String port, String sslPort, String hostname, ITestContext context) throws Exception {
        LOGGER.info("Starting broker on " + port + " for suite " + context.getSuite().getName());
        BrokerConfiguration configuration = new BrokerConfiguration();
        AmqpServerConfiguration serverConfiguration = new AmqpServerConfiguration();
        serverConfiguration.getPlain().setPort(port);
        serverConfiguration.getPlain().setHostName(hostname);

        serverConfiguration.getSsl().setEnabled(true);
        serverConfiguration.getSsl().setHostName(hostname);
        serverConfiguration.getSsl().setPort(sslPort);
        serverConfiguration.getSsl().getKeyStore().setLocation(TestConstants.KEYSTORE_LOCATION);
        serverConfiguration.getSsl().getKeyStore().setPassword(TestConstants.KEYSTORE_PASSWORD);
        serverConfiguration.getSsl().getTrustStore().setLocation(TestConstants.TRUST_STORE_LOCATION);
        serverConfiguration.getSsl().getTrustStore().setPassword(TestConstants.TRUST_STORE_PASSWORD);

        broker = new Broker(configuration);
        broker.startMessageDelivery();
        server = new Server(broker, serverConfiguration);
        server.start();
    }

    @AfterSuite
    public void afterSuite(ITestContext context) throws Exception {
        server.stop();
        broker.stopMessageDelivery();
        LOGGER.info("Stopped broker for suite " + context.getSuite().getName());
    }
}
