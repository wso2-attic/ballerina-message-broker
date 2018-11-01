package io.ballerina.messaging.broker.eventing;

import io.ballerina.messaging.broker.common.BrokerClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to load registered EventPublishers.
 */
public class EventRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventRegistry.class);

    /**
     * Provides an instance of @{@link EventSync}.
     *
     * @param eventConfiguration the event configuration
     * @return publisher for given configuration
     */
    EventSync getPublisher(EventConfiguration eventConfiguration) throws Exception {
        EventSync publisher;
        String publisherClass = eventConfiguration.getPublisherClassName();
        LOGGER.info("Initializing Event Publisher: {}", publisherClass);
        publisher = BrokerClassLoader.loadClass(publisherClass, EventSync.class);
        return publisher;
    }

}
