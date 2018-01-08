package org.wso2.broker.core.store.dao;

import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Queue;

import java.util.function.Consumer;
import javax.sql.DataSource;

/**
 * Defines functionality required at persistence layer for managing {@link Queue}s.
 */
public abstract class QueueDao extends BaseDao {

    public QueueDao(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Save a Queue in persistent storage.
     * 
     * @param queue the queue
     */
    public abstract void persist(Queue queue) throws BrokerException;

    /**
     * Remove a queue from persistent storage.
     * 
     * @param queue the queue.
     */
    public abstract void delete(Queue queue) throws BrokerException;

    public abstract void retrieveAll(Consumer<String> queueNameCollector) throws BrokerException;
}
