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

import org.wso2.broker.common.data.types.FieldTable;
import org.wso2.broker.core.store.dao.BindingDao;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the bindings for a given {@link Exchange}.
 * TODO why do we repeat routing key in two places (as key and as field in binding object)?
 * Feels like we need to refactor this class.
 */
final class BindingsRegistry {

    private final Map<String, BindingSet> routingKeyToBindingMap;

    private final Exchange exchange;

    private final BindingDao bindingDao;

    public BindingsRegistry(Exchange exchange, BindingDao bindingDao) {
        this.routingKeyToBindingMap = new HashMap<>();
        this.exchange = exchange;
        this.bindingDao = bindingDao;
    }

    void bind(Queue queue, String bindingKey, FieldTable arguments) throws BrokerException {
        BindingSet bindingSet = routingKeyToBindingMap.computeIfAbsent(bindingKey, k -> new BindingSet());
        Binding binding = new Binding(queue, bindingKey, arguments);
        boolean success = bindingSet.add(binding);
        if (success && queue.isDurable()) {
            bindingDao.persist(exchange.getName(), binding);
        }
    }

    void unbind(Queue queue, String routingKey) throws BrokerException {
        BindingSet bindingSet = routingKeyToBindingMap.get(routingKey);
        if (queue.isDurable()) {
            bindingDao.delete(queue.getName(), routingKey, exchange.getName());
        }
        bindingSet.remove(queue);

        if (bindingSet.isEmpty()) {
            routingKeyToBindingMap.remove(routingKey);
        }
    }

    BindingSet getBindingsForRoute(String routingKey) {
        BindingSet bindingSet = routingKeyToBindingMap.get(routingKey);
        if (bindingSet == null) {
            bindingSet = BindingSet.emptySet();
        }
        return bindingSet;
    }

    boolean isEmpty() {
        return routingKeyToBindingMap.isEmpty();
    }

    public void retrieveAllBindingsForExchange(QueueRegistry queueRegistry) throws BrokerException {

        bindingDao.retrieveBindingsForExchange(exchange.getName(), (queueName, bindingKey, messageFilter) -> {
            QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);

            Binding binding = new Binding(queueHandler.getQueue(), bindingKey, FieldTable.EMPTY_TABLE);
            BindingSet bindingSet = routingKeyToBindingMap.computeIfAbsent(bindingKey, k -> new BindingSet());
            bindingSet.add(binding);
        });
    }
}
