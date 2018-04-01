package io.ballerina.messaging.broker.amqp.consumer;

import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.Message;
import io.netty.channel.ChannelFutureListener;

/**
 * Factory used to create ConsumerErrorHandler objects.
 */
public class ConsumerErrorHandlerFactory implements ChannelFutureListenerFactory {
    private String queueName;
    private Broker broker;

    ConsumerErrorHandlerFactory(Broker broker, String queueName) {
        this.queueName = queueName;
        this.broker = broker;
    }

    @Override
    public ChannelFutureListener createListener(Message message) {
        return new ConsumerErrorHandler(broker, queueName, message);
    }
}
