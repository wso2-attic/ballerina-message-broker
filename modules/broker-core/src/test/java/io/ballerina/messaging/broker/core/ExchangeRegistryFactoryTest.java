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

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.eventingutil.TestPublisher;
import io.ballerina.messaging.broker.core.store.dao.BindingDao;
import io.ballerina.messaging.broker.core.store.dao.ExchangeDao;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Objects;


public class ExchangeRegistryFactoryTest {

    private BrokerCoreConfiguration.EventConfig eventConfig;
    @BeforeClass
    public void setup() {
        eventConfig = new BrokerCoreConfiguration().getEventConfig();
    }

    @Test(description = "Test get exchange registry function", dataProvider = "sample publishers")
    public void testGetExchangeRegistry(String exchangeName, TestPublisher testPublisher, boolean enabled)
            throws BrokerException,
            ValidationException {
        eventConfig.setExchangeAdminEventsEnabled(enabled);
        ExchangeRegistryFactory exchangeRegistryFactory = new ExchangeRegistryFactory(Mockito.mock(ExchangeDao.class),
                Mockito.mock(BindingDao.class),
                testPublisher,
                eventConfig);
        ExchangeRegistry exchangeRegistry = exchangeRegistryFactory.getExchangeRegistry();
        exchangeRegistry.createExchange(exchangeName, Exchange.Type.TOPIC, false);
        if (Objects.nonNull(testPublisher) && enabled) {
            Assert.assertNotNull(testPublisher.id);
        }
    }

    @DataProvider(name = "sample publishers")
    public Object[][] publishers() {
        return new Object[][] {
                {"test1", null, true},
                {"test2", new TestPublisher(), true},
                {"test3", null, false},
                {"test4", new TestPublisher(), false}};
    }
}
