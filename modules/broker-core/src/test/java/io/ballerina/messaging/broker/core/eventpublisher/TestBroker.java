package io.ballerina.messaging.broker.core.eventpublisher;


import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.core.BindingSet;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.Consumer;
import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Exchange;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.QueueHandler;
import io.ballerina.messaging.broker.core.transaction.BrokerTransaction;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.transaction.xa.Xid;

public class TestBroker implements Broker {

    private Message message;

    public Message getMessage() {
        return this.message;
    }

    public void clearMessage() {
        message = null;
    }

    @Override
    public void publish(Message message) {
        this.message = message;
    }

    @Override
    public void acknowledge(String queueName, DetachableMessage detachableMessage) {

    }

    @Override
    public Set<QueueHandler> enqueue(Xid xid, Message message) {

        return null;
    }

    @Override
    public QueueHandler dequeue(Xid xid, String queueName, DetachableMessage detachableMessage) {

        return null;
    }

    @Override
    public void addConsumer(Consumer consumer) {

    }

    @Override
    public boolean removeConsumer(Consumer consumer) {

        return false;
    }

    @Override
    public void declareExchange(String exchangeName, String type, boolean passive, boolean durable) {

    }

    @Override
    public void createExchange(String exchangeName, String type, boolean durable) {

    }

    @Override
    public boolean deleteExchange(String exchangeName, boolean ifUnused) {

        return false;
    }

    @Override
    public boolean createQueue(String queueName, boolean passive, boolean durable, boolean autoDelete,
                               FieldTable arguments) {

        return false;
    }

    @Override
    public int deleteQueue(String queueName, boolean ifUnused, boolean ifEmpty) {

        return 0;
    }

    @Override
    public boolean queueExists(String queueName) {

        return false;
    }

    @Override
    public void bind(String queueName, String exchangeName, String routingKey, FieldTable arguments) {

    }

    @Override
    public void unbind(String queueName, String exchangeName, String routingKey) {

    }

    @Override
    public void startMessageDelivery() {

    }

    @Override
    public int purgeQueue(String queueName) {

        return 0;
    }

    @Override
    public void stopMessageDelivery() {

    }

    @Override
    public QueueHandler getQueue(String queueName) {

        return null;
    }

    @Override
    public void requeue(String queueName, Message message) {

    }

    @Override
    public Collection<QueueHandler> getAllQueues() {

        return null;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Collection<Exchange> getAllExchanges() {

        return null;
    }

    @Override
    public void moveToDlc(String queueName, Message message) {

    }

    @Override
    public Map<String, BindingSet> getAllBindingsForExchange(String exchangeName) {

        return null;
    }

    @Override
    public Exchange getExchange(String exchangeName) {

        return null;
    }

    @Override
    public BrokerTransaction newLocalTransaction() {

        return null;
    }

    @Override
    public BrokerTransaction newDistributedTransaction() {

        return null;
    }

    @Override
    public Set<QueueHandler> restoreDtxPreparedMessages(Xid xid, Collection<Message> messages) {

        return null;
    }
}
