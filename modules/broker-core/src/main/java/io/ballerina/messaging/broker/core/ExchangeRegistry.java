package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.ValidationException;
import java.util.Collection;

/**
 * Abstract class for Exchange Registry objects.
 */
public abstract class ExchangeRegistry {

    abstract Exchange getExchange(String exchangeName);

    abstract boolean deleteExchange(String exchangeName, boolean ifUnused) throws BrokerException, ValidationException;

    /**
     * Method to create or check for existence of an exchange. If passive boolean is set exchange existence can be
     * checked.
     *
     * @param exchangeName name of the exchange
     * @param type         type of the exchange
     * @param passive      if true do not create exchange
     * @param durable      is the exchange durable or not.
     * @throws BrokerException on exchange creation failure
     */
    abstract void declareExchange(String exchangeName, String type,
                            boolean passive, boolean durable) throws ValidationException, BrokerException;

    /**
     * Creates a exchange with given parameters.
     *
     * @param exchangeName name of the exchange
     * @param type         {@link io.ballerina.messaging.broker.core.Exchange.Type} of the exchange
     * @param durable      durability of the exchange
     * @throws BrokerException     if there is an internal error when creating the exchange.
     * @throws ValidationException if exchange already exist
     */
    public abstract void createExchange(String exchangeName, Exchange.Type type, boolean durable)
            throws BrokerException, ValidationException;

    public abstract Exchange getDefaultExchange();

    abstract void retrieveFromStore(QueueRegistry queueRegistry) throws BrokerException;

    public abstract Collection<Exchange> getAllExchanges();

    /**
     * Method to reload exchanges and bindings from the database on becoming the active node.
     *
     * @param queueRegistry the queue registry object
     * @throws BrokerException if an error occurs retrieving exchanges/bindings from the database
     */
    abstract void reloadExchangesOnBecomingActive(QueueRegistry queueRegistry) throws BrokerException;

}
