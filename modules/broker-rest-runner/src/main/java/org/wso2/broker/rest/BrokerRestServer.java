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

package org.wso2.broker.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.common.BrokerConfigProvider;
import org.wso2.broker.common.StartupContext;
import org.wso2.broker.rest.config.RestServerConfiguration;
import org.wso2.msf4j.MicroservicesRunner;

/**
 * Handles Rest server related tasks.
 */
public class BrokerRestServer {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerRestServer.class);

    private final MicroservicesRunner microservicesRunner;

    public BrokerRestServer(StartupContext context) throws Exception {
        BrokerConfigProvider configProvider = context.getService(BrokerConfigProvider.class);
        RestServerConfiguration configuration = configProvider.getConfigurationObject(RestServerConfiguration.NAMESPACE,
                                                                                      RestServerConfiguration.class);
        int port = Integer.parseInt(configuration.getPlain().getPort());
        microservicesRunner = new MicroservicesRunner(port);
        context.registerService(BrokerServiceRunner.class, new BrokerServiceRunner(microservicesRunner));
    }

    public void start() {
        microservicesRunner.start();
        LOGGER.info("Broker rest server started");
    }

    public void stop() {
        microservicesRunner.stop();
        LOGGER.info("Broker rest server stopped");
    }
}
