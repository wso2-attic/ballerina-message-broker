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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.common.data.types.FieldTable;
import org.wso2.broker.core.configuration.BrokerConfiguration;
import org.wso2.broker.core.store.dao.DaoFactory;
import org.wso2.broker.core.store.dao.MessageDao;
import org.wso2.broker.core.store.dao.QueueDao;
import org.wso2.broker.core.task.TaskExecutorService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Broker's messaging core which handles message publishing, create and delete queue operations.
 */
final class MessagingEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingEngine.class);

    /**
     * Delay for waiting for an idle task.
     */
    private static final long IDLE_TASK_DELAY_MILLIS = 100;

    /**
     * Number of worker.
     */
    private static final int WORKER_COUNT = 5;

    private final Map<String, QueueHandler> queueRegistry;

    private final TaskExecutorService<MessageDeliveryTask> deliveryTaskService;

    private final ExchangeRegistry exchangeRegistry;

    private final MessageDao messageDao;
    
    private final QueueDao queueDao;

    /**
     * In memory message id.
     */
    private final AtomicLong messageIdGenerator;

    MessagingEngine(BrokerConfiguration brokerConfiguration) {
        queueRegistry = new ConcurrentHashMap<>();
        exchangeRegistry = new ExchangeRegistry();
        DaoFactory daoFactory = new DaoFactory(brokerConfiguration.getDatasource());
        messageDao = daoFactory.createMesageDao();
        queueDao = daoFactory.createQueueDao();
        
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("MessageDeliveryTaskThreadPool-%d").build();
        deliveryTaskService = new TaskExecutorService<>(WORKER_COUNT, IDLE_TASK_DELAY_MILLIS, threadFactory);
        messageIdGenerator = new AtomicLong(0);
    }

    void bind(String queueName, String exchangeName, String routingKey, FieldTable arguments) throws BrokerException {
        Exchange exchange = exchangeRegistry.getExchange(exchangeName);
        QueueHandler queueHandler = queueRegistry.get(queueName);
        if (exchange == null) {
            throw new BrokerException("Unknown exchange name: " + exchangeName);
        }

        if (queueHandler == null) {
            throw new BrokerException("Unknown queue name: " + queueName);
        }

        if (!routingKey.isEmpty()) {
            exchange.bind(queueHandler.getQueue(), routingKey, arguments);
        }

    }

    void unbind(String queueName, String exchangeName, String routingKey) throws BrokerException {
        Exchange exchange = exchangeRegistry.getExchange(exchangeName);
        QueueHandler queueHandler = queueRegistry.get(queueName);

        if (exchange == null) {
            throw new BrokerException("Unknown exchange name: " + exchangeName);
        }

        if (queueHandler == null) {
            throw new BrokerException("Unknown queue name: " + queueName);
        }

        exchange.unbind(queueHandler.getQueue(), routingKey);
    }

    void createQueue(String queueName, boolean passive, boolean durable, boolean autoDelete) throws BrokerException {

        QueueHandler queueHandler = queueRegistry.get(queueName);

        if (passive && queueHandler == null) {
            throw new BrokerException("QueueHandler [ " + queueName + " ] doesn't exists. Passive parameter " +
                    "is set, hence not creating the queue.");
        }

        if (queueHandler == null) {
            if (durable) {
                queueHandler = QueueHandler.createDurableQueue(queueName, messageDao, queueDao, autoDelete);
            } else {
                queueHandler = QueueHandler.createNonDurableQueue(queueName, 1000, autoDelete);
            }
            queueRegistry.put(queueName, queueHandler);
            // we need to bind every queue to the default exchange
            ExchangeRegistry.DEFAULT_EXCHANGE.bind(queueHandler.getQueue(), queueName, FieldTable.EMPTY_TABLE);

            deliveryTaskService.add(new MessageDeliveryTask(queueHandler));
        } else if (!passive && (queueHandler.getQueue().isDurable() != durable
                || queueHandler.getQueue().isAutoDelete() != autoDelete)) {
            throw new BrokerException("Existing QueueHandler [ " + queueName + " ] does not match given parameters.");
        }
    }

    void publish(Message message) throws BrokerException {
        Metadata metadata = message.getMetadata();
        Exchange exchange = exchangeRegistry.getExchange(metadata.getExchangeName());
        if (exchange != null) {
            String routingKey = metadata.getRoutingKey();
            BindingSet bindingSet = exchange.getBindingsForRoute(routingKey);

            if (bindingSet.isEmpty()) {
                LOGGER.info("Dropping message since no queues found for routing key " + routingKey);
                message.release();
            } else {
                boolean published = false;
                for (Binding binding : bindingSet.getUnfilteredBindings()) {
                    published |= pushToInMemoryQueue(message, binding);
                }

                for (Binding binding : bindingSet.getFilteredBindings()) {
                    if (binding.getFilterExpression().evaluate(metadata)) {
                        published |= pushToInMemoryQueue(message, binding);
                    }
                }
                if (!published) {
                    LOGGER.info("Dropping message since message didn't have any routes for routing key "
                            + metadata.getRoutingKey());
                }
                // Release the original message. Shallow copies are distributed
                message.release(); // TODO: avoid shallow copying when there is only one binding
            }
        } else {
            throw new BrokerException("Message publish failed. Unknown exchange: " + metadata.getExchangeName());
        }
    }

    private boolean pushToInMemoryQueue(Message message, Binding binding) {
        Metadata metadata = message.getMetadata();
        String queueName = binding.getQueue().getName();
        QueueHandler queueHandler = queueRegistry.get(queueName);
        metadata.addOwnedQueue(queueName);
        Message copiedMessage = message.shallowCopy();
        boolean success = queueHandler.enqueue(copiedMessage);
        if (!success) {
            copiedMessage.release();
        }
        return success;
    }

    /**
     * 
     * @param queueName name of the queue
     * @param deliveryTag synonymous for message id
     * @param multiple if true acknowledges multiple messages with delivery tag less than the provided delivery tag
     */
    void acknowledge(String queueName, long deliveryTag, boolean multiple) {
        QueueHandler queueHandler = queueRegistry.get(queueName);
        queueHandler.acknowledge(deliveryTag, multiple);

        messageDao.detachFromQueue(queueName, deliveryTag);
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
            queueDao.delete(queueHandler.getQueue());
        }
    }

    void consume(Consumer consumer) throws BrokerException {
        QueueHandler queueHandler = queueRegistry.get(consumer.getQueueName());
        if (queueHandler != null) {
            queueHandler.addConsumer(consumer);
        } else {
            throw new BrokerException("Cannot add consumer. Queue [ " + consumer.getQueueName() + " ] " +
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

    void closeConsumer(Consumer consumer) {
        QueueHandler queueHandler = queueRegistry.get(consumer.getQueueName());
        if (queueHandler != null)  {
            queueHandler.removeConsumer(consumer);
        }
    }

    long getNextMessageId() {
        return messageIdGenerator.incrementAndGet();
    }
}
