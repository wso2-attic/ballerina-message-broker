package org.wso2.broker.core.store.dao;

import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Message;

import javax.sql.DataSource;

/**
 * Defines a functionality required for manipulating messages in persistent storage.
 */
public abstract class MessageDao extends BaseDao {

    public MessageDao(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Storage a message in the persistant storage.
     * 
     * @param message the message.
     */
    public abstract void persist(Message message) throws BrokerException;

    /**
     * Removes the linkage a messages has with given queue. after the removal if there are links to any other queues
     * this message should be deleted automatically.
     * 
     * @param queueName name of the queue
     * @param messageId Id of the message
     */
    public abstract void detachFromQueue(String queueName, Long messageId) throws BrokerException;

    /**
     * Deletes a given message and its associations to a queues.
     * 
     * @param messageId internal message id
     */
    public abstract void delete(Long messageId) throws BrokerException;

    /**
     * Retrieve all messages from a given queue.
     * @param queueName name of the queue.
     */
    public abstract void readAll(String queueName) throws BrokerException;
}
