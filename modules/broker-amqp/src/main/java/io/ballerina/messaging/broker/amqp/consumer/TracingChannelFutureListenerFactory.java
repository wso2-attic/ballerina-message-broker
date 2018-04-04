package io.ballerina.messaging.broker.amqp.consumer;

import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.Message;
import io.netty.channel.ChannelFutureListener;

/**
 * Factory class for TracingChannelFutureListener.
 */
public class TracingChannelFutureListenerFactory implements ChannelFutureListenerFactory {
    private Broker broker;
    private final String queueName;
    private final int channelId;
    private final ShortString consumerTag;
    private final AmqpConsumer consumer;

    TracingChannelFutureListenerFactory(Broker broker,
                                        String queueName,
                                        int channelId,
                                        ShortString consumerTag,
                                        AmqpConsumer consumer) {
        this.broker = broker;
        this.queueName = queueName;
        this.channelId = channelId;
        this.consumerTag = consumerTag;
        this.consumer = consumer;
    }

    @Override
    public ChannelFutureListener createListener(Message message) {
        return new TracingChannelFutureListener(message, broker, queueName, channelId, consumerTag, consumer);
    }
}
