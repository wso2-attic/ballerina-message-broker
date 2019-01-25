package io.ballerina.messaging.broker.core.eventpublisher;

import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Metadata;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class DefaultExchangePublisherTest {

    private ExchangePublisher exchangePublisher;
    private TestBroker testBroker;

    @BeforeMethod
    public void setUp() {
        this.testBroker = new TestBroker();
        exchangePublisher = new DefaultExchangePublisher(testBroker);
    }

    @AfterMethod
    public void tearDown() {
        this.testBroker = null;
        exchangePublisher = null;
    }

    @Test(dataProvider = "example notifications")
    public void testPublishNotification(String key, String property1, String value1, String property2, String value2) {

        Map<String, String> properties = new HashMap<>();
        properties.put(property1, value1);
        properties.put(property2, value2);
        exchangePublisher.publishNotification(key, properties);
        Message message = testBroker.getMessage();
        Metadata metadata = message.getMetadata();
        Assert.assertEquals(metadata.getRoutingKey(), key, "Invalid routing key");
        Assert.assertEquals(metadata.getHeader(ShortString.parseString(property1)).toString(), value1,
                "Invalid message property");
        Assert.assertEquals(metadata.getHeader(ShortString.parseString(property2)).toString(), value2,
                "Invalid message property");
    }

    @Test(description = "Test unable to publish message to broker with BrokerException")
    public void testPublishNotificationWithException() throws BrokerException {
        Broker broker = Mockito.mock(Broker.class);
        doThrow(BrokerException.class).when(broker).publish(any(Message.class));
        ExchangePublisher exchangePublisher = new DefaultExchangePublisher(broker);
        Map<String, String> properties = new HashMap<>();
        properties.put("testProperty", "testValue");
        exchangePublisher.publishNotification("test", properties);
    }

    @DataProvider(name = "example notifications")
    public Object[][] exampleNotifications() {
        return new Object[][] {
            {"testing.test1", "Name", "Test1", "Value", "10"},
            {"testing.test2", "Name", "Test2", "Value", "12"}
        };
    }

}
