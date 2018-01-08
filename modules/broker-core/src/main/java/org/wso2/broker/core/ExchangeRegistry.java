/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.core;

import org.wso2.broker.core.store.dao.BindingDao;
import org.wso2.broker.core.store.dao.ExchangeDao;
import org.wso2.broker.core.store.dao.NoOpBindingDao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry object which contains all the registered exchanges of the broker.
 */
final class ExchangeRegistry {

    private static final String DIRECT = "amq.direct";

    private static final String TOPIC = "amq.topic";

    private static final String DEFAULT = "<<default>>";

    private static final BindingDao NO_OP_BINDING_DAO = new NoOpBindingDao();

    private final Map<String, Exchange> exchangeMap;

    private final ExchangeDao exchangeDao;

    private final BindingDao bindingDao;

    ExchangeRegistry(ExchangeDao exchangeDao, BindingDao bindingDao) {
        exchangeMap = new ConcurrentHashMap<>(3);
        exchangeMap.put(DIRECT, new DirectExchange(DIRECT, bindingDao));
        exchangeMap.put(TOPIC, new TopicExchange(TOPIC, bindingDao));
        exchangeMap.put(DEFAULT, new DirectExchange(DEFAULT, bindingDao));
        this.exchangeDao = exchangeDao;
        this.bindingDao = bindingDao;

    }

    Exchange getExchange(String exchangeName) {
        return exchangeMap.get(exchangeName);
    }

    void deleteExchange(String exchangeName, boolean ifUnused) throws BrokerException {
        // TODO: ifUnused property
        Exchange exchange = exchangeMap.get(exchangeName);
        if (exchange == null) {
            return;
        }
        if (!isBuiltInExchange(exchange)) {
            exchangeMap.remove(exchangeName);
        } else {
            throw new BrokerException("Cannot delete built in exchange '" + exchangeName + "'");
        }
    }

    private boolean isBuiltInExchange(Exchange exchange) {
        String name = exchange.getName();
        return DEFAULT.equals(name) || DIRECT.equals(name) || TOPIC.equals(name);
    }

    /**
     * @param exchangeName name of the exchange
     * @param type         type of the exchange
     * @param passive      if true do not create exchange
     * @param durable      is the exchange durable or not.
     * @throws BrokerException throws on exchange creation failure
     */
    void declareExchange(String exchangeName, Exchange.Type type,
                         boolean passive, boolean durable) throws BrokerException {
        if (exchangeName.isEmpty()) {
            throw new BrokerException("Exchange name cannot be empty.");
        }

        Exchange exchange = exchangeMap.get(exchangeName);
        if (passive && exchange == null) {
            throw new BrokerException(
                    "Exchange [ " + exchangeName
                            + " ] doesn't exists. Passive parameter is set, hence not creating the exchange.");
        } else if (exchange == null) {
            BindingDao dao = durable ? bindingDao : NO_OP_BINDING_DAO;
            exchange = ExchangeFactory.newInstance(exchangeName, type, dao);
            exchangeMap.put(exchange.getName(), exchange);
            if (durable) {
                exchangeDao.persist(exchange);
            }
        } else if (!passive) {
            throw new BrokerException("Exchange [ " + exchangeName + " ] already exists.");
        } else if (exchange.getType() != type) {
            throw new BrokerException("Exchange type [ " + type + " ] does not match the existing one [ "
                    + exchange.getType() + " ].");
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
        for (Exchange exchange: exchangeMap.values()) {
            exchange.retrieveBindingsFromDb(queueRegistry);
        }
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
