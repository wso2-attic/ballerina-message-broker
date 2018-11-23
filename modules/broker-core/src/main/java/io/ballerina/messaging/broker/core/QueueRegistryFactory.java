package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.core.store.dao.QueueDao;
import io.ballerina.messaging.broker.eventing.EventSync;

import java.util.Objects;

/**
 * Factory for creating queue registry objects.
 */
public class QueueRegistryFactory {

    private QueueDao queueDao;
    private EventSync eventSync;
    private QueueHandlerFactory queueHandlerFactory;

    public QueueRegistryFactory(QueueDao queueDao, QueueHandlerFactory queueHandlerFactory, EventSync eventSync) {

        this.eventSync = eventSync;
        this.queueHandlerFactory = queueHandlerFactory;
        this.queueDao = queueDao;
    }

    public QueueRegistry getQueueRegistry() throws BrokerException {

        if (Objects.nonNull(eventSync)) {
            return new QueueRegistry(queueDao,
                                    queueHandlerFactory,
                                    new QueueRegistry.DefaultQueueRegistryEventPublisher(eventSync));
        } else {
            return new QueueRegistry(queueDao,
                                    queueHandlerFactory,
                                    new QueueRegistry.NullQueueRegistryEventPublisher());
        }
    }

}
