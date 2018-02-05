package org.wso2.broker.amqp.codec.txn;

/**
 * Provide standard interface to handle enqueue/dequeue operation based on underlying transaction object.
 * A caller may register list of post transactions actions to be performed on commit or rollback operation.
 */
public interface BrokerTransaction {

    /**
     * Dequeue a message from queue by post transaction action
     */
    void dequeue();

    /**
     * Enqueue a message from queue by post transaction action
     */
    void enqueue();

    /**
     * Commit the transaction represent by this object
     */
    void commit();

    /**
     * Rollback the transaction represent by this object
     */
    void rollback();
}
