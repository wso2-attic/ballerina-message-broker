package org.wso2.broker.core;

/**
 * Represents the queue of the broker. Contains a bounded queue to store messages.
 */
public class Queue {

    /**
     * Name of the queue
     */
    private String name;

    /**
     * 
     */
    private boolean passive;

    private boolean durable;

    private boolean autoDelete;

    private int capacity;

    /**
     * Creates a queue
     * 
     * @param name
     * @param passive
     * @param durable
     * @param autoDelete
     * @param capacity
     */
    public Queue(String name, boolean passive, boolean durable, boolean autoDelete, int capacity) {
        this.name = name;
        this.passive = passive;
        this.durable = durable;
        this.autoDelete = autoDelete;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPassive() {
        return passive;
    }

    /**
     * If true the queue will be durable. Durable queues remain active when the broker restarts
     * Non­durable queues (transient queues) are purged if/when the broker restarts
     *
     * @return True if the queue is durable. False otherwise
     */
    public boolean isDurable() {
        return durable;
    }

    /**
     * If true queue can be deleted once there are no consumers for the queue
     *
     * @return True if the queue is auto deletable
     */
    public boolean isAutoDelete() {
        return autoDelete;
    }
    
    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return "Queue [name=" + name + ", passive=" + passive + ", durable=" + durable + ", autoDelete=" + autoDelete
                + ", capacity=" + capacity + "]";
    }

    
    
    
}
