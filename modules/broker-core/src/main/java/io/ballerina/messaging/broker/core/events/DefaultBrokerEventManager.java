package io.ballerina.messaging.broker.core.events;

import io.ballerina.messaging.broker.common.EventConstants;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.QueueHandler;
import io.ballerina.messaging.broker.eventing.EventSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link BrokerEventManager}.
 */
public class DefaultBrokerEventManager implements BrokerEventManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBrokerEventManager.class);

    private EventSync eventSync;

    public DefaultBrokerEventManager(EventSync eventSync) {

        this.eventSync = eventSync;
        logger.info("Default Event Manager Declared");
    }

    public void messagePublished(Message message) {
        //No need
    }

    public  void queueCreated(QueueHandler queueHandler) {
        Map<String, String> properties = new HashMap<>();
        String queueName = queueHandler.getUnmodifiableQueue().getName();
        String isAutoDelete = String.valueOf(queueHandler.getUnmodifiableQueue().isAutoDelete());
        String isDurable = String.valueOf(queueHandler.getUnmodifiableQueue().isDurable());
        properties.put("Queue_Name", queueName);
        properties.put("isAutoDelete", isAutoDelete);
        properties.put("isDurable", isDurable);
        logger.info("Consumer Subscription Published");
        eventSync.publish(EventConstants.QUEUECREATED, properties);
    }

}
