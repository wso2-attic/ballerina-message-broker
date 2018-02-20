/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.core.store.dao.BindingDao;
import io.ballerina.messaging.broker.core.store.dao.ExchangeDao;
import io.ballerina.messaging.broker.core.store.dao.impl.NoOpBindingDao;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry object which contains all the registered exchanges of the broker.
 */
public final class ExchangeRegistry {

    private static final String DIRECT = "amq.direct";

    private static final String TOPIC = "amq.topic";

    private static final String DEFAULT = "<<default>>";

    public static final String DEFAULT_DEAD_LETTER_EXCHANGE = "amq.dlx";

    private static final BindingDao NO_OP_BINDING_DAO = new NoOpBindingDao();

    private final Map<String, Exchange> exchangeMap;

    private final ExchangeDao exchangeDao;

    private final BindingDao bindingDao;

    private final Collection<Exchange> unmodifiableExchangesView;

    public ExchangeRegistry(ExchangeDao exchangeDao, BindingDao bindingDao) {
        exchangeMap = new ConcurrentHashMap<>(3);
        exchangeMap.put(DIRECT, new DirectExchange(DIRECT, bindingDao));
        exchangeMap.put(TOPIC, new TopicExchange(TOPIC, bindingDao));
        exchangeMap.put(DEFAULT, new DirectExchange(DEFAULT, bindingDao));
        exchangeMap.put(DEFAULT_DEAD_LETTER_EXCHANGE, new DirectExchange(DEFAULT_DEAD_LETTER_EXCHANGE, bindingDao));
        this.exchangeDao = exchangeDao;
        this.bindingDao = bindingDao;
        this.unmodifiableExchangesView = Collections.unmodifiableCollection(exchangeMap.values());
    }

    Exchange getExchange(String exchangeName) {
        return exchangeMap.get(exchangeName);
    }

    boolean deleteExchange(String exchangeName, boolean ifUnused) throws BrokerException, ValidationException {
        Exchange exchange = exchangeMap.get(exchangeName);
        if (Objects.isNull(exchange)) {
            return false;
        }
        if (!isBuiltInExchange(exchange)) {
            if (!ifUnused || exchange.isUnused()) {
                if (exchange.isDurable()) {
                    exchangeDao.delete(exchange);
                }
                exchangeMap.remove(exchangeName);
                return true;
            } else {
                throw new ValidationException("Cannot delete exchange. Exchange " + exchangeName + " has bindings.");
            }
        } else {
            throw new ValidationException("Cannot delete built in exchange '" + exchangeName + "'");
        }
    }

    private boolean isBuiltInExchange(Exchange exchange) {
        String name = exchange.getName();
        return DEFAULT.equals(name) || DIRECT.equals(name) || TOPIC.equals(name);
    }

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
    void declareExchange(String exchangeName, String type,
                         boolean passive, boolean durable) throws ValidationException, BrokerException {
        if (exchangeName.isEmpty()) {
            throw new ValidationException("Exchange name cannot be empty.");
        }

        Exchange exchange = exchangeMap.get(exchangeName);
        if (passive) {
            if (Objects.isNull(exchange)) {
                throw new ValidationException("Exchange [ " + exchangeName + " ] doesn't exists. Passive parameter "
                                                      + "is set, hence not creating the exchange.");
            }
        } else {
            createExchange(exchangeName, Exchange.Type.from(type), durable);
        }
    }

    /**
     * Creates a exchange with given parameters.
     *
     * @param exchangeName name of the exchange
     * @param type {@link io.ballerina.messaging.broker.core.Exchange.Type} of the exchange
     * @param durable durability of the exchange
     * @throws BrokerException if there is an internal error when creating the exchange.
     * @throws ValidationException if exchange already exist
     */
    public void createExchange(String exchangeName, Exchange.Type type, boolean durable) throws BrokerException,
                                                                                                ValidationException {
        Exchange exchange = exchangeMap.get(exchangeName);
        if (Objects.isNull(exchange)) {
            BindingDao dao = durable ? bindingDao : NO_OP_BINDING_DAO;
            exchange = ExchangeFactory.newInstance(exchangeName, type, dao);
            exchangeMap.put(exchange.getName(), exchange);
            if (durable) {
                exchangeDao.persist(exchange);
            }
        } else {
            throw new ValidationException("Exchange [ " + exchangeName + " ] already exists.");
        }
    }

    private void retrieveAllExchangesFromDao() throws BrokerException {
        exchangeDao.retrieveAll(
                (name, typeString) -> {
                    Exchange exchange = ExchangeFactory.newInstance(name,
                                                                    Exchange.Type.from(typeString), bindingDao);
                    exchangeMap.putIfAbsent(name, exchange);
                });
    }

    public Exchange getDefaultExchange() {
        return exchangeMap.get(DEFAULT);
    }

    public void retrieveFromStore(QueueRegistry queueRegistry) throws BrokerException {
        retrieveAllExchangesFromDao();
        for (Exchange exchange : exchangeMap.values()) {
            exchange.retrieveBindingsFromDb(queueRegistry);
        }
    }

    public Collection<Exchange> getAllExchanges() {
        return unmodifiableExchangesView;
    }

    /**
     * Method to reload exchanges and bindings from the database on becoming the active node.
     *
     * @param queueRegistry the queue registry object
     * @throws BrokerException if an error occurs retrieving exchanges/bindings from the database
     */
    void reloadExchangesOnBecomingActive(QueueRegistry queueRegistry) throws BrokerException {
        exchangeMap.clear();
        retrieveFromStore(queueRegistry);
    }

    /**
     * Factory class to create the relevant exchange for the requested exchange type.
     */
    public static class ExchangeFactory {

        private ExchangeFactory() {
        }

        public static Exchange newInstance(String exchangeName, Exchange.Type type,
                                           BindingDao bindingDao) throws BrokerException {
            Exchange exchange;
            switch (type) {
                case DIRECT:
                    exchange = new DirectExchange(exchangeName, bindingDao);
                    break;
                case TOPIC:
                    exchange = new TopicExchange(exchangeName, bindingDao);
                    break;
                default:
                    throw new BrokerException("Unknown exchange type [ " + type + " ].");
            }
            return exchange;
        }
    }
}
