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

import io.ballerina.messaging.broker.auth.BrokerAuthConfiguration;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.config.BrokerCommonConfiguration;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public class BrokerAPITest {

    private Broker broker;

    private static final String DEFAULT_QUEUE_NAME = "TestQueue";

    private static final String DEFAULT_EXCHANGE_NAME = "amq.direct";

    private static final String DEFAULT_ROUTING_KEY = "TestQueue";

    @BeforeClass
    public void beforeTest() throws Exception {
        DataSource dataSource = DbUtil.getDataSource();
        StartupContext startupContext = new StartupContext();
        TestBrokerConfigProvider testBrokerConfigProvider = new TestBrokerConfigProvider();
        testBrokerConfigProvider.addConfigObject(BrokerCoreConfiguration.NAMESPACE, new BrokerCoreConfiguration());
        BrokerAuthConfiguration brokerAuthConfiguration = new BrokerAuthConfiguration();
        BrokerCommonConfiguration brokerCommonConfiguration = new BrokerCommonConfiguration();
        brokerAuthConfiguration.getAuthentication().setEnabled(false);
        brokerAuthConfiguration.getAuthorization().setEnabled(false);
        testBrokerConfigProvider.addConfigObject(BrokerCommonConfiguration.NAMESPACE, brokerCommonConfiguration);
        testBrokerConfigProvider.addConfigObject(BrokerAuthConfiguration.NAMESPACE, brokerAuthConfiguration);

        startupContext.registerService(BrokerConfigProvider.class, testBrokerConfigProvider);
        startupContext.registerService(DataSource.class, dataSource);
        broker = new BrokerImpl(startupContext);
    }

    @BeforeMethod
    public void setup() throws BrokerException, ValidationException {
        broker.createQueue(DEFAULT_QUEUE_NAME, false, false, false);
        broker.bind(DEFAULT_QUEUE_NAME, DEFAULT_EXCHANGE_NAME, DEFAULT_ROUTING_KEY, FieldTable.EMPTY_TABLE);
    }

    @AfterMethod
    public void tearDown() throws BrokerException, ValidationException {
        broker.unbind(DEFAULT_QUEUE_NAME, DEFAULT_EXCHANGE_NAME, DEFAULT_ROUTING_KEY);

    }

    @Test (description = "Test multiple identical binding calls for the same queue. This shouldn't throw any errors")
    public void testMultipleIdenticalBindingsForTheSameQueue() throws BrokerException, ValidationException {
        broker.bind(DEFAULT_QUEUE_NAME, DEFAULT_EXCHANGE_NAME, DEFAULT_ROUTING_KEY, FieldTable.EMPTY_TABLE);
        broker.bind(DEFAULT_QUEUE_NAME, DEFAULT_EXCHANGE_NAME, DEFAULT_ROUTING_KEY, FieldTable.EMPTY_TABLE);
    }

    @Test (dataProvider = "nonExistingQueues", description = "Test bind operation with non existing queues"
            , expectedExceptions = ValidationException.class)
    public void testNegativeBindWithNonExistingQueue(String queueName) throws Exception {
        broker.bind(queueName, DEFAULT_EXCHANGE_NAME, DEFAULT_ROUTING_KEY, FieldTable.EMPTY_TABLE);
    }

    @Test (dataProvider = "nonExistingExchanges", description = "Test bind operation with non existing queues"
            , expectedExceptions = ValidationException.class)
    public void testNegativeBindWithNonExistingExchange(String exchangeName) throws Exception {
        broker.bind(DEFAULT_QUEUE_NAME, exchangeName, DEFAULT_ROUTING_KEY, FieldTable.EMPTY_TABLE);
    }

    @Test (dataProvider = "nonExistingExchanges"
            , description = "Test multiple bind operation for the same queue with identical bindings"
            , expectedExceptions = ValidationException.class)
    public void testNegativeUnbindWithNonExistingExchangeTest(String exchangeName)
            throws BrokerException, ValidationException {
        broker.unbind(DEFAULT_QUEUE_NAME, exchangeName, DEFAULT_ROUTING_KEY);
    }

    @Test (dataProvider = "nonExistingQueues", description = "Test unbind operation with non existing queues"
            , expectedExceptions = ValidationException.class)
    public void testNegativeUnbindWithNonExistingQueueTest(String queueName) throws BrokerException,
                                                                                    ValidationException {
        broker.unbind(queueName, DEFAULT_EXCHANGE_NAME, DEFAULT_ROUTING_KEY);
    }

    @Test (dataProvider = "nonExistingQueues",
           description = "Test non existing queue delete.",
           expectedExceptions = {ResourceNotFoundException.class})
    public void testNonExistingQueueDelete(String queueName)
            throws BrokerException, ResourceNotFoundException, ValidationException {
        broker.deleteQueue(queueName, false, false);
    }

    @Test (dataProvider = "nonExistingExchanges",
           description = "Test non existing exchange delete. This shouldn't throw an exception")
    public void testNonExistingExchangeDelete(String exchangeName)
            throws BrokerException, ValidationException, ResourceNotFoundException {
        broker.deleteExchange(exchangeName, false);
    }

    @DataProvider(name = "nonExistingExchanges")
    public Object[] nonExistingExchanges() {
        return new Object[]{ "myExchange", "testExchange" };
    }

    @DataProvider(name = "nonExistingQueues")
    public Object[] nonExistingQueues() {
        return new Object[]{ "myQueue", "invalidQueue" };
    }

    private static class TestBrokerConfigProvider implements BrokerConfigProvider {
        Map<String, Object> configMap = new HashMap<>();

        @Override
        public <T> T getConfigurationObject(String namespace, Class<T> configurationClass) {
            Object configObject = configMap.get(namespace);
            return configurationClass.cast(configObject);
        }

        private void addConfigObject(String namespace, Object configObject) {
            configMap.put(namespace, configObject);
        }
    }
}
