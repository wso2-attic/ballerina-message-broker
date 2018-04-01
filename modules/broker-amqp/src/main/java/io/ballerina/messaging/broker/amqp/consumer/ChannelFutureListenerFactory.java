package io.ballerina.messaging.broker.amqp.consumer;

import io.ballerina.messaging.broker.core.Message;
import io.netty.channel.ChannelFutureListener;

/**
 * Interface used in ChannelFutureListenerFactory implementations.
 */
public interface ChannelFutureListenerFactory {
    ChannelFutureListener createListener(Message message);
}
