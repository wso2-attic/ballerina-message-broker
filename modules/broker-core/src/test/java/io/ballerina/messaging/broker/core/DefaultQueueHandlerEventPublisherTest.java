package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.core.metrics.NullBrokerMetricManager;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class DefaultQueueHandlerEventPublisherTest {

    private QueueHandler.QueueHandlerEventPublisher queueHandlerEventPublisher;
    private TestPublisher testPublisher;
    private int limit;

    @BeforeClass
    public void setup() {
        testPublisher = new TestPublisher();
        limit = 10;
        List<Integer> limits = new ArrayList<>();
        limits.add(limit);
        queueHandlerEventPublisher = new QueueHandler.DefaultQueueHandlerEventPublisher(testPublisher, limits);
    }

    @Test
    public void testConsumerEvent() {
        Consumer consumer = new TestConsumer();
        queueHandlerEventPublisher.publishConsumerEvent("test", consumer);
        Assert.assertEquals(testPublisher.getProperty("consumerID"), String.valueOf(consumer.getId()));
        Assert.assertEquals(testPublisher.getProperty("exclusive"), String.valueOf(consumer.isExclusive()));
        Assert.assertEquals(testPublisher.getProperty("ready"), String.valueOf(consumer.isReady()));
        Assert.assertEquals(testPublisher.getProperty("queueName"), consumer.getQueueName());
    }

    @Test
    public void testBindingEvent() throws BrokerException {
        TestQueue testQueue = new TestQueue("TestQueue", false, false, 10);
        FieldTable arguments = new FieldTable();
        Binding binding = new Binding(testQueue, "TestPattern", arguments);
        queueHandlerEventPublisher.publishBindingEvent("test", binding);
        Assert.assertEquals(testPublisher.getProperty("bindingQueue"), "TestQueue");
        Assert.assertEquals(testPublisher.getProperty("bindingPattern"), "TestPattern");
    }

    @Test(dataProvider = "queue sizes")
    public void testQueueLimitReachedEvent(int size) {
        testPublisher.id = null;
        TestQueue testQueue = new TestQueue("TestQueue", false, false, size);
        QueueHandler queueHandler = new QueueHandler(testQueue,
                                                    new NullBrokerMetricManager(),
                                                    new QueueHandler.NullQueueHandlerEventPublisher());
        queueHandlerEventPublisher.publishQueueLimitReachedEvent(queueHandler);
        if (queueHandler.size() == limit) {
            Assert.assertEquals(testPublisher.getProperty("queueName"), "TestQueue");
            Assert.assertEquals(testPublisher.getProperty("durable"), String.valueOf(false));
            Assert.assertEquals(testPublisher.getProperty("autoDelete"), String.valueOf(false));
            Assert.assertEquals(testPublisher.getProperty("messageCount"), String.valueOf(limit));
            Assert.assertEquals(testPublisher.id, "queue.limitReached.TestQueue." + limit);
        } else {
            Assert.assertNull(testPublisher.id);
        }

    }

    @AfterClass
    public void tearDown() {
        testPublisher = null;
        queueHandlerEventPublisher = null;
    }

    @DataProvider(name = "queue sizes")
    public Object[] queueExamples() {
        return new Object[] { 1, 2, 10, 1};
    }

}
