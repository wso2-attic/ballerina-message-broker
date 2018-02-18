package org.wso2.broker.core.store.dao;

import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.store.TransactionData;

import java.util.Collection;
import java.util.Map;

/**
 * Defines a functionality required for manipulating messages in persistent storage.
 */
public interface MessageDao {

    /**
     * Update database with message storing, deleting and detaching from queue operations.
     * All operations are done in a single transaction.
     *
     * @param transactionData {@link TransactionData} object which transactional operations list
     */
    void persist(TransactionData transactionData) throws BrokerException;

    /**
     * Retrieve all messages from a given queue.
     *
     * @param queueName name of the queue.
     */
    Collection<Message> readAll(String queueName) throws BrokerException;

    /**
     * Read message data for given messages.
     *
     * @param readList list of messages.
     */
    Collection<Message> read(Map<Long, Message> readList) throws BrokerException;
}
