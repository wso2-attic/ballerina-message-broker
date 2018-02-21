/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.messaging.broker.coordination.BrokerHaConfiguration;
import io.ballerina.messaging.broker.coordination.rdbms.RdbmsCoordinationConstants;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigProviderFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to test broker HA configuration.
 */
public class BrokerHaConfigurationTest {

    private BrokerHaConfiguration brokerHaConfiguration = new BrokerHaConfiguration();

    @BeforeClass
    public void setUp() throws Exception {
        String brokerFilePath = new File(this.getClass().getResource("broker.yaml").getFile())
                .getAbsolutePath();
        Path brokerYamlFile = Paths.get(brokerFilePath).toAbsolutePath();
        brokerHaConfiguration = ConfigProviderFactory.getConfigProvider(brokerYamlFile, null)
                .getConfigurationObject(BrokerHaConfiguration.NAMESPACE, BrokerHaConfiguration.class);
    }

    @Test(description = "Test configuration loading from yaml files")
    public void testHaConfigurationCreationFromYamlFile() {
        Assert.assertTrue(brokerHaConfiguration.isEnabled(), "\"enabled\" value not set correctly.");
        Assert.assertEquals(brokerHaConfiguration.getStrategy(),
                            "io.ballerina.messaging.broker.coordination.rdbms.RdbmsHaStrategy",
                            "HA strategy not set correctly");
    }


    @Test(dataProvider = "haConfigs", description = "Test setters and getters of BrokerHaConfiguration")
    public void testSetAndGetHaConfiguration(boolean enabled, String strategy, String nodeId, String hearbeatInterval,
                                             String coordinatorEntryCreationWaitTime) {
        Map<String, String> options = new HashMap<>();
        options.put(RdbmsCoordinationConstants.NODE_IDENTIFIER, nodeId);
        options.put(RdbmsCoordinationConstants.HEARTBEAT_INTERVAL, hearbeatInterval);
        options.put(RdbmsCoordinationConstants.COORDINATOR_ENTRY_CREATION_WAIT_TIME, coordinatorEntryCreationWaitTime);
        String haConfigString = "Failover [enabled=" + enabled + ", strategy=" + strategy + "]";
        brokerHaConfiguration.setEnabled(enabled);
        brokerHaConfiguration.setStrategy(strategy);
        brokerHaConfiguration.setOptions(options);
        Assert.assertEquals(brokerHaConfiguration.isEnabled(), enabled, "\"enabled\" value does not match the value"
                + " set");
        Assert.assertEquals(brokerHaConfiguration.getStrategy(), strategy,
                "HA strategy value does not match the value set");
        Assert.assertEquals(brokerHaConfiguration.getOptions(), options, "HA strategy options values do not match "
                + "the value set");
        Assert.assertEquals(brokerHaConfiguration.toString(), haConfigString, "Incorrect toString() representation "
                + "of HaConfiguration " + brokerHaConfiguration.toString());
    }

    @DataProvider(name = "haConfigs")
    public Object[][] haConfigs() {
        return new Object[][] {
                { true, "io.ballerina.messaging.broker.coordination.HaStrategyOne", "nodeIdOne", "5000", "7000" },
                { false, "io.ballerina.messaging.broker.coordination.HaStrategyTwo", "nodeIdTwo", "10000", "8000" }
        };
    }

}
