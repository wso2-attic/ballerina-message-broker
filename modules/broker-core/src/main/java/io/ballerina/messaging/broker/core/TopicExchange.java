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
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.core.store.dao.BindingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * AMQP topic exchange implementation.
 */
final class TopicExchange extends Exchange {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicExchange.class);

    private final FastTopicMatcher fastTopicMatcher;

    private final ReadWriteLock lock;

    TopicExchange(String exchangeName, BindingDao bindingDao) {
        super(exchangeName, Type.TOPIC, bindingDao);
        fastTopicMatcher = new FastTopicMatcher();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public void bind(QueueHandler queue, String routingPattern, FieldTable arguments) throws BrokerException,
                                                                                             ValidationException {
        lock.writeLock().lock();
        try {
            LOGGER.debug("Binding added for queue {} with pattern {}", queue, routingPattern);
            getBindingsRegistry().bind(queue, routingPattern, arguments);
            fastTopicMatcher.add(routingPattern);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void unbind(Queue queue, String routingPattern) throws BrokerException {
        lock.writeLock().lock();
        try {
            getBindingsRegistry().unbind(queue, routingPattern);
            if (getBindingsRegistry().getBindingsForRoute(routingPattern).isEmpty()) {
                fastTopicMatcher.remove(routingPattern);
            }
            LOGGER.debug("Binding removed from queue {} with pattern {}", queue, routingPattern);
        } finally {
            lock.writeLock().unlock();
        }
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
}
