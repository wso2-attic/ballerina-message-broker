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

import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.eventingutil.TestConsumer;
import io.ballerina.messaging.broker.core.eventingutil.TestPublisher;
import io.ballerina.messaging.broker.core.eventingutil.TestQueue;
import io.ballerina.messaging.broker.core.metrics.NullBrokerMetricManager;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.when;

public class QueueHandlerFactoryTest {

    private BrokerCoreConfiguration.QueueEvents queueEventConfiguration;
    private TestPublisher testPublisher;
    private BrokerCoreConfiguration config;


    @BeforeClass
    public void setup() {
        testPublisher = new TestPublisher();
        config = new BrokerCoreConfiguration();
        queueEventConfiguration = config.getEventConfig().getQueueLimitEvents();
    }

    @Test(description = "Test null and non null inputs", dataProvider = "Queue creation inputs")
    public void testCreateQueueHandler(List<Integer> commonLimits,
                                       List<BrokerCoreConfiguration.QueueEvents.QueueLimitEvent> queueLimitEvents,
                                       FieldTable arguments) throws BrokerException {
        QueueHandlerFactory queueHandlerFactory = new MemBackedQueueHandlerFactory(new NullBrokerMetricManager(),
                config, testPublisher);
        queueEventConfiguration.setCommonLimits(commonLimits);
        queueEventConfiguration.setQueues(queueLimitEvents);
        TestQueue queue = new TestQueue("test", false, true, 0);
        QueueHandler queueHandler = queueHandlerFactory.createQueueHandler(queue, new NullBrokerMetricManager(),
                arguments,
                config.getEventConfig());
        queueHandler.enqueue(Mockito.mock(Message.class));
    }

    @Test(description = "Test queue creation with consumer and binding events", dataProvider = "sample publishers")
    public void testCreateQueueHandler(TestPublisher testPublisher, boolean externalEventsEnabled) {
        config.getEventConfig().setQueueExternalEventsEnabled(externalEventsEnabled);
        QueueHandlerFactory queueHandlerFactory = new MemBackedQueueHandlerFactory(new NullBrokerMetricManager(),
                config, testPublisher);
        QueueHandler queueHandler = queueHandlerFactory.createQueueHandler(Mockito.mock(Queue.class),
                new NullBrokerMetricManager(),
                null,
                config.getEventConfig());
        if (Objects.nonNull(testPublisher) && externalEventsEnabled) {
            queueHandler.addConsumer(new TestConsumer(false, false, "test"));
            Assert.assertNotNull(testPublisher.id);
        } else {
            queueHandler.addConsumer(new TestConsumer(false, false, "test"));
        }
    }

    @Test(description = "Test queue creation with common message limits", dataProvider = "sample publishers")
    public void testCreateQueueHandlerWithCommonLimits(TestPublisher testPublisher, boolean enabled)
            throws BrokerException {
        queueEventConfiguration.setEnabled(enabled);
        List<Integer> limits = new ArrayList<>();
        limits.add(2);
        queueEventConfiguration.setCommonLimits(limits);
        Queue queue = Mockito.mock(Queue.class);
        QueueHandlerFactory queueHandlerFactory = new MemBackedQueueHandlerFactory(new NullBrokerMetricManager(),
                config, testPublisher);
        QueueHandler queueHandler = queueHandlerFactory.createQueueHandler(queue, new NullBrokerMetricManager(), null,
                config.getEventConfig());
        when(queue.size()).thenReturn(2);
        when(queue.enqueue(null)).thenReturn(true);
        queueHandler.enqueue(null);
        if (Objects.nonNull(testPublisher) && enabled) {
            Assert.assertNotNull(testPublisher.id);
        }
    }

    @Test(description = "Test queue creation with specific limits for a queue in config file ", dataProvider =
            "specific limit inputs")
    public void testCreateQueueHandlerWithSpecificLimits(String name, boolean enabled) throws BrokerException {
        testPublisher.id = null;
        config.getEventConfig().getQueueLimitEvents().setEnabled(enabled);
        List<Integer> limits = new ArrayList<>();
        limits.add(13);
        BrokerCoreConfiguration.QueueEvents.QueueLimitEvent queueLimitEvent =
                new BrokerCoreConfiguration.QueueEvents.QueueLimitEvent();
        queueLimitEvent.setName("test");
        queueLimitEvent.setLimits(limits);
        List<BrokerCoreConfiguration.QueueEvents.QueueLimitEvent> queueLimitEvents = new ArrayList<>();
        queueLimitEvents.add(queueLimitEvent);
        queueEventConfiguration.setQueues(queueLimitEvents);

        Queue testQueue = new TestQueue(name, false, true, 13);
        QueueHandlerFactory queueHandlerFactory = new MemBackedQueueHandlerFactory(new NullBrokerMetricManager(),
                config, testPublisher);
        QueueHandler queueHandler = queueHandlerFactory.createQueueHandler(testQueue, new NullBrokerMetricManager(),
                null,
                config.getEventConfig());
        queueHandler.enqueue(null);

        if (enabled && "test".equals(name)) {
            Assert.assertNotNull(testPublisher.id);
        } else {
            Assert.assertNull(testPublisher.id);
        }
    }

    @Test(description = "test QueueHandler with message limits in arguments")
    public void testQueueHandlerWithArgumentLimits() throws BrokerException {
        testPublisher.id = null;
        FieldTable arguments = new FieldTable();
        arguments.add(ShortString.parseString("x-queue-limits"), FieldValue.parseLongString("12,20"));
        config.getEventConfig().getQueueLimitEvents().setEnabled(true);
        Queue queue = Mockito.mock(Queue.class);
        QueueHandlerFactory queueHandlerFactory = new MemBackedQueueHandlerFactory(new NullBrokerMetricManager(),
                config, testPublisher);
        QueueHandler queueHandler = queueHandlerFactory.createQueueHandler(queue, new NullBrokerMetricManager(),
                arguments,
                config.getEventConfig());
        when(queue.size()).thenReturn(20);
        when(queue.enqueue(null)).thenReturn(true);
        queueHandler.enqueue(null);
        Assert.assertNotNull(testPublisher.id);
    }

    @DataProvider(name = "Queue creation inputs")
    public Object[][] queueInputs() {
        FieldTable arguments = new FieldTable();
        List<Integer> commonLimits = new ArrayList<>();
        commonLimits.add(11);
        List<BrokerCoreConfiguration.QueueEvents.QueueLimitEvent> queueLimitEvents = new ArrayList<>();
        BrokerCoreConfiguration.QueueEvents.QueueLimitEvent queueLimitEvent =
                new BrokerCoreConfiguration.QueueEvents.QueueLimitEvent();
        List<Integer> specificLimits = new ArrayList<>();
        specificLimits.add(11);
        queueLimitEvent.setName("test");
        queueLimitEvent.setLimits(specificLimits);

        BrokerCoreConfiguration.QueueEvents.QueueLimitEvent queueLimitEvent2 =
                new BrokerCoreConfiguration.QueueEvents.QueueLimitEvent();
        List<Integer> specificLimits2 = new ArrayList<>();
        specificLimits2.add(10);

        queueLimitEvent2.setName("new");
        queueLimitEvent2.setLimits(specificLimits2);

        queueLimitEvents.add(queueLimitEvent);
        queueLimitEvents.add(queueLimitEvent2);
        arguments.add(ShortString.parseString("x-queue-limits"), FieldValue.parseLongString("12,11"));
        return new Object[][] {
                {null, null, null},
                {commonLimits, queueLimitEvents, arguments},
                {null, null, new FieldTable()},
                {null, queueLimitEvents, null},
                {queueLimitEvents, null, null}
        };
    }

    @DataProvider(name = "sample publishers")
    public Object[][] publishers() {
        return new Object[][] {
                {null, true},
                {new TestPublisher(), true},
                {null, false},
                {new TestPublisher(), false}};
    }

    @DataProvider(name = "specific limit inputs")
    public Object[][] specificLimitDetails() {
            return new Object[][]{
                    {"test", true},
                    {"test", false},
                    {"different", true},
                    {"different", false}
            };
    }
}
