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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.broker.coordination.BrokerHaConfiguration;
import org.wso2.carbon.config.ConfigProviderFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to test broker HA configuration.
 */
public class BrokerHaConfigurationTest {

    private BrokerHaConfiguration brokerHaConfiguration = new BrokerHaConfiguration();

    private BrokerHaConfiguration.HaConfiguration haConfiguration;

    @BeforeClass
    public void setUp() throws Exception {
        String brokerFilePath = new File(this.getClass().getResource("broker.yaml").getFile())
                .getAbsolutePath();
        Path brokerYamlFile = Paths.get(brokerFilePath).toAbsolutePath();
        haConfiguration = ConfigProviderFactory.getConfigProvider(brokerYamlFile, null)
                .getConfigurationObject("haConfig", BrokerHaConfiguration.HaConfiguration.class);
    }

    @Test(description = "Test configuration loading from yaml files")
    public void testHaConfigurationCreationFromYamlFile() {
        Assert.assertTrue(haConfiguration.isEnabled(), "\"enabled\" value not set correctly.");
        Assert.assertEquals(haConfiguration.getStrategy(), "org.wso2.broker.coordination.rdbms.RdbmsHaStrategy",
                "HA strategy not set correctly");
    }


    @Test(dataProvider = "haConfigs", description = "Test setters and getters of BrokerHaConfiguration")
    public void testSetAndGetHaConfiguration(boolean enabled, String strategy) {
        String haConfigString = "HA Configuration [enabled=" + enabled + ", strategy=" + strategy + "]";
        haConfiguration.setEnabled(enabled);
        haConfiguration.setStrategy(strategy);
        brokerHaConfiguration.setHaConfig(haConfiguration);
        Assert.assertEquals(haConfiguration.isEnabled(), enabled, "\"enabled\" value does not match the value set");
        Assert.assertEquals(haConfiguration.getStrategy(), strategy,
                "HA strategy value does not match the value set");
        Assert.assertEquals(haConfiguration.toString(), haConfigString, "Incorrect toString() representation of "
                + "HaConfiguration");
        Assert.assertEquals(brokerHaConfiguration.getHaConfig(), haConfiguration, "HA configuration does not match "
                + "the configuration set");
    }

    @DataProvider(name = "haConfigs")
    public Object[][] haConfigs() {
        return new Object[][] {
                { true, "org.wso2.broker.coordination.HaStrategyOne" },
                { false, "org.wso2.broker.coordination.HaStrategyTwo" }
        };
    }

}
