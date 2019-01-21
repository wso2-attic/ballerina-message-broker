package io.ballerina.messaging.broker.core.eventpublisher;

import io.ballerina.messaging.broker.common.EventSync;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Objects;

public class BrokerCoreEventPublisherTest {

    private EventSync eventSync;
    private TestBroker broker;

    @BeforeClass
    public void setup() {
        broker = new TestBroker();
        this.eventSync = new BrokerCoreEventPublisher();
    }

    @Test(description = "test activating events",  dataProvider = "sample brokers")
    public void testActivate(TestBroker broker) {
        ((BrokerCoreEventPublisher) this.eventSync).setBroker(broker);
        eventSync.activate();
        eventSync.publish("test", new HashMap<>());
        if (Objects.nonNull(broker)) {
            Assert.assertNotNull(broker.getMessage());
        }
    }

    @Test(description = "Test deactivating events")
    public void testDeactivate() {
        ((BrokerCoreEventPublisher) this.eventSync).setBroker(broker);
        eventSync.deactivate();
        eventSync.publish("test", new HashMap<>());
            Assert.assertNull(broker.getMessage());
    }

    @AfterMethod
    public void cleanUp() {
        broker.clearMessage();
    }

    @DataProvider(name = "sample brokers")
    public Object[] sampleBrokers() {
        return new Object[] {broker, null};
    }
}
