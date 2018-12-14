package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.eventingutil.TestPublisher;
import io.ballerina.messaging.broker.core.store.dao.BindingDao;
import io.ballerina.messaging.broker.core.store.dao.ExchangeDao;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Objects;


public class ExchangeRegistryFactoryTest {

    private BrokerCoreConfiguration.EventConfig eventConfig;
    @BeforeClass
    public void setup() {
        eventConfig = new BrokerCoreConfiguration().getEventConfig();
    }
    @Test(dataProvider = "sample publishers")
    public void testGetExchangeRegistry(TestPublisher testPublisher, boolean enabled) throws BrokerException,
            ValidationException {
        eventConfig.setExchangeAdminEventsEnabled(enabled);
        ExchangeRegistryFactory exchangeRegistryFactory = new ExchangeRegistryFactory(Mockito.mock(ExchangeDao.class),
                Mockito.mock(BindingDao.class),
                testPublisher,
                new BrokerCoreConfiguration().getEventConfig());
        ExchangeRegistry exchangeRegistry = exchangeRegistryFactory.getExchangeRegistry();
        exchangeRegistry.createExchange("test", Exchange.Type.TOPIC, false);
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
