package org.wso2.broker.core.store.dao;

import org.wso2.broker.core.Message;

/**
 * Defines a functionality required for manipulating messages in persistent storage.
 */
public interface MessageDao {

    /**
     * Storage a message in the persistant storage
     * 
     * @param message the message.
     */
    void persist(Message message);

    /**
     * Removes the linkage a messages has with given queue. after the removal if there are links to any other queues
     * this message should be deleted automatically.
     * 
     * @param queueName name of the queue
     * @param messageId Id of the message
     */
    void detachFromQueue(String queueName, Long messageId);

    /**
     * Deletes a given message and its associations to a queues.
     * 
     * @param messageId
     */
    void delete(Long messageId);

    /**
     * Retrieve all messages from a given queue.
     * @param queueName name of the queue.
     */
    void readAll(String queueName);
}
