package io.ballerina.messaging.broker.amqp.consumer;

import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Message;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;

/**
 * Handle Error related to AMQP consumer.
 */
class ConsumerErrorHandler implements ChannelFutureListener {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerErrorHandler.class);

    private Broker broker;
    private final String queueName;
    private Message message;

    ConsumerErrorHandler(Broker broker, String queueName, Message message) {
        this.broker = broker;
        this.queueName = queueName;
        this.message = message;
    }

    @Override
    public void operationComplete(ChannelFuture future) {
        if (!future.isSuccess()) {
            Throwable cause = future.cause();
            LOGGER.warn("Error while sending message for " + queueName, cause);
            if (cause instanceof ClosedChannelException) {
                requeueMessage();
            }
        }
    }

    private void requeueMessage() {
        try {
            broker.requeue(queueName, message);
        } catch (BrokerException | ResourceNotFoundException e) {
            // Ideally, we shouldn't get an exception requeueing a delivery failed message.
            LOGGER.warn("Message " + message.getInternalId() + " requeueing failed for " + queueName, e);
        }
    }
}
