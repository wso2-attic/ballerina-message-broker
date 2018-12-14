package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.eventingutil.TestPublisher;
import io.ballerina.messaging.broker.core.metrics.NullBrokerMetricManager;
import io.ballerina.messaging.broker.core.store.dao.QueueDao;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Objects;

public class QueueRegistryFactoryTest {

    private BrokerCoreConfiguration.EventConfig eventConfig;
    @BeforeClass
    public void setup() {
        eventConfig = new BrokerCoreConfiguration().getEventConfig();
    }
    @Test(dataProvider = "sample publishers")
    public void testGetQueueRegistry(TestPublisher testPublisher, boolean enabled) throws BrokerException {
        eventConfig.setQueueAdminEventsEnabled(enabled);
        QueueRegistryFactory queueRegistryFactory = new QueueRegistryFactory(Mockito.mock(QueueDao.class),
                new MemBackedQueueHandlerFactory(new NullBrokerMetricManager(),
                        new BrokerCoreConfiguration(),
                        testPublisher),
                testPublisher,
                eventConfig);
        QueueRegistry queueRegistry = queueRegistryFactory.getQueueRegistry();
        queueRegistry.addQueue("test", false, false, false, null);
        if (Objects.nonNull(testPublisher) && enabled) {
            Assert.assertNotNull(testPublisher.id);
        }
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
