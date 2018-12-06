package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.util.function.ThrowingConsumer;
import io.ballerina.messaging.broker.core.metrics.BrokerMetricManager;
import io.ballerina.messaging.broker.eventing.EventSync;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.transaction.xa.Xid;

/**
 * Represents an Queue Handler which trigger events for the broker.
 */
public class ObservableQueueHandlerImpl extends QueueHandler {

    private QueueHandlerImpl queueHandler;
    private DefaultQueueHandlerEventPublisher defaultQueueHandlerEventPublisher;
    private EventSync eventSync;

    ObservableQueueHandlerImpl(Queue queue,
                               BrokerMetricManager metricManager,
                               EventSync eventSync,
                               List<Integer> messageLimits) {
        queueHandler = new QueueHandlerImpl(queue, metricManager);
        defaultQueueHandlerEventPublisher = new DefaultQueueHandlerEventPublisher(eventSync, messageLimits);
        this.eventSync = eventSync;
    }

    @Override
    public Queue getUnmodifiableQueue() {
        return queueHandler.getUnmodifiableQueue();
    }

    @Override
    public Collection<Consumer> getConsumers() {
        return queueHandler.getConsumers();
    }

    @Override
    boolean addConsumer(Consumer consumer) {
        boolean consumerAdded = queueHandler.addConsumer(new ObservableConsumer(consumer, eventSync));
        if (consumerAdded) {
            defaultQueueHandlerEventPublisher.publishConsumerEvent("consumer.added", consumer);
        }
        return consumerAdded;
    }

    @Override
    boolean removeConsumer(Consumer consumer) {
        boolean consumerRemoved = queueHandler.removeConsumer(consumer);
        if (consumerRemoved) {
            defaultQueueHandlerEventPublisher.publishConsumerEvent("consumer.removed", consumer);
        }
        return consumerRemoved;
    }

    @Override
    void enqueue(Message message) throws BrokerException {
        queueHandler.enqueue(message);
    }

    @Override
    void prepareForEnqueue(Xid xid, Message message) throws BrokerException {
        queueHandler.prepareForEnqueue(xid, message);
    }

    @Override
    void prepareForDetach(Xid xid, DetachableMessage detachableMessage) throws BrokerException {
        queueHandler.prepareForDetach(xid, detachableMessage);
    }

    @Override
    public void commit(Xid xid) {
        queueHandler.commit(xid);
    }

    @Override
    public void rollback(Xid xid) {
        queueHandler.rollback(xid);
    }

    @Override
    Message takeForDelivery() {
        return queueHandler.takeForDelivery();
    }

    @Override
    void dequeue(DetachableMessage detachableMessage) throws BrokerException {
        queueHandler.dequeue(detachableMessage);
        defaultQueueHandlerEventPublisher.publishQueueLimitReachedEvent(this.queueHandler);
    }

    @Override
    public void requeue(Message message) throws BrokerException {
        queueHandler.requeue(message);
    }

    @Override
    CyclicConsumerIterator getCyclicConsumerIterator() {
        return queueHandler.getCyclicConsumerIterator();
    }

    @Override
    boolean isEmpty() {
        return queueHandler.isEmpty();
    }

    @Override
    public int size() {
        return queueHandler.size();
    }

    @Override
    boolean isUnused() {
        return queueHandler.isUnused();
    }

    @Override
    public int consumerCount() {
        return queueHandler.consumerCount();
    }

    @Override
    public void addBinding(Binding binding, ThrowingConsumer<Binding, BrokerException> bindingChangeListener) {
        queueHandler.addBinding(binding, bindingChangeListener);
        defaultQueueHandlerEventPublisher.publishBindingEvent("binding.added", binding);
    }

    @Override
    public int releaseResources() throws BrokerException {
        return queueHandler.releaseResources();
    }

    @Override
    public void removeBinding(Binding binding) {
        queueHandler.removeBinding(binding);
        defaultQueueHandlerEventPublisher.publishBindingEvent("binding.removed", binding);
    }

    @Override
    public int purgeQueue() throws ValidationException {
        return queueHandler.purgeQueue();
    }

    private static class DefaultQueueHandlerEventPublisher {
        EventSync eventSync;
        HashSet<Integer> messageLimits;

        DefaultQueueHandlerEventPublisher(EventSync eventSync, List<Integer> messageLimits) {
            this.eventSync = eventSync;
            this.messageLimits = new HashSet<>(messageLimits);
        }

        private void publishConsumerEvent(String id, Consumer consumer) {
            Map<String, String> properties = new HashMap<>();
            properties.put("consumerID", String.valueOf(consumer.getId()));
            properties.put("queueName", consumer.getQueueName());
            properties.put("ready", String.valueOf(consumer.isReady()));
            properties.put("exclusive", String.valueOf(consumer.isExclusive()));
            eventSync.publish(id, properties);
        }

        private void publishBindingEvent(String id, Binding binding) {
            Map<String, String> properties = new HashMap<>();
            properties.put("bindingQueue", binding.getQueue().getName());
            properties.put("bindingPattern", binding.getBindingPattern());
            eventSync.publish(id, properties);
        }

        private void publishQueueLimitReachedEvent(QueueHandlerImpl queueHandler) {
            int queueSize = queueHandler.size();
            if (messageLimits.contains(queueSize)) {
                Map<String, String> properties = new HashMap<>();
                String queueName = queueHandler.getUnmodifiableQueue().getName();
                String isAutoDelete = String.valueOf(queueHandler.getUnmodifiableQueue().isAutoDelete());
                String isDurable = String.valueOf(queueHandler.getUnmodifiableQueue().isDurable());
                properties.put("queueName", queueName);
                properties.put("autoDelete", isAutoDelete);
                properties.put("durable", isDurable);
                properties.put("messageCount", String.valueOf(queueHandler.size()));
                String id = "queue.limitReached." + queueName + "." + queueSize;
                eventSync.publish(id, properties);
            }
        }
    }
}
