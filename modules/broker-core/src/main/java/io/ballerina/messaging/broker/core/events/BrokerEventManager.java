package io.ballerina.messaging.broker.core.events;

import io.ballerina.messaging.broker.core.QueueHandler;

/**
 * BrokerEventManager handles all the events related to Broker class.
 */
public interface BrokerEventManager {

    void queueCreated(QueueHandler queueHandler);

}
