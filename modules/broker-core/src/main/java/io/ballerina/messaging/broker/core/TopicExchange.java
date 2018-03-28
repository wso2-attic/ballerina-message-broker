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

import io.ballerina.messaging.broker.common.FastTopicMatcher;
import io.ballerina.messaging.broker.core.store.dao.BindingDao;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * AMQP topic exchange implementation.
 */
final class TopicExchange extends Exchange implements BindingsRegistryListener {

    private final FastTopicMatcher fastTopicMatcher;

    private final ReadWriteLock lock;

    TopicExchange(String exchangeName, BindingDao bindingDao) {
        super(exchangeName, Type.TOPIC, bindingDao);
        fastTopicMatcher = new FastTopicMatcher();
        lock = new ReentrantReadWriteLock();
        getBindingsRegistry().addBindingsRegistryListeners(this);
    }

    @Override
    public BindingSet getBindingsForRoute(String routingKey) {
        if (routingKey.isEmpty()) {
            return BindingSet.emptySet();
        }

        lock.readLock().lock();
        try {
            BindingSet matchedBindingSet = new BindingSet();
            fastTopicMatcher.matchingBindings(routingKey, subscribedPattern -> {
                BindingSet bindingSet = getBindingsRegistry().getBindingsForRoute(subscribedPattern);
                matchedBindingSet.add(bindingSet);
            });
            return matchedBindingSet;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void onBind(String routingKey) {
        fastTopicMatcher.add(routingKey);
    }

    @Override
    public void onUnbind(String routingKey, boolean isLastSubscriber) {
        if (isLastSubscriber) {
            fastTopicMatcher.remove(routingKey);
        }
    }

    @Override
    public void onRetrieveAllBindingsForExchange(String routingKey) {
        fastTopicMatcher.add(routingKey);
    }
}
