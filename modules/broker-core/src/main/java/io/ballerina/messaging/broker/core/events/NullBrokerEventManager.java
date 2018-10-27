package io.ballerina.messaging.broker.core.events;

import io.ballerina.messaging.broker.core.QueueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Null object implementation of {@link BrokerEventManager}.
 */
public class NullBrokerEventManager implements BrokerEventManager {

    private static final Logger logger = LoggerFactory.getLogger(NullBrokerEventManager.class);

    public  NullBrokerEventManager() {
        logger.info("NullEventManager Started");
    }

    public void queueCreated(QueueHandler queueHandler) {
        //No implementation
    }

}
