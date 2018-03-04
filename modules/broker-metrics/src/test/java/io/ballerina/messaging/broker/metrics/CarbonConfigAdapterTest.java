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

package io.ballerina.messaging.broker.metrics;

import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CarbonConfigAdapterTest {

    private static final String TEST_CONFIG_NAMESPACE = "test.config";
    private static final String NAMESPACE_KEY = "namespace-key";
    private static final String CLASS_KEY = "class-key";
    private TestBrokerConfigProvider configProvider;

    @BeforeClass
    public void setUp() throws Exception {
        configProvider = new TestBrokerConfigProvider();
        configProvider.addConfigObject(TEST_CONFIG_NAMESPACE, new TestConfig(NAMESPACE_KEY));
        configProvider.addConfigObject(TestConfig.class.getCanonicalName(), new TestConfig(CLASS_KEY));
    }

    @Test
    public void testGetConfigurationObject() throws Exception {
        CarbonConfigAdapter carbonConfigAdapter = new CarbonConfigAdapter(configProvider);
        TestConfig configurationObject = (TestConfig) carbonConfigAdapter.getConfigurationObject(TEST_CONFIG_NAMESPACE);
        Assert.assertEquals(configurationObject.getTestField(), NAMESPACE_KEY);
    }

    @Test
    public void testGetConfigurationObject1() throws Exception {
        CarbonConfigAdapter carbonConfigAdapter = new CarbonConfigAdapter(configProvider);
        TestConfig configurationObject = carbonConfigAdapter.getConfigurationObject(TestConfig.class);
        Assert.assertEquals(configurationObject.getTestField(), CLASS_KEY);
    }

    @Test
    public void testGetConfigurationObject2() throws Exception {
        CarbonConfigAdapter carbonConfigAdapter = new CarbonConfigAdapter(configProvider);
        TestConfig configurationObject = carbonConfigAdapter.getConfigurationObject(TEST_CONFIG_NAMESPACE,
                                                                                    TestConfig.class);
        Assert.assertEquals(configurationObject.getTestField(), NAMESPACE_KEY);
    }

    @Test(expectedExceptions = ConfigurationException.class)
    public void testGetInvalidConfigurationObject() throws Exception {
        CarbonConfigAdapter carbonConfigAdapter = new CarbonConfigAdapter(configProvider);
        TestConfig configurationObject = carbonConfigAdapter.getConfigurationObject("invalid.namespace",
                                                                                    TestConfig.class);
        Assert.assertEquals(configurationObject.getTestField(), NAMESPACE_KEY);
    }

    private static class TestBrokerConfigProvider implements BrokerConfigProvider {
        Map<String, Object> configMap = new HashMap<>();
        @Override
        public <T> T getConfigurationObject(String namespace, Class<T> configurationClass) throws Exception {
            Object configObject = configMap.get(namespace);

            if (Objects.isNull(configObject)) {
                throw new Exception("Unknown key");
            }
            return configurationClass.cast(configObject);
        }

        private void addConfigObject(String namespace, Object configObject) {
            configMap.put(namespace, configObject);
        }
    }

    private static class TestConfig {
        private String testField;

        public TestConfig(String value) {
            testField = value;
        }

        public String getTestField() {
            return testField;
        }

        public void setTestField(String testField) {
            this.testField = testField;
        }
    }
}
