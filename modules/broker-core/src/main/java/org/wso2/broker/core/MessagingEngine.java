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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.wso2.broker.core.task.TaskExecutorService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * Broker's messaging core which handles message publishing, create and delete queue operations.
 */
final class MessagingEngine {

    private final Map<String, QueueHandler> queueRegistry;

    private final TaskExecutorService<MessageDeliveryTask> deliveryTaskService;

    private final ExchangeRegistry exchangeRegistry;

    /**
     * Number of worker.
     */
    private static final int WORKER_COUNT = 5;

    /**
     * Delay for waiting for an idle task.
     */
    private static final long IDLE_TASK_DELAY_MILLIS = 100;

    MessagingEngine() {
        queueRegistry = new ConcurrentHashMap<>();
        exchangeRegistry = new ExchangeRegistry();
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("MessageDeliveryTaskThreadPool-%d").build();
        deliveryTaskService = new TaskExecutorService<>(WORKER_COUNT, IDLE_TASK_DELAY_MILLIS, threadFactory);
    }

    void bind(String queueName, String exchangeName, String routingKey) throws BrokerException {
        Exchange exchange = exchangeRegistry.getExchange(exchangeName);
        QueueHandler queueHandler = queueRegistry.get(queueName);
        if (exchange == null) {
            throw new BrokerException("Unknown exchange name: " + exchangeName);
        }

        if (queueHandler == null) {
            throw new BrokerException("Unknown queue name: " + queueName);
        }

        if (!routingKey.isEmpty()) {
            exchange.bind(queueHandler, routingKey);
        }

    }

    void unbind(String queueName, String exchangeName, String routingKey) throws BrokerException {
        Exchange exchange = exchangeRegistry.getExchange(exchangeName);
        if (exchange == null) {
            throw new BrokerException("Unknown exchange name: " + exchangeName);
        }

        exchange.unbind(queueName, routingKey);
    }

    void createQueue(String queueName, boolean passive, boolean durable, boolean autoDelete) throws BrokerException {
        QueueHandler queueHandler = queueRegistry.get(queueName);

        if (passive && queueHandler == null) {
            throw new BrokerException("QueueHandler [ " + queueName + " ] doesn't exists. Passive parameter " +
                    "is set, hence not creating the queue.");
        }

        if (queueHandler == null) {
            queueHandler = new QueueHandler(queueName, durable, autoDelete, 1000);
            queueRegistry.put(queueName, queueHandler);
            deliveryTaskService.add(new MessageDeliveryTask(queueHandler));
        } else if (!passive) {
            throw new BrokerException("QueueHandler [ " + queueName + " ] already exists.");
        }
    }

    void publish(Message message) throws BrokerException {
        Metadata metadata = message.getMetadata();
        Exchange exchange = exchangeRegistry.getExchange(metadata.getExchangeName());
        if (exchange != null) {
            Set<Binding> bindings = exchange.getBindingsForRoute(metadata.getRoutingKey());
            bindings.forEach(binding -> {
                QueueHandler queueHandler = queueRegistry.get(binding.getQueueName());
                metadata.addOwnedQueue(binding.getQueueName());
                queueHandler.enqueue(message);
            });
        } else {
            throw new BrokerException("Message publish failed. Unknown exchange: " + metadata.getExchangeName());
        }
    }

    void acknowledge(String queueName, long deliveryTag, boolean multiple) {
        QueueHandler queueHandler = queueRegistry.get(queueName);
        queueHandler.acknowledge(deliveryTag, multiple);
    }

    void deleteQueue(String queueName, boolean ifUnused, boolean ifEmpty) throws BrokerException {
        QueueHandler queueHandler = queueRegistry.get(queueName);
        if (queueHandler == null) {
            return;
        }

        if (ifUnused && !queueHandler.isUnused()) {
            throw new BrokerException("Cannot delete queue. Queue [ " + queueName +
                    " ] has active consumers and the ifUnused parameter is set.");
        } else if (ifEmpty && !queueHandler.isEmpty()) {
            throw new BrokerException("Cannot delete queue. Queue [ " + queueName +
                    " ] is not empty and the ifEmpty parameter is set.");
        } else {
            deliveryTaskService.remove(queueName);
            queueRegistry.remove(queueName);
            queueHandler.closeAllConsumers();
        }
    }

    void consume(Consumer consumer) throws BrokerException {
        QueueHandler queueHandler = queueRegistry.get(consumer.getSubscribedQueue());
        if (queueHandler != null) {
            queueHandler.addConsumer(consumer);
        } else {
            throw new BrokerException("Cannot add consumer. Queue [ " + consumer.getSubscribedQueue() + " ] " +
                    "not found. Create the queue before attempting to consume.");
        }
    }

    void startMessageDelivery() {
        deliveryTaskService.start();
    }

    void stopMessageDelivery() {
        deliveryTaskService.stop();
    }

    void createExchange(String exchangeName, String type,
                        boolean passive, boolean durable) throws BrokerException {
        exchangeRegistry.declareExchange(exchangeName, Exchange.Type.from(type), passive, durable);
    }

    void deleteExchange(String exchangeName, String type, boolean ifUnused) throws BrokerException {
        exchangeRegistry.deleteExchange(exchangeName, Exchange.Type.from(type), ifUnused);
    }
}
