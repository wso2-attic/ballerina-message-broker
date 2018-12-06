package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.eventing.EventSync;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Represents an Consumer which trigger events for the broker.
 */
public class ObservableConsumer extends Consumer {
    Consumer consumer;
    EventSync eventSync;

    ObservableConsumer(Consumer consumer, EventSync eventSync) {
        this.consumer = consumer;
        this.eventSync = eventSync;
    }

    @Override
    protected void send(Message message) throws BrokerException {
        consumer.send(message);
    }

    @Override
    public String getQueueName() {
        return consumer.getQueueName();
    }

    @Override
    protected void close() throws BrokerException {
        publishConsumerEvent(this.consumer);
        consumer.close();
    }

    @Override
    public boolean isExclusive() {
        return consumer.isExclusive();
    }

    @Override
    public boolean isReady() {
        return consumer.isReady();
    }

    @Override
    public Properties getTransportProperties() {
        return consumer.getTransportProperties();
    }

    private void publishConsumerEvent(Consumer consumer) {
        Map<String, String> properties = new HashMap<>();
        properties.put("consumerID", String.valueOf(consumer.getId()));
        properties.put("queueName", consumer.getQueueName());
        properties.put("ready", String.valueOf(consumer.isReady()));
        properties.put("exclusive", String.valueOf(consumer.isExclusive()));
        eventSync.publish("consumer.removed", properties);
    }
}
