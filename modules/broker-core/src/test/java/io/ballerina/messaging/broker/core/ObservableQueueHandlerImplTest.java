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
import io.ballerina.messaging.broker.common.util.function.ThrowingConsumer;
import io.ballerina.messaging.broker.core.eventingutil.TestConsumer;
import io.ballerina.messaging.broker.core.eventingutil.TestPublisher;
import io.ballerina.messaging.broker.core.eventingutil.TestQueue;
import io.ballerina.messaging.broker.core.metrics.NullBrokerMetricManager;
import io.ballerina.messaging.broker.core.queue.MemQueueImpl;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.List;

public class ObservableQueueHandlerImplTest {

    private ObservableQueueHandlerImpl observableQueueHandler;
    private TestPublisher testPublisher;
    private Queue limitQueue;
    private Queue limitQueue2;
    private List<Integer> queueLimits;

    @BeforeClass
    public void setup() throws BrokerException {
        TestQueue testQueue = new TestQueue("TestQueue", false, false, 10);
        testPublisher = new TestPublisher();
        observableQueueHandler = new ObservableQueueHandlerImpl(
                new QueueHandlerImpl(testQueue, new NullBrokerMetricManager()),
                testPublisher);
        Queue testQueue1 = new MemQueueImpl("TestQueue", 16, false);

       //Queues to check message limit events
        queueLimits = new ArrayList<>();
        queueLimits.add(2);
        queueLimits.add(11);
        queueLimits.add(17);
        limitQueue = new ObservableQueue(testQueue1, testPublisher, queueLimits);
        new QueueHandlerImpl(limitQueue, new NullBrokerMetricManager());

        Queue testQueue2 = new MemQueueImpl("TestQueue", 1000, false);
        limitQueue2 = new ObservableQueue(testQueue2, testPublisher, queueLimits);
        new QueueHandlerImpl(limitQueue2, new NullBrokerMetricManager());

        Message message = Mockito.mock(Message.class);
        for (int i = 0; i < 15; i++) {
            limitQueue2.enqueue(message);
        }
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

    @Test(description = "test properties of consumer added event publish", dataProvider = "example consumers")
    public void testAddConsumer(boolean exclusive, boolean ready, String queueName) {
        Consumer consumer = new TestConsumer(exclusive, ready, queueName);
        observableQueueHandler.addConsumer(consumer);
        Assert.assertEquals(testPublisher.getProperty("consumerID"), String.valueOf(consumer.getId()),
                "Invalid event property");
        Assert.assertEquals(testPublisher.getProperty("exclusive"), String.valueOf(consumer.isExclusive()),
                "Invalid event property");
        Assert.assertEquals(testPublisher.getProperty("ready"), String.valueOf(consumer.isReady()),
                "Invalid event property");
        Assert.assertEquals(testPublisher.getProperty("queueName"), consumer.getQueueName(), "Invalid event Property");
    }

     @Test(description = "Test existing consumer addition")
     public void testExistingConsumerConsumerAdd() {
         Consumer consumer = new TestConsumer(true, false, "existing");
         observableQueueHandler.addConsumer(consumer);
         testPublisher.id = null;
         observableQueueHandler.addConsumer(consumer);
         Assert.assertNull(testPublisher.id, "Consumer already exists");
     }

     @Test(description = "Test non existing consumer removal")
     public void testNonExistingConsumerRemoval() {
        observableQueueHandler.removeConsumer(new TestConsumer(true, false, "non-existing"));
        Assert.assertNull(testPublisher.id, "Consumer does not exist");
     }


    @Test(description = "test properties of consumer removed event publish", dataProvider = "example consumers")
    public void testRemoveConsumer(boolean exclusive, boolean ready, String queueName) {
        Consumer consumer = new TestConsumer(exclusive, ready, queueName);
        observableQueueHandler.addConsumer(consumer);
        testPublisher.properties = null;
        observableQueueHandler.removeConsumer(consumer);
        Assert.assertEquals(testPublisher.getProperty("consumerID"), String.valueOf(consumer.getId()),
                "Invalid event Property");
        Assert.assertEquals(testPublisher.getProperty("exclusive"), String.valueOf(consumer.isExclusive()),
                "Invalid event Property");
        Assert.assertEquals(testPublisher.getProperty("ready"), String.valueOf(consumer.isReady()),
                "Invalid event Property");
        Assert.assertEquals(testPublisher.getProperty("queueName"), consumer.getQueueName(),
                "Invalid event Property");
        Assert.assertEquals(testPublisher.id, "consumer.removed", "Invalid event id");
    }

    @Test(description = "test properties of queue limit reached event publish", invocationCount = 17)
    public void testQueueLimitReachedPublishEvent() throws BrokerException {
        Message message = Mockito.mock(Message.class);
        limitQueue.enqueue(message);
        int queueSize = limitQueue.getQueueHandler().size();
        if (queueLimits.contains(queueSize)) {
            Assert.assertEquals(testPublisher.getProperty("queueName"), "TestQueue", "Invalid event Property");
            Assert.assertEquals(testPublisher.getProperty("durable"), String.valueOf(false), "Invalid event Property");
            Assert.assertEquals(testPublisher.getProperty("autoDelete"), String.valueOf(false),
                    "Invalid event Property");
            Assert.assertEquals(testPublisher.getProperty("messageCount"), String.valueOf(queueSize),
                    "Invalid event Property");
            Assert.assertEquals(testPublisher.id, "queue.publishLimitReached.TestQueue." + String.valueOf(queueSize),
                    "Invalid event Property");
        } else {
            Assert.assertNull(testPublisher.id, "Event published at an undefined limit");
        }
    }

    @Test(description = "test properties of queue limit reached event publish", invocationCount = 14)
    public void testQueueLimitReachedDeliveredEvent() {
        limitQueue2.dequeue();
        int queueSize = limitQueue2.getQueueHandler().size();
        if (queueLimits.contains(queueSize)) {
            Assert.assertEquals(testPublisher.getProperty("queueName"), "TestQueue", "Invalid event property");
            Assert.assertEquals(testPublisher.getProperty("durable"), String.valueOf(false), "Invalid event property");
            Assert.assertEquals(testPublisher.getProperty("autoDelete"), String.valueOf(false),
                    "Invalid event property");
            Assert.assertEquals(testPublisher.getProperty("messageCount"), String.valueOf(queueSize));
            Assert.assertEquals(testPublisher.id, "queue.deliverLimitReached.TestQueue." + String.valueOf(queueSize),
                    "Invalid event property");
        } else {
            Assert.assertNull(testPublisher.id, "Event published at an undefined limit");
        }
    }

    @Test(description = "test properties of binding event publish", dataProvider = "example bindings")
    public void testRemoveBinding(String queueName, String bindingPattern) throws BrokerException {

        TestQueue testQueue = new TestQueue(queueName, false, false, 10);
        FieldTable arguments = new FieldTable();
        Binding binding = new Binding(testQueue, bindingPattern, arguments);
        ThrowingConsumer<Binding, BrokerException> bindingChangeListener = Mockito.mock(ThrowingConsumer.class);
        observableQueueHandler.addBinding(binding, bindingChangeListener);
        observableQueueHandler.removeBinding(binding);
        Assert.assertEquals(testPublisher.getProperty("bindingQueue"), queueName, "Invalid event property");
        Assert.assertEquals(testPublisher.getProperty("bindingPattern"), bindingPattern, "Invalid event property");
        Assert.assertEquals(testPublisher.id, "binding.removed", "Invalid event id");
    }

    @Test(description = "test properties of consumer removed event publish", dataProvider = "example consumers")
    public void testReleaseResources(boolean exclusive, boolean ready, String queueName) throws BrokerException {

        TestQueue testQueue = new TestQueue("TestQueue", false, false, 10);
        ObservableQueueHandlerImpl newHandler = new ObservableQueueHandlerImpl(
                new QueueHandlerImpl(testQueue, new NullBrokerMetricManager()), testPublisher);
        Consumer consumer = new TestConsumer(exclusive, ready, queueName);
        newHandler.addConsumer(consumer);
        int consumerCount = newHandler.consumerCount();
        newHandler.releaseResources();
        Assert.assertEquals(consumerCount, 1);
        Assert.assertEquals(testPublisher.getProperty("consumerID"), String.valueOf(consumer.getId()),
                "Invalid event property");
        Assert.assertEquals(testPublisher.getProperty("exclusive"), String.valueOf(consumer.isExclusive()),
                "Invalid Event Property");
        Assert.assertEquals(testPublisher.getProperty("ready"), String.valueOf(consumer.isReady()),
                "Invalid Event Property");
        Assert.assertEquals(testPublisher.getProperty("queueName"), consumer.getQueueName(), "Invalid Event Property");
    }

    @Test(description = "test properties of binding event publish", dataProvider = "example bindings")
    public void testAddBinding(String queueName, String bindingPattern)
            throws BrokerException {
        TestQueue testQueue = new TestQueue(queueName, false, false, 10);
        FieldTable arguments = new FieldTable();
        Binding binding = new Binding(testQueue, bindingPattern, arguments);
        ThrowingConsumer<Binding, BrokerException> bindingChangeListener = Mockito.mock(ThrowingConsumer.class);
        observableQueueHandler.addBinding(binding, bindingChangeListener);
        Assert.assertEquals(testPublisher.getProperty("bindingQueue"), queueName, "Invalid Event Property");
        Assert.assertEquals(testPublisher.getProperty("bindingPattern"), bindingPattern, "Invalid Event Property");
        Assert.assertEquals(testPublisher.id, "binding.added", "Invalid Event ID");
        observableQueueHandler.removeBinding(binding);
    }

    @Test(description = "Observable Consumer equals testing")
    public void testObservableConsumerEquals() {
        Consumer consumer = new TestConsumer(false, false, "test");
        Consumer consumer1 = new TestConsumer(false, false, "test");
        Consumer observableConsumer1 = new ObservableConsumer(consumer, testPublisher);
        Consumer observableConsumer2 = new ObservableConsumer(consumer, testPublisher);
        Consumer observableConsumer3 = new ObservableConsumer(consumer1, testPublisher);

        Assert.assertEquals(observableConsumer1, observableConsumer1, "Consumer equality failed");
        Assert.assertEquals(observableConsumer1, observableConsumer2, "Consumer equality failed");
        Assert.assertNotEquals(observableConsumer1, observableConsumer3, "Consumers equality failed");
        Assert.assertEquals(observableConsumer1, consumer, "Consumers equality failed");
        Assert.assertNotEquals(observableConsumer1, consumer1, "Consumers equality failed");
        Assert.assertNotEquals(observableConsumer1, new Object(), "Consumer equals a different object");
        Assert.assertFalse(observableConsumer1.equals(new Object()), "Consumer equals a different object");
    }

    @DataProvider(name = "example consumers")
    public Object[][] consumerExamples() {
        return new Object[][]{
                {true, true, "q1"},
                {true, false, "q2"},
                {false, true, "q3"},
                {false, false, "q4"}};
    }

    @DataProvider(name = "example bindings")
    public Object[][] exampleBindings() {
        return new Object[][]{
                {"b1", "pattern1"},
                {"b2", "pattern2"},
                {"b3", "pattern3"},
                {"b4", "pattern4"}};
    }
}
