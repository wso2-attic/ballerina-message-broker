package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.core.store.dao.BindingDao;
import io.ballerina.messaging.broker.core.store.dao.ExchangeDao;
import io.ballerina.messaging.broker.eventing.EventSync;

import java.util.Objects;

/**
 * Factory for creating exchange registry objects.
 */
public class ExchangeRegistryFactory {

    private final ExchangeDao exchangeDao;

    private final BindingDao bindingDao;

    private final EventSync eventSync;

    public ExchangeRegistryFactory(ExchangeDao exchangeDao, BindingDao bindingDao, EventSync eventSync) {

        this.exchangeDao = exchangeDao;
        this.bindingDao = bindingDao;
        this.eventSync = eventSync;
    }

    /**
     * Create a observable or a non observable exchange registry with the give arguments.
     * @return ExchangeRegistry object
     */
    public ExchangeRegistry getExchangeRegistry() {

        if (Objects.nonNull(this.eventSync)) {
            return new ExchangeRegistry(exchangeDao, bindingDao,
                    new ExchangeRegistry.DefaultExchangeRegistryEventPublisher(eventSync));
        } else {
            return new ExchangeRegistry(exchangeDao, bindingDao,
                    new ExchangeRegistry.NullExchangeRegistryEventPublisher());
        }
    }

}
