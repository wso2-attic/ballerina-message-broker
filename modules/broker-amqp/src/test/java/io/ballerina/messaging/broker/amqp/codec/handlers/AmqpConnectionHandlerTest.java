package io.ballerina.messaging.broker.amqp.codec.handlers;

import io.ballerina.messaging.broker.amqp.AmqpConnectionManager;
import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.amqp.codec.AmqpChannelFactory;
import io.ballerina.messaging.broker.amqp.metrics.AmqpMetricManager;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.core.Broker;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AmqpConnectionHandlerTest {

    AmqpConnectionHandler connectionHandler;

    AmqpChannel amqpChannel;

    @BeforeMethod
    public void setUp() {
        Broker broker = Mockito.mock(Broker.class);
        AmqpMetricManager metricManager = Mockito.mock(AmqpMetricManager.class);
        Mockito.doNothing().when(metricManager).incrementChannelCount();
        AmqpChannelFactory amqpChannelFactory = Mockito.mock(AmqpChannelFactory.class);
        amqpChannel = Mockito.mock(AmqpChannel.class);
        AmqpConnectionManager amqpConnectionManager = Mockito.mock(AmqpConnectionManager.class);
        connectionHandler = new AmqpConnectionHandler(metricManager, amqpChannelFactory, amqpConnectionManager);
        connectionHandler.attachBroker(broker);
        Mockito.when(amqpChannelFactory.createChannel(broker, 1, connectionHandler)).thenReturn(amqpChannel);
    }

    @Test
    public void testCloseConnection() throws Exception {

        int channelCount = 5;
        for (int i = 1; i <= channelCount; i++) {
            connectionHandler.createChannel(i);
        }

        //test force=false, used=false
        try {
            connectionHandler.closeConnection("something", false, false);
            Assert.fail("Expected ValidationException not thrown");
        } catch (ValidationException e) {
            Assert.assertEquals(e.getMessage(),
                                "Cannot close connection. " + channelCount + " active channels exist and used "
                                + "parameter is not set.");
        }
    }

    @Test
    public void testCloseChannel() throws Exception {

        int channelId = 1;
        int consumerCount = 5;
        connectionHandler.createChannel(channelId);
        Mockito.when(amqpChannel.getConsumerCount()).thenReturn(consumerCount);

        //test force=false, used=false
        try {
            connectionHandler.closeChannel(channelId, false, "something");
            Assert.fail("Expected ValidationException not thrown");
        } catch (ValidationException e) {
            Assert.assertEquals(e.getMessage(),
                                "Cannot close channel. " + consumerCount + " active consumers exist and used "
                                + "parameter is not set.");
        }
    }

}
