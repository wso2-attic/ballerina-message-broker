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

import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.eventingutil.TestPublisher;
import io.ballerina.messaging.broker.core.metrics.NullBrokerMetricManager;
import io.ballerina.messaging.broker.core.queue.MemQueueImpl;
import io.ballerina.messaging.broker.core.store.dao.QueueDao;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ObservableQueueRegistryImplTest {

    private TestPublisher testPublisher;
    private ObservableQueueRegistryImpl observableQueueRegistry;

    @BeforeClass
    public void setup() throws BrokerException {

        testPublisher = new TestPublisher();
        observableQueueRegistry = new ObservableQueueRegistryImpl(new QueueRegistryImpl(
                new ObservableQueueRegistryImplTest.TestQueueDao(),
                new ObservableQueueRegistryImplTest.TestQueueHandlerFactory()),
                testPublisher);
    }

    @Test(description = "test properties of queue event publish", dataProvider = "example queues")
    public void testAddQueue(String queueName, String durable, String autoDelete)
            throws BrokerException {

        observableQueueRegistry.addQueue(queueName, false, Boolean.valueOf(durable),
                Boolean.valueOf(autoDelete), null);
        Assert.assertEquals(testPublisher.getProperty("queueName"), queueName);
        Assert.assertEquals(testPublisher.getProperty("durable"), durable);
        Assert.assertEquals(testPublisher.getProperty("autoDelete"), autoDelete);
    }

    @Test(description = "test properties of queue event publish", dataProvider = "example queues")
    public void testRemoveQueue(String queueName, String durable, String autoDelete)
            throws BrokerException, ResourceNotFoundException, ValidationException {

        observableQueueRegistry.addQueue(queueName, false, Boolean.valueOf(durable),
                Boolean.valueOf(autoDelete), null);
        testPublisher.id = null;
        testPublisher.properties = null;
        observableQueueRegistry.removeQueue(queueName, true, true);
        Assert.assertEquals(testPublisher.getProperty("queueName"), queueName);
        Assert.assertEquals(testPublisher.getProperty("durable"), durable);
        Assert.assertEquals(testPublisher.getProperty("autoDelete"), autoDelete);
    }

    @DataProvider(name = "example queues")
    public Object[][] queueExamples() {

        return new Object[][]{
                {"q1", "true", "true"},
                {"q2", "true", "false"},
                {"q3", "false", "true"},
                {"q4", "false", "true"}};
    }

    private static class TestQueueHandlerFactory extends QueueHandlerFactory {

        TestQueueHandlerFactory() {

            super(new BrokerCoreConfiguration.QueueEvents(), null);
        }

        @Override
        public QueueHandlerImpl createDurableQueueHandler(String queueName, boolean autoDelete, FieldTable arguments) {

            Queue queue = new MemQueueImpl(queueName, true, 10000, autoDelete);
            return new QueueHandlerImpl(queue, new NullBrokerMetricManager());
        }

        @Override
        public QueueHandlerImpl createNonDurableQueueHandler(String queueName,
                                                             boolean autoDelete,
                                                             FieldTable arguments) {

            Queue queue = new MemQueueImpl(queueName, false, 10000, autoDelete);
            return new QueueHandlerImpl(queue, new NullBrokerMetricManager());
        }
    }

    private static class TestQueueDao implements QueueDao {

        @Override
        public void persist(Queue queue) {

        }

        @Override
        public void delete(Queue queue) {

        }

        @Override
        public void retrieveAll(QueueCollector queueNameCollector) {

        }
    }
}
