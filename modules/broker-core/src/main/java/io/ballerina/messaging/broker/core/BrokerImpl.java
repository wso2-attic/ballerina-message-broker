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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.ballerina.messaging.broker.auth.AuthManager;
import io.ballerina.messaging.broker.auth.authorization.Authorizer;
import io.ballerina.messaging.broker.auth.authorization.authorizer.empty.NoOpAuthorizer;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.config.BrokerCommonConfiguration;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.coordination.BasicHaListener;
import io.ballerina.messaging.broker.coordination.HaListener;
import io.ballerina.messaging.broker.coordination.HaStrategy;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.metrics.BrokerMetricManager;
import io.ballerina.messaging.broker.core.metrics.DefaultBrokerMetricManager;
import io.ballerina.messaging.broker.core.metrics.NullBrokerMetricManager;
import io.ballerina.messaging.broker.core.rest.api.ExchangesApi;
import io.ballerina.messaging.broker.core.rest.api.QueuesApi;
import io.ballerina.messaging.broker.core.store.DbBackedStoreFactory;
import io.ballerina.messaging.broker.core.store.MemBackedStoreFactory;
import io.ballerina.messaging.broker.core.store.MessageStore;
import io.ballerina.messaging.broker.core.store.StoreFactory;
import io.ballerina.messaging.broker.core.task.Task;
import io.ballerina.messaging.broker.core.task.TaskExecutorService;
import io.ballerina.messaging.broker.core.transaction.BrokerTransaction;
import io.ballerina.messaging.broker.core.transaction.BrokerTransactionFactory;
import io.ballerina.messaging.broker.core.util.MessageTracer;
import io.ballerina.messaging.broker.rest.BrokerServiceRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.MetricService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.sql.DataSource;
import javax.transaction.xa.Xid;

/**
 * Broker APIs not protected by authorization.
 */
public final class BrokerImpl implements Broker {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrokerImpl.class);

    /**
     * Internal queue used to put unprocessable messages.
     */
    public static final String DEFAULT_DEAD_LETTER_QUEUE = "amq.dlq";

    /**
     * Generated header names when putting a file to dead letter queue.
     */
    public static final String ORIGIN_QUEUE_HEADER = "x-origin-queue";
    public static final String ORIGIN_EXCHANGE_HEADER = "x-origin-exchange";
    public static final String ORIGIN_ROUTING_KEY_HEADER = "x-origin-routing-key";

    /**
     * Used to manage metrics related to broker.
     */
    private final BrokerMetricManager metricManager;

    /**
     * The {@link HaStrategy} for which the HA listener is registered.
     */
    private HaStrategy haStrategy;

    private BrokerHelper brokerHelper;

    private final BrokerTransactionFactory brokerTransactionFactory;

    private final QueueRegistry queueRegistry;

    private final TaskExecutorService<MessageDeliveryTask> deliveryTaskService;

    private final TaskExecutorService<Task> messageExpiryTaskService;

    private final ExchangeRegistry exchangeRegistry;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final MessageStore messageStore;

    private final MessageDeliveryTaskFactory messageDeliveryTaskFactory;

    private final MessageExpiryTaskFactory messageExpiryTaskFactory;

    public BrokerImpl(StartupContext startupContext) throws Exception {
        MetricService metrics = startupContext.getService(MetricService.class);
        metricManager = getMetricManager(metrics);

        BrokerConfigProvider configProvider = startupContext.getService(BrokerConfigProvider.class);
        BrokerCoreConfiguration configuration = configProvider.getConfigurationObject(BrokerCoreConfiguration.NAMESPACE,
                                                                                      BrokerCoreConfiguration.class);
        StoreFactory storeFactory = getStoreFactory(startupContext, configProvider, configuration);

        exchangeRegistry = storeFactory.getExchangeRegistry();
        messageStore = storeFactory.getMessageStore();
        queueRegistry = storeFactory.getQueueRegistry();
        exchangeRegistry.retrieveFromStore(queueRegistry);

        this.deliveryTaskService = createDeliveryTaskExecutorService(configuration);
        this.messageExpiryTaskService = createMessageExpiryTaskExecutorService(configuration);
        DLXMover dlxMover = new DLXMover() {
            @Override
            public void moveMessageToDlc(String queueName, Message message) throws BrokerException {
                moveToDlc(queueName, message);
            }
        };
        this.messageExpiryTaskFactory = new MessageExpiryTaskFactory(configuration.getMessageExpiryTask(), dlxMover);
        this.messageDeliveryTaskFactory = new MessageDeliveryTaskFactory(configuration.getDeliveryTask());
        initDefaultDeadLetterQueue();

        this.brokerTransactionFactory = new BrokerTransactionFactory(this, messageStore);
        brokerTransactionFactory.syncWithMessageStore(messageStore);

        startupContext.registerService(Broker.class, this);
        initRestApi(startupContext);
        initHaSupport(startupContext);

    }

    private StoreFactory getStoreFactory(StartupContext startupContext,
                                         BrokerConfigProvider configProvider,
                                         BrokerCoreConfiguration configuration) throws Exception {
        BrokerCommonConfiguration commonConfigs
                = configProvider.getConfigurationObject(BrokerCommonConfiguration.NAMESPACE,
                BrokerCommonConfiguration.class);

        // We use defaults if the common config is not there
        if (Objects.isNull(commonConfigs)) {
            commonConfigs = new BrokerCommonConfiguration();
        }
        DataSource dataSource = startupContext.getService(DataSource.class);

        if (commonConfigs.getEnableInMemoryMode()) {
            return new MemBackedStoreFactory(metricManager, configuration);
        } else {
            return new DbBackedStoreFactory(dataSource, metricManager, configuration);
        }
    }

    private void initRestApi(StartupContext startupContext) {
        BrokerServiceRunner serviceRunner = startupContext.getService(BrokerServiceRunner.class);
        if (Objects.nonNull(serviceRunner)) {
            AuthManager authManager = startupContext.getService(AuthManager.class);
            BrokerFactory brokerFactory;
            Authorizer dacHandler;
            if (null != authManager && authManager.isAuthenticationEnabled() && authManager.isAuthorizationEnabled()) {

                brokerFactory = new SecureBrokerFactory(startupContext);
                dacHandler = authManager.getAuthorizer();
            } else {
                brokerFactory = new DefaultBrokerFactory(startupContext);
                dacHandler = new NoOpAuthorizer();
            }
            serviceRunner.deploy(new QueuesApi(brokerFactory, dacHandler), new ExchangesApi(brokerFactory, dacHandler));
        }
    }

    private void initHaSupport(StartupContext startupContext) {
        haStrategy = startupContext.getService(HaStrategy.class);
        if (haStrategy == null) {
            brokerHelper = new BrokerHelper();
        } else {
            LOGGER.info("Broker is in PASSIVE mode"); //starts up in passive mode
            brokerHelper = new HaEnabledBrokerHelper();
        }
    }

    private void initDefaultDeadLetterQueue() throws BrokerException, ValidationException {
        createQueue(DEFAULT_DEAD_LETTER_QUEUE, false, true, false);
        bind(DEFAULT_DEAD_LETTER_QUEUE,
                ExchangeRegistry.DEFAULT_DEAD_LETTER_EXCHANGE,
                DEFAULT_DEAD_LETTER_QUEUE,
                FieldTable.EMPTY_TABLE);
    }

    private BrokerMetricManager getMetricManager(MetricService metrics) {
        if (Objects.nonNull(metrics)) {
            return new DefaultBrokerMetricManager(metrics);
        } else {
            return new NullBrokerMetricManager();
        }
    }

    private TaskExecutorService<MessageDeliveryTask>
    createDeliveryTaskExecutorService(BrokerCoreConfiguration configuration) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("MessageDeliveryTaskThreadPool-%d")
                .build();
        int workerCount = Integer.parseInt(configuration.getDeliveryTask().getWorkerCount());
        int idleTaskDelay = Integer.parseInt(configuration.getDeliveryTask().getIdleTaskDelay());
        return new TaskExecutorService<>(workerCount, idleTaskDelay, threadFactory);
    }

    private TaskExecutorService<Task> createMessageExpiryTaskExecutorService(BrokerCoreConfiguration configuration) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("MessageExpiryTaskThreadPool-%d")
                .build();
        int workerCount = Integer.parseInt(configuration.getMessageExpiryTask().getWorkerCount());
        int idleTaskDelay = Integer.parseInt(configuration.getMessageExpiryTask().getIdleTaskDelay());
        return new TaskExecutorService<>(workerCount, idleTaskDelay, threadFactory);
    }

    @Override
    public void publish(Message message) throws BrokerException {
        lock.readLock().lock();
        try {
            Metadata metadata = message.getMetadata();
            Exchange exchange = exchangeRegistry.getExchange(metadata.getExchangeName());
            if (exchange != null) {
                String routingKey = metadata.getRoutingKey();
                BindingSet bindingSet = exchange.getBindingsForRoute(routingKey);

                if (bindingSet.isEmpty()) {
                    LOGGER.info("Dropping message since no queues found for routing key {} in {}",
                            routingKey, exchange);
                    MessageTracer.trace(message, MessageTracer.NO_ROUTES);
                } else {
                    try {
                        messageStore.add(message.shallowCopy());
                        Set<QueueHandler> uniqueQueues = getUniqueQueueHandlersForBinding(metadata, bindingSet);
                        publishToQueues(message, uniqueQueues);
                    } finally {
                        messageStore.flush(message.getInternalId());
                    }
                }
            } else {
                MessageTracer.trace(message, MessageTracer.UNKNOWN_EXCHANGE);
                throw new BrokerException("Message publish failed. Unknown exchange: " + metadata.getExchangeName());
            }
        } finally {
            lock.readLock().unlock();
            // Release the original message. Shallow copies are distributed
            message.release();
        }

    }

    private Set<QueueHandler> getUniqueQueueHandlersForBinding(Metadata metadata, BindingSet bindingSet) {
        Set<QueueHandler> uniqueQueues = new HashSet<>();
        for (Binding binding : bindingSet.getUnfilteredBindings()) {
            uniqueQueues.add(binding.getQueue().getQueueHandler());
        }

        for (Binding binding : bindingSet.getFilteredBindings()) {
            if (binding.getFilterExpression().evaluate(metadata)) {
                uniqueQueues.add(binding.getQueue().getQueueHandler());
            }
        }
        return uniqueQueues;
    }

    private void publishToQueues(Message message, Set<QueueHandler> uniqueQueueHandlers) throws BrokerException {
        // Unique queues can be empty due to un-matching selectors.
        if (uniqueQueueHandlers.isEmpty()) {
            LOGGER.info("Dropping message since message didn't have any routes to {}",
                        message.getMetadata().getRoutingKey());
            MessageTracer.trace(message, MessageTracer.NO_ROUTES);
            return;
        }

        for (QueueHandler handler : uniqueQueueHandlers) {
            handler.enqueue(message.shallowCopy());
        }
        metricManager.markPublish();
    }

    @Override
    public void acknowledge(String queueName, DetachableMessage detachableMessage) throws BrokerException {
        lock.readLock().lock();
        try {
            QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);
            queueHandler.dequeue(detachableMessage);
            metricManager.markAcknowledge();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<QueueHandler> enqueue(Xid xid, Message message) throws BrokerException {
        lock.readLock().lock();
        try {
            Metadata metadata = message.getMetadata();
            Exchange exchange = exchangeRegistry.getExchange(metadata.getExchangeName());
            if (Objects.nonNull(exchange)) {
                BindingSet bindingsForRoute = exchange.getBindingsForRoute(metadata.getRoutingKey());
                Set<QueueHandler> uniqueQueueHandlers = getUniqueQueueHandlersForBinding(metadata, bindingsForRoute);
                if (uniqueQueueHandlers.isEmpty()) {
                    MessageTracer.trace(message, xid, MessageTracer.NO_ROUTES);
                    return uniqueQueueHandlers;
                }
                messageStore.add(xid, message.shallowCopy());
                for (QueueHandler handler : uniqueQueueHandlers) {
                    handler.prepareForEnqueue(xid, message.shallowCopy());
                }
                return uniqueQueueHandlers;
            } else {
                MessageTracer.trace(message, xid, MessageTracer.UNKNOWN_EXCHANGE);
                throw new BrokerException("Message published to unknown exchange " + metadata.getExchangeName());
            }
        } finally {
            lock.readLock().unlock();
            message.release();
        }
    }

    @Override
    public QueueHandler dequeue(Xid xid, String queueName, DetachableMessage detachableMessage) throws BrokerException {
        lock.readLock().lock();
        try {
            QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);
            queueHandler.prepareForDetach(xid, detachableMessage);
            return queueHandler;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void addConsumer(Consumer consumer) throws BrokerException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Consume request received for {}", consumer.getQueueName());
        }

        lock.readLock().lock();
        try {
            QueueHandler queueHandler = queueRegistry.getQueueHandler(consumer.getQueueName());
            if (queueHandler != null) {
                synchronized (queueHandler) {
                    if (queueHandler.addConsumer(consumer) && queueHandler.consumerCount() == 1) {
                        deliveryTaskService.add(messageDeliveryTaskFactory.create(queueHandler));
                    }
                }
            } else {
                throw new BrokerException("Cannot add consumer. Queue [ " + consumer.getQueueName() + " ] "
                        + "not found. Create the queue before attempting to consume.");
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean removeConsumer(Consumer consumer) {
        lock.readLock().lock();
        boolean queueDeletable = false;
        QueueHandler queueHandler;
        try {
            queueHandler = queueRegistry.getQueueHandler(consumer.getQueueName());
            if (queueHandler != null) {
                synchronized (queueHandler) {
                    if (queueHandler.removeConsumer(consumer) && queueHandler.consumerCount() == 0) {
                        deliveryTaskService.remove(queueHandler.getUnmodifiableQueue().getName());
                        if (queueHandler.getUnmodifiableQueue().isAutoDelete()) {
                            queueDeletable = true;
                        }
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        // queue delete is done after releasing the read lock since we cannot upgrade to write lock from a read lock.
        if (queueDeletable) {
            try {
                deleteQueue(queueHandler.getUnmodifiableQueue().getName(), true, false);
            } catch (ValidationException | ResourceNotFoundException | BrokerException e) {
                // We do not propagate the error to transport layer since we should not get an error for a queue
                // delete initiated from server.
                LOGGER.warn("Exception while auto deleting the queue {}", queueHandler.getUnmodifiableQueue(), e);
            }
        }
        return queueDeletable;
    }

    @Override
    public void declareExchange(String exchangeName, String type,
                                boolean passive, boolean durable) throws BrokerException, ValidationException {
        lock.writeLock().lock();
        try {
            exchangeRegistry.declareExchange(exchangeName, type, passive, durable);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void createExchange(String exchangeName, String type, boolean durable) throws BrokerException,
            ValidationException {
        lock.writeLock().lock();
        try {
            exchangeRegistry.createExchange(exchangeName, Exchange.Type.from(type), durable);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean deleteExchange(String exchangeName, boolean ifUnused) throws BrokerException, ValidationException {
        lock.writeLock().lock();
        try {
            return exchangeRegistry.deleteExchange(exchangeName, ifUnused);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean createQueue(String queueName, boolean passive,
                               boolean durable, boolean autoDelete) throws BrokerException, ValidationException {
        lock.writeLock().lock();
        try {
            boolean queueAdded = queueRegistry.addQueue(queueName, passive, durable, autoDelete);
            QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);
            if (queueAdded) {
                // We need to bind every queue to the default exchange
                exchangeRegistry.getDefaultExchange().bind(queueHandler, queueName, FieldTable.EMPTY_TABLE);
            } else {
                // Do not check expired messages in DLQ
                if (!queueName.equals(DEFAULT_DEAD_LETTER_QUEUE)) {
                    messageExpiryTaskService.add(messageExpiryTaskFactory.create(queueHandler));
                }
            }
            return queueAdded;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int deleteQueue(String queueName, boolean ifUnused, boolean ifEmpty) throws BrokerException,
            ValidationException, ResourceNotFoundException {
        lock.writeLock().lock();
        try {
            messageExpiryTaskService.remove(queueName);
            return queueRegistry.removeQueue(queueName, ifUnused, ifEmpty);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean queueExists(String queueName) {
        lock.readLock().lock();
        try {
            return Objects.nonNull(queueRegistry.getQueueHandler(queueName));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void bind(String queueName, String exchangeName,
                     String routingKey, FieldTable arguments) throws BrokerException, ValidationException {
        lock.writeLock().lock();
        try {
            Exchange exchange = exchangeRegistry.getExchange(exchangeName);
            QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);
            if (exchange == null) {
                throw new ValidationException("Unknown exchange name: " + exchangeName);
            }

            if (queueHandler == null) {
                throw new ValidationException("Unknown queue name: " + queueName);
            }

            if (!routingKey.isEmpty()) {
                exchange.bind(queueHandler, routingKey, arguments);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void unbind(String queueName, String exchangeName, String routingKey)
            throws BrokerException, ValidationException {
        lock.writeLock().lock();
        try {
            Exchange exchange = exchangeRegistry.getExchange(exchangeName);
            QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);

            if (exchange == null) {
                throw new ValidationException("Unknown exchange name: " + exchangeName);
            }

            if (queueHandler == null) {
                throw new ValidationException("Unknown queue name: " + queueName);
            }

            exchange.unbind(queueHandler.getUnmodifiableQueue(), routingKey);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void startMessageDelivery() {
        brokerHelper.startMessageDelivery();
    }

    @Override
    public int purgeQueue(String queueName) throws ResourceNotFoundException, ValidationException {
        lock.writeLock().lock();
        try {
            QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);

            if (queueHandler == null) {
                throw new ResourceNotFoundException("Queue [ " + queueName + " ] Not found");
            }

            return queueHandler.purgeQueue();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void stopMessageDelivery() {
        LOGGER.info("Stopping message delivery threads.");
        deliveryTaskService.stop();
        LOGGER.info("Stopping message expiry checking threads.");
        messageExpiryTaskService.stop();
    }

    @Override
    public void shutdown() {
        brokerHelper.shutdown();
    }

    @Override
    public void requeue(String queueName, Message message) throws BrokerException, ResourceNotFoundException {
        lock.readLock().lock();
        try {
            QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);

            if (Objects.isNull(queueHandler)) {
                message.release();
                throw new ResourceNotFoundException("Queue [ " + queueName + " ] Not found");
            }
            queueHandler.requeue(message);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<QueueHandler> getAllQueues() {
        lock.readLock().lock();
        try {
            return queueRegistry.getAllQueues();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public QueueHandler getQueue(String queueName) throws ResourceNotFoundException {
        lock.readLock().lock();
        try {
            QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);
            if (Objects.isNull(queueHandler)) {
                throw new ResourceNotFoundException("Queue [ " + queueName + " ] Not found");
            }
            return queueHandler;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void moveToDlc(String queueName, Message message) throws BrokerException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Moving message to DLC: {}", message);
        }
        try {
            Message dlcMessage = message.shallowCopyWith(Broker.getNextMessageId(),
                    DEFAULT_DEAD_LETTER_QUEUE,
                    ExchangeRegistry.DEFAULT_DEAD_LETTER_EXCHANGE);
            dlcMessage.getMetadata().addHeader(ORIGIN_QUEUE_HEADER, queueName);
            dlcMessage.getMetadata().addHeader(ORIGIN_EXCHANGE_HEADER, message.getMetadata().getExchangeName());
            dlcMessage.getMetadata().addHeader(ORIGIN_ROUTING_KEY_HEADER, message.getMetadata().getRoutingKey());

            publish(dlcMessage);
            acknowledge(queueName, message.getDetachableMessage());
        } finally {
            message.release();
        }
    }

    @Override
    public Collection<Exchange> getAllExchanges() {
        lock.readLock().lock();
        try {
            return exchangeRegistry.getAllExchanges();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Map<String, BindingSet> getAllBindingsForExchange(String exchangeName) throws ValidationException {
        lock.readLock().lock();

        try {
            Exchange exchange = exchangeRegistry.getExchange(exchangeName);
            if (Objects.isNull(exchange)) {
                throw new ValidationException("Non existing exchange name " + exchangeName);
            }

            return exchange.getBindingsRegistry().getAllBindings();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Exchange getExchange(String exchangeName) {
        lock.readLock().lock();
        try {
            return exchangeRegistry.getExchange(exchangeName);
        } finally {
            lock.readLock().unlock();
        }

    }

    @Override
    public BrokerTransaction newLocalTransaction() {
        return brokerTransactionFactory.newLocalTransaction();
    }

    @Override
    public BrokerTransaction newDistributedTransaction() {
        return brokerTransactionFactory.newDistributedTransaction();
    }

    @Override
    public Set<QueueHandler> restoreDtxPreparedMessages(Xid xid, Collection<Message> messages) throws BrokerException {
        Set<QueueHandler> queueHandlers = new HashSet<>();
        lock.readLock().lock();
        try {
            for (Message message : messages) {
                try {
                    messageStore.add(xid, message.shallowCopy());
                    for (String queueName : message.getAttachedDurableQueues()) {
                        QueueHandler queueHandler = queueRegistry.getQueueHandler(queueName);
                        queueHandler.prepareForEnqueue(xid, message.shallowCopy());
                        queueHandlers.add(queueHandler);
                    }
                } finally {
                    message.release();
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return queueHandlers;
    }

    private class BrokerHelper {

        public void startMessageDelivery() {
            LOGGER.info("Starting message delivery threads.");
            deliveryTaskService.start();
            LOGGER.info("Starting message expiry checking threads.");
            messageExpiryTaskService.start();
        }

        public void shutdown() {
            stopMessageDelivery();
        }

    }

    private class HaEnabledBrokerHelper extends BrokerHelper implements HaListener {

        private BasicHaListener basicHaListener;

        HaEnabledBrokerHelper() {
            basicHaListener = new BasicHaListener(this);
            haStrategy.registerListener(basicHaListener, 1);
        }

        @Override
        public synchronized void startMessageDelivery() {
            basicHaListener.setStartCalled(); //to allow starting when the node becomes active when HA is enabled
            if (!basicHaListener.isActive()) {
                return;
            }
            super.startMessageDelivery();
        }

        @Override
        public void shutdown() {
            haStrategy.unregisterListener(basicHaListener);
            super.shutdown();
        }

        /**
         * {@inheritDoc}
         */
        public void activate() {
            try {
                queueRegistry.reloadQueuesOnBecomingActive();
                exchangeRegistry.reloadExchangesOnBecomingActive(queueRegistry);
                brokerTransactionFactory.syncWithMessageStore(messageStore);
            } catch (BrokerException e) {
                LOGGER.error("Error on loading data from the database on becoming active ", e);
            }
            startMessageDeliveryOnBecomingActive();
            LOGGER.info("Broker mode changed from PASSIVE to ACTIVE");
        }

        /**
         * {@inheritDoc}
         */
        public void deactivate() {
            stopMessageDelivery();
            LOGGER.info("Broker mode changed from ACTIVE to PASSIVE");
        }

        /**
         * Method to start message delivery by the broker, only if startMessageDelivery()} has been called, prior to
         * becoming the active node.
         */
        private synchronized void startMessageDeliveryOnBecomingActive() {
            if (basicHaListener.isStartCalled()) {
                startMessageDelivery();
            }
        }

    }
}
