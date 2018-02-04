package org.wso2.broker.core.store.dao;

import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.store.DbOperation;

import java.util.Collection;
import javax.sql.DataSource;

/**
 * Defines a functionality required for manipulating messages in persistent storage.
 */
public abstract class MessageDao extends BaseDao {

    public MessageDao(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Storage a message in the persistent storage.
     * 
     * @param messageList the messages to persist.
     */
    public abstract void persist(Collection<Message> messageList) throws BrokerException;

    /**
     * Removes the linkage a messages has with given queue. after the removal if there are links to any other queues
     * this message should be deleted automatically.
     * 
     * @param dbOperations {@link DbOperation} objects which contain the queue names and the message ids to detach.
     */
    public abstract void detachFromQueue(Collection<DbOperation> dbOperations) throws BrokerException;

    /**
     * Deletes a given message and its associations to a queues.
     *
     * @param messageId internal message ids
     */
    public abstract void delete(Collection<Long> messageId) throws BrokerException;

    /**
     * Retrieve all messages from a given queue.
     * @param queueName name of the queue.
     */
    public abstract Collection<Message> readAll(String queueName) throws BrokerException;
}
