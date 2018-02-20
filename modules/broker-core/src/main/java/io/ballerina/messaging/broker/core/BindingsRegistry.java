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
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.util.function.ThrowingConsumer;
import io.ballerina.messaging.broker.core.store.dao.BindingDao;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the bindings for a given {@link Exchange}.
 */
public final class BindingsRegistry {

    private final Map<String, BindingSet> bindingPatternToBindingsMap;

    private final Exchange exchange;

    private final BindingDao bindingDao;

    private final Map<String, BindingSet> unmodifiableBindingSetView;

    private final BindingDeleteListener bindingDeleteListener;

    BindingsRegistry(Exchange exchange, BindingDao bindingDao) {
        this.bindingPatternToBindingsMap = new HashMap<>();
        this.exchange = exchange;
        this.bindingDao = bindingDao;
        this.unmodifiableBindingSetView = Collections.unmodifiableMap(bindingPatternToBindingsMap);
        bindingDeleteListener = new BindingDeleteListener();
    }

    void bind(QueueHandler queueHandler, String bindingKey, FieldTable arguments) throws BrokerException,
                                                                                         ValidationException {
        BindingSet bindingSet = bindingPatternToBindingsMap.computeIfAbsent(bindingKey, k -> new BindingSet());
        Queue queue = queueHandler.getQueue();
        Binding binding = new Binding(queue, bindingKey, arguments);
        boolean success = bindingSet.add(binding);

        if (success) {
            queueHandler.addBinding(binding, bindingDeleteListener);
            if (queue.isDurable()) {
                bindingDao.persist(exchange.getName(), binding);
            }
        }
    }

    void unbind(Queue queue, String routingKey) throws BrokerException {
        BindingSet bindingSet = bindingPatternToBindingsMap.get(routingKey);
        if (queue.isDurable()) {
            bindingDao.delete(queue.getName(), routingKey, exchange.getName());
        }
        bindingSet.remove(queue);

        if (bindingSet.isEmpty()) {
            bindingPatternToBindingsMap.remove(routingKey);
        }
    }

    BindingSet getBindingsForRoute(String routingKey) {
        BindingSet bindingSet = bindingPatternToBindingsMap.get(routingKey);
        if (bindingSet == null) {
            bindingSet = BindingSet.emptySet();
        }
        return bindingSet;
    }

    boolean isEmpty() {
        return bindingPatternToBindingsMap.isEmpty();
    }

    public void retrieveAllBindingsForExchange(QueueRegistry queueRegistry) throws BrokerException {
        bindingDao.retrieveBindingsForExchange(exchange.getName(), (queueName, bindingKey, filterTable) -> {
            QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);

            Binding binding = new Binding(queueHandler.getQueue(), bindingKey, filterTable);
            BindingSet bindingSet = bindingPatternToBindingsMap.computeIfAbsent(bindingKey, k -> new BindingSet());
            bindingSet.add(binding);
            queueHandler.addBinding(binding, bindingDeleteListener);
        });
    }

    public Map<String, BindingSet> getAllBindings() {
        return unmodifiableBindingSetView;
    }

    /**
     * Handles binding delete events coming from listeners.
     */
    private class BindingDeleteListener implements ThrowingConsumer<Binding, BrokerException> {

        @Override
        public void accept(Binding binding) throws BrokerException {
            unbind(binding.getQueue(), binding.getBindingPattern());
        }
    }
}
