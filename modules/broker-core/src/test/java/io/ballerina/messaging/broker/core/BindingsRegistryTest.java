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
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.core.queue.MemQueueImpl;
import io.ballerina.messaging.broker.core.store.dao.impl.NoOpBindingDao;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Objects;

/**
 * Test binding registry bind and unbind operations.
 */
public class BindingsRegistryTest {

    private BindingsRegistry registry;

    @BeforeMethod
    public void setUp() {
        NoOpBindingDao dao = new NoOpBindingDao();
        DirectExchange exchange = new DirectExchange("TestExchange", dao);
        registry = new BindingsRegistry(exchange, dao);
    }

    @AfterMethod
    public void tearDown() {
        registry = null;
    }

    @Test(dataProvider = "InvalidBindingData", expectedExceptions = ValidationException.class)
    public void testMultipleBindCallsWithDifferentSelectors(String queueName,
                                                            String selectorOne, String selectorTwo) throws Exception {

        QueueHandler queueHandler = new QueueHandler(new MemQueueImpl(queueName, 2, false), null);

        registry.bind(queueHandler, queueName, getFieldTable(selectorOne));
        registry.bind(queueHandler, queueName, getFieldTable(selectorTwo));
    }

    @Test(dataProvider = "SimilarBindingData")
    public void testMultipleBindCallsWithSameArguments(String queueName,
                                                       String selectorOne, String selectorTwo) throws Exception {
        QueueHandler queueHandler = new QueueHandler(new MemQueueImpl(queueName, 2, false), null);
        // Bind with similar bindings twice.
        registry.bind(queueHandler, queueName, getFieldTable(selectorOne));
        registry.bind(queueHandler, queueName, getFieldTable(selectorTwo));

        // Test the bindings
        BindingSet bindingsForRoute = registry.getBindingsForRoute(queueName);
        Collection<Binding> bindings;
        if (Objects.isNull(selectorOne) || selectorOne.isEmpty()) {
            bindings = bindingsForRoute.getUnfilteredBindings();
        } else {
            bindings = bindingsForRoute.getFilteredBindings();
        }
        Assert.assertEquals(bindings.size(), 1);

        Binding binding = new Binding(queueHandler.getUnmodifiableQueue(), queueName, getFieldTable(selectorOne));
        Assert.assertTrue(bindings.contains(binding));
    }

    private FieldTable getFieldTable(String selector) {
        FieldTable fieldTable;
        if (Objects.isNull(selector)) {
            fieldTable = FieldTable.EMPTY_TABLE;
        } else {
            fieldTable = new FieldTable();
            fieldTable.add(Binding.JMS_SELECTOR_ARGUMENT, FieldValue.parseLongString(selector));
        }
        return fieldTable;
    }

    @DataProvider(name = "InvalidBindingData")
    public static Object[][] invalidBindingData() {
        return new Object[][]{
                {"TestQueue", "CorrelationId = 'testMessage'", ""},
                {"TestQueue", "", "MessageId = 'myId1234'"},
                {"MyQueue", "CorrelationId = 'testMessage'", ""},
                {"MyQueue", "", "MessageId = 'myId1234'"}
        };
    }

    @DataProvider(name = "SimilarBindingData")
    public static Object[][] equalBindingData() {
        return new Object[][]{
                {"TestQueue", "CorrelationId = 'testMessage'", "CorrelationId = 'testMessage'"},
                {"TestQueue", "", ""},
                {"MyQueue", "MessageId = 'testMessage'", "MessageId = 'testMessage'"},

                // JMS Selector can be set with empty string or not set at all. Both should be treated as selector is
                // not set
                {"MyQueue", "", null},
                {"MyQueue", null, ""},
                {"MyQueue", null, null},
                {"MyQueue", "", ""}
        };
    }
}
