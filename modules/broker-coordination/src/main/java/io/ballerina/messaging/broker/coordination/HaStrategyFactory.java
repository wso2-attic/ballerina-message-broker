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

package io.ballerina.messaging.broker.coordination;

import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class to retrieve an instance of the specified implementation of {@link HaStrategy}.
 */
public class HaStrategyFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(HaStrategyFactory.class);

    /**
     * Method to retrieve an instance of the specified implementation of {@link HaStrategy}.
     *
     * @param startupContext the startup context from which registered services can be retrieved
     * @return an instance of the specified implementation of {@link HaStrategy}
     * @throws Exception if an error occurs initializing the HaStrategy
     */
    public static HaStrategy getHaStrategy(StartupContext startupContext) throws Exception {
        HaStrategy haStrategy;
        BrokerConfigProvider brokerConfigProvider = startupContext.getService(BrokerConfigProvider.class);
        BrokerHaConfiguration brokerHaConfiguration = brokerConfigProvider
                .getConfigurationObject(BrokerHaConfiguration.NAMESPACE, BrokerHaConfiguration.class);
        startupContext.registerService(BrokerHaConfiguration.class, brokerHaConfiguration);
        if (!brokerHaConfiguration.isEnabled()) {
            return null;
        }
        String haStrategyClass;
        haStrategyClass = brokerHaConfiguration.getStrategy();
        LOGGER.info("Initializing HA Strategy: {}", haStrategyClass);
        haStrategy = (HaStrategy) ClassLoader.getSystemClassLoader().loadClass(haStrategyClass).newInstance();
        haStrategy.setup(startupContext);
        startupContext.registerService(HaStrategy.class, haStrategy);
        return haStrategy;
    }

}
