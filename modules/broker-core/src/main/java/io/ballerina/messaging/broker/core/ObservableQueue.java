package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.eventing.EventSync;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.transaction.xa.Xid;

/**
 * Represents an Queue which trigger events for the broker.
 */
public class ObservableQueue extends Queue {

    private Queue queue;
    private HashSet<Integer> messageLimits;
    private EventSync eventSync;

    ObservableQueue(Queue queue, EventSync eventSync, List<Integer> messageLimits) {
        super(queue.getName(), queue.isDurable(), queue.isAutoDelete());
        this.queue = queue;
        this.eventSync = eventSync;
        this.messageLimits = new HashSet<>(messageLimits);
    }
    @Override
    public int capacity() {
        return queue.capacity();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean enqueue(Message message) throws BrokerException {
        boolean enqueued = queue.enqueue(message);
        if (enqueued) {
            publishQueueLimitReachedEvent("queue.publishLimitReached.", getQueueHandler());
        }
        return enqueued;
    }

    @Override
    public void prepareEnqueue(Xid xid, Message message) throws BrokerException {
        queue.prepareEnqueue(xid, message);
    }

    @Override
    public void commit(Xid xid) {
        queue.commit(xid);
    }

    @Override
    public void rollback(Xid xid) {
        queue.rollback(xid);
    }

    @Override
    public Message dequeue() {
        Message message =  queue.dequeue();
        publishQueueLimitReachedEvent("queue.deliverLimitReached.", this.getQueueHandler());
        return message;
    }

    @Override
    public void detach(DetachableMessage detachableMessage) throws BrokerException {
        queue.detach(detachableMessage);
    }

    @Override
    public void prepareDetach(Xid xid, DetachableMessage detachableMessage) throws BrokerException {
        queue.prepareDetach(xid, detachableMessage);
    }

    @Override
    public int clear() {
        return queue.clear();
    }

    private void publishQueueLimitReachedEvent(String type, QueueHandler queueHandler) {
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
            String id = type + queueName + "." + queueSize;
            eventSync.publish(id, properties);
        }
    }
}
