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

import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.coordination.BrokerHaConfiguration;
import io.ballerina.messaging.broker.coordination.HaStrategyFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for {@link HaStrategyFactory}.
 */
public class HaStrategyFactoryTest {

    @Test
    public void testGetHaStrategyWithFailoverDisabled() throws Exception {
        BrokerConfigProvider testConfigProvider = new TestConfigProvider();
        BrokerHaConfiguration brokerHaConfiguration = new BrokerHaConfiguration();
        brokerHaConfiguration.setEnabled(false);
        ((TestConfigProvider) testConfigProvider)
                .registerConfigurationObject(BrokerHaConfiguration.NAMESPACE, brokerHaConfiguration);
        StartupContext testStartupContext = new StartupContext();
        testStartupContext.registerService(BrokerConfigProvider.class, testConfigProvider);
        Assert.assertNull(HaStrategyFactory.getHaStrategy(testStartupContext),
                          "HaStrategy not null when failover is disabled");
    }

    private class TestConfigProvider implements BrokerConfigProvider {

        private Map<String, Object> configMap = new HashMap<>();

        @Override
        public <T> T getConfigurationObject(String namespace, Class<T> configurationClass) throws Exception {
            return configurationClass.cast(configMap.get(namespace));
        }

        void registerConfigurationObject(String namespace, Object configObject) {
            configMap.put(namespace, configObject);
        }

    }


}
