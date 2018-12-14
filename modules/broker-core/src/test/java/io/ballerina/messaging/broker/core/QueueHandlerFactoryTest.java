package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.eventingutil.TestConsumer;
import io.ballerina.messaging.broker.core.eventingutil.TestPublisher;
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

    @Test(dataProvider = "Queue creation inputs")
    public void testCreateQueueHandler(List<Integer> commonLimits,
                                       List<BrokerCoreConfiguration.QueueEvents.QueueLimitEvent> queueLimitEvents,
                                       FieldTable arguments) {
        QueueHandlerFactory queueHandlerFactory = new MemBackedQueueHandlerFactory(new NullBrokerMetricManager(),
                config, testPublisher);
        queueEventConfiguration.setCommonLimits(commonLimits);
        queueEventConfiguration.setQueues(queueLimitEvents);
        queueHandlerFactory.createQueueHandler(Mockito.mock(Queue.class), new NullBrokerMetricManager(), arguments,
                config.getEventConfig());
    }

    @Test(dataProvider = "sample publishers")
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

    @Test(dataProvider = "sample publishers")
    public void testCreateQueueHandlerWithLimits(TestPublisher testPublisher, boolean enabled) throws BrokerException {
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
        queueLimitEvent.setLimits(specificLimits);
        arguments.add(ShortString.parseString("x-queue-limits"), FieldValue.parseLongString("12,11"));
        return new Object[][] {
                {null, null, null},
                {commonLimits, queueLimitEvents, arguments},
                {null, null, new FieldTable()}
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
}
