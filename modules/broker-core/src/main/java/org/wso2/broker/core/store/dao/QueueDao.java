package org.wso2.broker.core.store.dao;

import org.wso2.broker.core.Queue;

/**
 * Defines functionality required at persistence layer for managing {@link Queue}s.
 */
public interface QueueDao {

    /**
     * Save a Queue in persistent storage.
     * 
     * @param queue the queue
     */
    void persist(Queue queue);

    /**
     * Remove a queue from persistent storage.
     * 
     * @param queue the queue.
     */
    void delete(Queue queue);
}
