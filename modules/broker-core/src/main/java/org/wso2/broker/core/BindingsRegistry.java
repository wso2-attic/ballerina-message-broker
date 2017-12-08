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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages the bindings for a given {@link Exchange}.
 * TODO why do we repeat routing key in two places (as key and as field in binding object)?
 * Feels like we need to refactor this class.
 */
final class BindingsRegistry {

    private final Map<String, Set<Binding>> routingKeyToBindingMap;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    BindingsRegistry() {
        this.routingKeyToBindingMap = new ConcurrentHashMap<>();
    }

    void bind(QueueHandler queueHandler, String bindingKey) {
        lock.writeLock().lock();
        try {
            Binding binding = new Binding(bindingKey, queueHandler.getQueue().getName());
            Set<Binding> bindingList =
                    routingKeyToBindingMap.computeIfAbsent(bindingKey, k -> ConcurrentHashMap.newKeySet());
            bindingList.add(binding);
        } finally {
            lock.writeLock().unlock();
        }
    }

    void unbind(String queueName, String routingKey) {
        lock.writeLock().lock();
        try {
            Binding deadBind = new Binding(routingKey, queueName);
            Set<Binding> bindings = routingKeyToBindingMap.get(routingKey);
            bindings.remove(deadBind);

            if (bindings.isEmpty()) {
                routingKeyToBindingMap.remove(routingKey);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    Set<Binding> getBindingsForRoute(String routingKey) {
        lock.readLock().lock();
        try {
            Set<Binding> bindings = routingKeyToBindingMap.get(routingKey);
            if (bindings == null) {
                bindings = Collections.emptySet();
            }
            return bindings;
        } finally {
            lock.readLock().unlock();
        }
    }

    boolean isEmpty() {
        lock.readLock().lock();
        try {
            return routingKeyToBindingMap.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    int uniqueRoutingKeyCount() {
        return routingKeyToBindingMap.keySet().size();
    }
}
