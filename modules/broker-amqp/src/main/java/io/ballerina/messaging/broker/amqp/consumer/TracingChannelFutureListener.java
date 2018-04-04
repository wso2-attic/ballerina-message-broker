package io.ballerina.messaging.broker.amqp.consumer;

import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.util.MessageTracer;
import io.ballerina.messaging.broker.core.util.TraceField;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Channel listener to trace errors with message written to the socket.
 */
class TracingChannelFutureListener implements ChannelFutureListener {

    private static final String TRANSPORT_DELIVERY_FAILURE = "Message delivery failed. AMQP transport error.";

    private static final String SENT_FROM_TRANSPORT = "Message sent from transport.";

    private final Message message;

    private List<TraceField> tracingProperties;
    private final AmqpConsumer consumer;
    private final ConsumerErrorHandler consumerErrorHandler;

    TracingChannelFutureListener(Message message,
                                 Broker broker,
                                 String queueName,
                                 int channelId,
                                 ShortString consumerTag,
                                 AmqpConsumer consumer) {
        this.message = message;
        this.tracingProperties = new ArrayList<>(2);
        tracingProperties.add(new TraceField(AmqpChannel.CHANNEL_ID_FIELD_NAME, channelId));
        tracingProperties.add(new TraceField(AmqpConsumer.CONSUMER_TAG_FIELD_NAME, consumerTag));
        this.consumer = consumer;
        consumerErrorHandler = new ConsumerErrorHandler(broker, queueName, message);
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) {
        consumerErrorHandler.operationComplete(channelFuture);

        if (channelFuture.isSuccess()) {
            MessageTracer.trace(message, consumer, SENT_FROM_TRANSPORT, tracingProperties);
        } else {
            MessageTracer.trace(message, consumer, TRANSPORT_DELIVERY_FAILURE, tracingProperties);
        }
    }
}
