package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.core.store.dao.BindingDao;
import io.ballerina.messaging.broker.core.store.dao.ExchangeDao;
import io.ballerina.messaging.broker.eventing.EventSync;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an Exchange Registry which trigger events for the broker.
 */
public class ObservableExchangeRegistryImpl extends ExchangeRegistry {
    private ExchangeRegistryImpl exchangeRegistry;
    private EventSync eventSync;

    ObservableExchangeRegistryImpl(ExchangeDao exchangeDao,
                                   BindingDao bindingDao,
                                   EventSync eventSync) {
        this.exchangeRegistry = new ExchangeRegistryImpl(exchangeDao, bindingDao);
        this.eventSync = eventSync;
    }

    @Override
    public Exchange getExchange(String exchangeName) {
        return exchangeRegistry.getExchange(exchangeName);
    }

    @Override
    public boolean deleteExchange(String exchangeName, boolean ifUnused) throws BrokerException, ValidationException {
        Exchange exchange = exchangeRegistry.getExchange(exchangeName);
        boolean exchangeDeleted = exchangeRegistry.deleteExchange(exchangeName, ifUnused);
        if (exchangeDeleted) {
            publishExchangeEvent("exchange.deleted", exchange);
        }
        return exchangeDeleted;
    }

    @Override
    public void declareExchange(String exchangeName, String type,
                                   boolean passive, boolean durable) throws ValidationException, BrokerException {
        if (exchangeName.isEmpty()) {
            throw new ValidationException("Exchange name cannot be empty.");
        }

        Exchange exchange = exchangeRegistry.getExchange(exchangeName);
        if (passive) {
            if (Objects.isNull(exchange)) {
                throw new ValidationException("Exchange [ " + exchangeName + " ] doesn't exists. Passive parameter "
                        + "is set, hence not creating the exchange.");
            }
        } else {
            createExchange(exchangeName, Exchange.Type.from(type), durable);
        }
    }

    @Override
    public void createExchange(String exchangeName, Exchange.Type type, boolean durable)
            throws BrokerException, ValidationException {
        exchangeRegistry.createExchange(exchangeName, type, durable);
        publishExchangeEvent("exchange.created", exchangeRegistry.getExchange(exchangeName));
    }

    @Override
    public Exchange getDefaultExchange() {
        return exchangeRegistry.getDefaultExchange();
    }

    @Override
    public void retrieveFromStore(QueueRegistry queueRegistry) throws BrokerException {
        exchangeRegistry.retrieveFromStore(queueRegistry);
    }

    @Override
    public Collection<Exchange> getAllExchanges() {
        return exchangeRegistry.getAllExchanges();
    }

    @Override
    public void reloadExchangesOnBecomingActive(QueueRegistry queueRegistry) throws BrokerException {
        exchangeRegistry.retrieveFromStore(queueRegistry);
    }

    private void publishExchangeEvent(String id, Exchange exchange) {
        Map<String, String> properties = new HashMap<>();
        properties.put("exchangeName", exchange.getName());
        properties.put("type", exchange.getType().toString());
        properties.put("durable", String.valueOf(exchange.isDurable()));
        eventSync.publish(id, properties);
    }
}
