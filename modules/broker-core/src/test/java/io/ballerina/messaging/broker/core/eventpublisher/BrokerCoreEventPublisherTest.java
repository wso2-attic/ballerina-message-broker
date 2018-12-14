package io.ballerina.messaging.broker.core.eventpublisher;

import io.ballerina.messaging.broker.common.EventSync;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;


public class BrokerCoreEventPublisherTest {

    private EventSync eventSync;
    private TestBroker broker;

    @BeforeClass
    public void setup() {

        broker = new TestBroker();
        this.eventSync = new BrokerCoreEventPublisher();
        ((BrokerCoreEventPublisher) this.eventSync).setBroker(broker);
    }

    @Test(description = "test activating events")
    public void testActivate() {
        eventSync.activate();
        eventSync.publish("test", new HashMap<>());
        Assert.assertNotNull(broker.getMessage());
    }

    @Test(description = "Test deactivating events")
    public void testDeactivate() {
        eventSync.deactivate();
        eventSync.publish("test", new HashMap<>());
        Assert.assertNull(broker.getMessage());
    }

    @AfterMethod
    public void cleanUp() {
        broker.clearMessage();
    }

    @AfterClass
    public void tearDown() {
        this.eventSync = null;
        this.broker = null;
    }
}
