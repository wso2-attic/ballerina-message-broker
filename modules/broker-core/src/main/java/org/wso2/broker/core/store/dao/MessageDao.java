package org.wso2.broker.core.store.dao;

import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.store.DbOperation;

import java.util.Collection;

/**
 * Defines a functionality required for manipulating messages in persistent storage.
 */
public interface MessageDao {

    /**
     * Storage a message in the persistent storage.
     * 
     * @param messageList the messages to persist.
     */
    void persist(Collection<Message> messageList) throws BrokerException;

    /**
     * Removes the linkage a messages has with given queue. after the removal if there are links to any other queues
     * this message should be deleted automatically.
     * 
     * @param dbOperations {@link DbOperation} objects which contain the queue names and the message ids to detach.
     */
    void detachFromQueue(Collection<DbOperation> dbOperations) throws BrokerException;

    /**
     * Deletes a given message and its associations to a queues.
     *
     * @param messageId internal message ids
     */
    void delete(Collection<Long> messageId) throws BrokerException;

    /**
     * Retrieve all messages from a given queue.
     * @param queueName name of the queue.
     */
    Collection<Message> readAll(String queueName) throws BrokerException;

}
