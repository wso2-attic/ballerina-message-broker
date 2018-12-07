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

package io.ballerina.messaging.broker.rest;

import io.ballerina.messaging.broker.auth.AuthManager;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.coordination.BasicHaListener;
import io.ballerina.messaging.broker.coordination.HaListener;
import io.ballerina.messaging.broker.coordination.HaStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.transport.http.netty.config.TransportsConfiguration;

/**
 * Handles Rest server related tasks.
 */
public class BrokerRestServer {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerRestServer.class);

    private final MicroservicesRunner microservicesRunner;
    private static final String TRANSPORT_CONFIGURATION_NAMESPACE = "wso2.broker.admin.service";

    /**
     * The {@link HaStrategy} for which the HA listener is registered.
     */
    private HaStrategy haStrategy;

    private BrokerRestRunnerHelper brokerRestRunnerHelper;

    public BrokerRestServer(StartupContext startupContext) throws Exception {
        AuthManager authManager = startupContext.getService(AuthManager.class);
        BrokerConfigProvider configProvider = startupContext.getService(BrokerConfigProvider.class);
        TransportsConfiguration transportsConfiguration = configProvider
                .getConfigurationObject(TRANSPORT_CONFIGURATION_NAMESPACE, TransportsConfiguration.class);

        microservicesRunner = new MicroservicesRunner(transportsConfiguration);

        startupContext.registerService(BrokerServiceRunner.class,
                                       new BrokerServiceRunner(microservicesRunner, authManager));
        haStrategy = startupContext.getService(HaStrategy.class);
        if (haStrategy == null) {
            brokerRestRunnerHelper = new BrokerRestRunnerHelper();
        } else {
            LOGGER.info("Broker Rest Runner is in PASSIVE mode"); //starts up in passive mode
            brokerRestRunnerHelper = new HaEnabledBrokerRestRunnerHelper();
        }
    }

    public void start() {
        brokerRestRunnerHelper.start();
    }

    public void stop() {
        microservicesRunner.stop();
        LOGGER.info("Broker admin service stopped.");
    }

    public void shutdown() {
        brokerRestRunnerHelper.shutdown();
    }

    private class BrokerRestRunnerHelper {

        public void start() {
            microservicesRunner.start();
            LOGGER.info("Broker admin service started");
        }

        public void shutdown() {
            stop();
        }

    }

    private class HaEnabledBrokerRestRunnerHelper extends BrokerRestRunnerHelper implements HaListener {

        private BasicHaListener basicHaListener;

        HaEnabledBrokerRestRunnerHelper() {
            basicHaListener = new BasicHaListener(this);
            haStrategy.registerListener(basicHaListener, 3);
        }

        @Override
        public synchronized void start() {
            basicHaListener.setStartCalled(); //to allow starting when the node becomes active when HA is enabled
            if (!basicHaListener.isActive()) {
                return;
            }
            super.start();
        }

        @Override
        public void shutdown() {
            haStrategy.unregisterListener(basicHaListener);
            super.shutdown();
        }

        /**
         * {@inheritDoc}
         */
        public void activate() {
            startOnBecomingActive();
            LOGGER.info("Broker Rest Server mode changed from PASSIVE to ACTIVE");
        }

        /**
         * {@inheritDoc}
         */
        public void deactivate() {
            stop();
            LOGGER.info("Broker Rest Server mode changed from ACTIVE to PASSIVE");
        }

        /**
         * Method to start the broker rest server, only if start has been called, prior to becoming the active node.
         */
        private synchronized void startOnBecomingActive() {
            if (basicHaListener.isStartCalled()) {
                start();
            }
        }

    }
}
