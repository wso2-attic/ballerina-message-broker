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
import io.ballerina.messaging.broker.core.store.dao.impl.NoOpBindingDao;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.messaging.broker.core.Exchange.Type.DIRECT;

/**
 * Tests the functionality for {@link #exchangeRegistry}.
 */
public class ExchangeRegistryTest {

    private static final String NON_EXISTING_EXCHANGE = "non-existing-exchange";

    private ExchangeRegistry exchangeRegistry;

    @BeforeMethod
    public void beforeTestSetup() {
        exchangeRegistry = new ExchangeRegistry(new NoOpExchangeDaoTestUtil(), new NoOpBindingDao());
    }

    @Test(dataProvider = "exchangeNames", description = "test frequently used exchanges types are defined")
    public void testGetExchanges(String exchangeName) {

        Exchange exchange = exchangeRegistry.getExchange(exchangeName);

        Assert.assertNotNull(exchange, "unable to find the exchange");
        Assert.assertEquals(exchange.getName(), exchangeName, "invalid exchange returned");

    }

    @Test(description = "Test frequently used exchanges types are defined",
          expectedExceptions = ValidationException.class)
    public void testDeclareExchangesWithEmptyName() throws ValidationException, BrokerException {
        exchangeRegistry.declareExchange("", DIRECT.toString(), false, true);
    }

    @Test(dataProvider = "exchangeNames", description = "declare a existing exchange with passive parameter set")
    public void testDeclareExistingExchangesWithPassiveParameter(String exchangeName) throws BrokerException,
                                                                                             ValidationException {
        // This should not throw any exception. durable and type params should be ignored.
        exchangeRegistry.declareExchange(exchangeName, null, true, true);
        exchangeRegistry.declareExchange(exchangeName, "", true, false);
        exchangeRegistry.declareExchange(exchangeName, DIRECT.toString(), true, true);

    }

    @Test(description = "Declare a non-existing exchange with passive parameter set",
          expectedExceptions = ValidationException.class)
    public void testDeclareNonExistingExchangesWithPassiveParameter() throws BrokerException, ValidationException {

        // This should throw an exception.
        exchangeRegistry.declareExchange(NON_EXISTING_EXCHANGE, DIRECT.toString(), true, false);
        Assert.fail("Declaring a non existing exchange with passive parameter set should throw a error.");

    }

    @Test(description = "Declare a non-existing exchange without passive parameter set")
    public void testDeclareNonExistingExchangesWithoutPassiveParameter() throws BrokerException, ValidationException {

        // This should not throw an exception.
        exchangeRegistry.declareExchange(NON_EXISTING_EXCHANGE, DIRECT.toString(), false, false);

        Exchange declaredExchange = exchangeRegistry.getExchange(NON_EXISTING_EXCHANGE);

        Assert.assertNotNull(declaredExchange, "declared exchange cannot be retreived");
    }

    
    @Test(description = "Test exchange delete operation")
    public void testDeleteExchange() throws BrokerException, ValidationException {

        // This should not throw an exception.
        exchangeRegistry.declareExchange(NON_EXISTING_EXCHANGE, DIRECT.toString(), false, false);
        
        exchangeRegistry.deleteExchange(NON_EXISTING_EXCHANGE, true);
        
        Exchange deletedExchange = exchangeRegistry.getExchange(NON_EXISTING_EXCHANGE);
        
        Assert.assertNull(deletedExchange, "deleted exchange cannot exist");
        
    }

    @Test(dataProvider = "exchangeNames", description = "try to delete built in exchanges",
          expectedExceptions = ValidationException.class)
    public void testDeleteBuiltInExchanges(String exchangeName) throws BrokerException, ValidationException {

        exchangeRegistry.deleteExchange(exchangeName,  true);
        Assert.fail("built in exchange type - " + exchangeName + " shouldn't be allowed to delete");

    }

    @AfterMethod
    public void tearDown() {
        exchangeRegistry = null;
    }

    @DataProvider(name = "exchangeNames")
    public Object[][] exchanges() {
        return new Object[][] { { "amq.direct" }, { "<<default>>" } };
    }


}
