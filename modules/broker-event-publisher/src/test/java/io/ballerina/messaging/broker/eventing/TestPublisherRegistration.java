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

package io.ballerina.messaging.broker.eventing;

import io.ballerina.messaging.broker.common.EventSync;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestPublisherRegistration {

    private EventConfiguration testConfig;
    private TestBrokerConfigProvider configProvider;
    private StartupContext startupContext;

    @BeforeMethod
    public void setUp() {
        configProvider = new TestBrokerConfigProvider();
        testConfig = new EventConfiguration();
        configProvider.addConfigObject(testConfig);
        startupContext = new StartupContext();
        startupContext.registerService(BrokerConfigProvider.class, configProvider);

    }

    @AfterMethod
    public void tearDown() {
        configProvider = new TestBrokerConfigProvider();
        testConfig = new EventConfiguration();
        configProvider.addConfigObject(testConfig);
        startupContext = new StartupContext();
        startupContext.registerService(EventConfiguration.class, testConfig);
    }

    @Test(description = "Test whether the right event publisher is loaded when events are enabled")
    public void testEventingEnabledPublisherLoad() throws Exception {
        testConfig.setEnabled(true);
        testConfig.setPublisherClass("io.ballerina.messaging.broker.eventing.TestPublisher");
        new EventService(startupContext);
        TestPublisher testPublisher = (TestPublisher) startupContext.getService(EventSync.class);
        String id = testPublisher.getID();
        Assert.assertEquals("TestPublisher", id);
    }

    @Test(description = "Test whether a event publisher is not loaded when events are disabled")
    public void testEventingDisabledPublisherLoad() throws Exception {
        testConfig.setEnabled(false);
        testConfig.setPublisherClass("io.ballerina.messaging.broker.eventing.TestPublisher");
        new EventService(startupContext);
        EventSync eventSync = startupContext.getService(EventSync.class);
        Assert.assertNull(eventSync);
    }

    @Test
    public void testStart() throws Exception {
        testConfig.setEnabled(true);
        testConfig.setPublisherClass("io.ballerina.messaging.broker.eventing.TestPublisher");
        EventService eventService = new EventService(startupContext);
        EventSync eventSync = startupContext.getService(EventSync.class);
        eventService.start();
        Assert.assertTrue(((TestPublisher) eventSync).isActiveState());
        eventService.stop();
        Assert.assertFalse(((TestPublisher) eventSync).isActiveState());
    }

    @Test
    public void testNegativeStartStop() throws Exception {
        testConfig.setEnabled(false);
        testConfig.setPublisherClass("io.ballerina.messaging.broker.eventing.TestPublisher");
        EventService eventService = new EventService(startupContext);
        eventService.start();
        eventService.stop();
    }

    @Test
    public void incorrectPublisherNameTest() throws Exception {
        testConfig.setEnabled(true);
        testConfig.setPublisherClass("io.ballerina.messaging.broker.eventing.IncorrectPublisherName");
        new EventService(startupContext);
        EventSync eventSync = startupContext.getService(EventSync.class);
        Assert.assertNull(eventSync);
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

        private void addConfigObject(Object configObject) {
            configMap.put(EventConfiguration.NAMESPACE, configObject);
        }
    }
}
