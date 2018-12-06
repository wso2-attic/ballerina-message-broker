package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.core.store.dao.QueueDao;
import io.ballerina.messaging.broker.eventing.EventSync;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an Queue Registry which trigger events for the broker.
 */
public class ObservableQueueRegistryImpl extends QueueRegistry {

    private QueueRegistryImpl queueRegistry;
    private EventSync eventSync;

    ObservableQueueRegistryImpl(QueueDao queueDao, QueueHandlerFactory queueHandlerFactory, EventSync eventSync)
            throws BrokerException {
        this.queueRegistry = new QueueRegistryImpl(queueDao, queueHandlerFactory);
        this.eventSync = eventSync;
    }

    @Override
    QueueHandler getQueueHandler(String queueName) {
        return queueRegistry.getQueueHandler(queueName);
    }
    @Override
    boolean addQueue(String queueName, boolean passive, boolean durable, boolean autoDelete,
                            FieldTable arguments) throws BrokerException {
        boolean queueAdded = queueRegistry.addQueue(queueName, passive, durable, autoDelete, arguments);
        if (queueAdded) {
            publishQueueEvent("queue.added", queueRegistry.getQueueHandler(queueName));
        }
        return queueAdded;
    }

    @Override
    int removeQueue(String queueName, boolean ifUnused, boolean ifEmpty) throws BrokerException,
            ValidationException, ResourceNotFoundException {

        QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);
        int releasedResources = queueRegistry.removeQueue(queueName, ifUnused, ifEmpty);
        publishQueueEvent("queue.deleted", queueHandler);
        return releasedResources;
    }

    @Override
    public Collection<QueueHandler> getAllQueues() {
        return queueRegistry.getAllQueues();
    }

    private void publishQueueEvent(String eventType, QueueHandler queueHandler) {
        Map<String, String> properties = new HashMap<>();
        String queueName = queueHandler.getUnmodifiableQueue().getName();
        String isAutoDelete = String.valueOf(queueHandler.getUnmodifiableQueue().isAutoDelete());
        String isDurable = String.valueOf(queueHandler.getUnmodifiableQueue().isDurable());
        properties.put("queueName", queueName);
        properties.put("autoDelete", isAutoDelete);
        properties.put("durable", isDurable);
        properties.put("messageCount", String.valueOf(queueHandler.size()));
        eventSync.publish(eventType, properties);
    }

    @Override
    void reloadQueuesOnBecomingActive() throws BrokerException {
        queueRegistry.reloadQueuesOnBecomingActive();
    }
}
