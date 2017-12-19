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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry object which contains all the registered exchanges of the broker.
 */
final class ExchangeRegistry {

    private static final String DIRECT = "amq.direct";

    private static final String TOPIC = "amq.topic";

    private static final String DEFAULT = "<<default>>";

    public static final Exchange DEFAULT_EXCHANGE = new DirectExchange(DEFAULT);

    private final Map<String, Exchange> exchangeMap;

    ExchangeRegistry() {
        exchangeMap = new ConcurrentHashMap<>(3);
        exchangeMap.put(DIRECT, new DirectExchange(DIRECT));
        exchangeMap.put(TOPIC, new TopicExchange(TOPIC));
        exchangeMap.put(DEFAULT, DEFAULT_EXCHANGE);
    }

    Exchange getExchange(String exchangeName) {
        return exchangeMap.get(exchangeName);
    }

    void deleteExchange(String exchangeName, Exchange.Type type, boolean ifUnused) throws BrokerException {
        // TODO: Go through the logic with exchange type in mind
        Exchange exchange = exchangeMap.get(exchangeName);
        if (exchange != null && type == exchange.getType() && !isBuiltInExchange(exchange)) {
            exchangeMap.remove(exchangeName);
        } else {
            throw new BrokerException("Cannot delete exchange.");
        }
    }

    private boolean isBuiltInExchange(Exchange exchange) {
        String name = exchange.getName();
        return DEFAULT.equals(name) || DIRECT.equals(name) || TOPIC.equals(name);
    }

    /**
     * TODO : behavior around durable is not implemented
     * TODO : Need to make this method synchronized since this can be called concurrently.
     *
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
            exchange = ExchangeFactory.newInstance(exchangeName, type);
            exchangeMap.put(exchange.getName(), exchange);
        } else if (!passive && exchange.getType() != type) { // TODO add durable check
            throw new BrokerException("Exchange [ " + exchangeName + " ] already exists.");
        } else if (exchange.getType() != type) {
            throw new BrokerException("Exchange type [ " + type + " ] does not match the existing one [ "
                                              + exchange.getType() + " ].");
        }
    }

    /**
     * Internal class to create the relevant exchange for the requested exchange type.
     */
    private static class ExchangeFactory {

        static Exchange newInstance(String exchangeName, Exchange.Type type) throws BrokerException {
            Exchange exchange;
            switch (type) {
                case DIRECT:
                    exchange = new DirectExchange(exchangeName);
                    break;
                case TOPIC:
                    exchange = new TopicExchange(exchangeName);
                    break;
                default:
                    throw new BrokerException("Unknown exchange type [ " + type + " ].");
            }

            return exchange;
        }
    }
}
