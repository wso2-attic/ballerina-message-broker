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
import io.ballerina.messaging.broker.core.eventingutil.TestPublisher;
import io.ballerina.messaging.broker.core.store.dao.BindingDao;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ObservableExchangeRegistryImplTest {
    private TestPublisher testPublisher;
    private ObservableExchangeRegistryImpl observableExchangeRegistry;

    @BeforeClass
    public void setup() {
        testPublisher = new TestPublisher();
        observableExchangeRegistry = new ObservableExchangeRegistryImpl(
                new ExchangeRegistryImpl(new NoOpExchangeDaoTestUtil(),
                        Mockito.mock(BindingDao.class)),
                testPublisher);
    }

    @BeforeMethod
    public void start() {
        testPublisher.id = null;
        testPublisher.properties = null;
    }

    @AfterMethod
    public void clean() {
        testPublisher.id = null;
        testPublisher.properties = null;
    }

    @Test(description = "test properties of exchange deleted event publish", dataProvider = "example exchanges")
    public void testDeleteExchange(String exchangeName, String type, String durable)
            throws BrokerException, ValidationException {

        Exchange.Type exchangeType;
        if (type.equals("topic")) {
            exchangeType = Exchange.Type.TOPIC;
        } else {
            exchangeType = Exchange.Type.DIRECT;
        }
        observableExchangeRegistry.createExchange(exchangeName, exchangeType, Boolean.parseBoolean(durable));
        testPublisher.id = null;
        testPublisher.properties = null;
        observableExchangeRegistry.deleteExchange(exchangeName, true);
        observableExchangeRegistry.deleteExchange(exchangeName, true);
        Assert.assertEquals(exchangeName, testPublisher.getProperty("exchangeName"));
        Assert.assertEquals(type, testPublisher.getProperty("type"));
        Assert.assertEquals(durable, testPublisher.getProperty("durable"));
    }

    @Test(description = "test exchange create event with passive bit set false",
            expectedExceptions = ValidationException.class)
    public void testExistingExchangeDeclare() throws BrokerException,
            ValidationException {
            String exchangeName = "test1";
            observableExchangeRegistry.createExchange(exchangeName, Exchange.Type.TOPIC, false);
            testPublisher.id = null;
            observableExchangeRegistry.declareExchange(exchangeName, "topic", false, false);
            Assert.assertNull(testPublisher.id);
            observableExchangeRegistry.deleteExchange(exchangeName, true);
    }

    @Test(description = "test exchange create event with passive bit set true")
    public void testNonPassiveExistingExchangeDeclare() throws BrokerException,
            ValidationException {
        String exchangeName = "test2";
        observableExchangeRegistry.createExchange(exchangeName, Exchange.Type.TOPIC, false);
        testPublisher.id = null;
        observableExchangeRegistry.declareExchange(exchangeName, "topic", true, false);
        Assert.assertNull(testPublisher.id);
        observableExchangeRegistry.deleteExchange(exchangeName, true);
    }

    @Test(description = "Test to check exchange declared when there is no exchange")
    public void testNonPassiveNonExistingExchangeDeclare() throws BrokerException,
            ValidationException {
        String exchangeName = "test3";
        testPublisher.id = null;
            observableExchangeRegistry.declareExchange(exchangeName, "topic", false, false);
                Assert.assertNotNull(testPublisher.id);
        observableExchangeRegistry.deleteExchange(exchangeName, true);

    }

    @Test(description = "Test to check exchange declared when there is no exchange", expectedExceptions =
            ValidationException.class)
    public void testPassiveNonExistingExchangeDeclare() throws BrokerException,
            ValidationException {
        testPublisher.id = null;
        String exchangeName = "test4";
        observableExchangeRegistry.declareExchange(exchangeName, "topic", true, false);
        Assert.assertNotNull(testPublisher.id);
        observableExchangeRegistry.deleteExchange(exchangeName, true);
    }

    @Test(description = "Test to check exchange declared when there is no exchange", expectedExceptions =
            ValidationException.class)
    public void testExchangeDeclareWithNullName() throws BrokerException, ValidationException {
            String exchangeName = "";
            observableExchangeRegistry.declareExchange(exchangeName, "topic", true, false);
            Assert.assertNull(testPublisher.id);
    }

    @Test(description = "test properties of exchange created event publish", dataProvider = "example exchanges")
    public void testAddExchange(String exchangeName, String type, String durable)
            throws BrokerException, ValidationException {
        Exchange.Type exchangeType;
        if (type.equals("topic")) {
            exchangeType = Exchange.Type.TOPIC;
        } else {
            exchangeType = Exchange.Type.DIRECT;
        }
        observableExchangeRegistry.createExchange(exchangeName, exchangeType, Boolean.parseBoolean(durable));
        Assert.assertEquals(exchangeName, testPublisher.getProperty("exchangeName"));
        Assert.assertEquals(type, testPublisher.getProperty("type"));
        Assert.assertEquals(durable, testPublisher.getProperty("durable"));
        observableExchangeRegistry.deleteExchange(exchangeName, true);
    }

    @DataProvider(name = "example exchanges")
    public Object[][] exampleExchanges() {
        return new Object[][]{
                {"e1", "topic", "false"},
                {"e2", "direct", "false"},
                {"e3", "topic", "true"},
                {"e4", "direct", "true"}};
    }

    @DataProvider(name = "declare test parameters")
    public Object[][] declareInputs() {
        return new Object[][]{
                {"test1", true},
                {"test2", false}};
    }
}
