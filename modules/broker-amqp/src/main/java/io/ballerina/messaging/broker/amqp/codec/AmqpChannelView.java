/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.amqp.codec;

/**
 * Channel interface represents common getters for the common properties.
 */
public interface AmqpChannelView {

    /**
     * Getter for the channel identifier.
     * <p>
     * The channel identifier is an auto incrementing integer starting from 1. The identifier is unique within a
     * connection.
     *
     * @return channel identifier
     */
    public int getChannelId();

    /**
     * Gets the active consumer count registered on a channel.
     *
     * @return integer representing the active consumer count
     */
    public int getConsumerCount();

    /**
     * Gets the number of messages that have been delivered but not yet acknowledged.
     *
     * @return integer representing the number of unacknowledged of messages
     */
    public int getUnackedMessageCount();

    /**
     * Gets the number of messages that are to be delivered.
     *
     * @return integer representing the number of messages scheduled to be delivered
     */
    public int getDeliveryPendingMessageCount();

    /**
     * Gets the transaction type used by the channel.
     *
     * @return one of "AutoCommit", "LocalTransaction", "DistributedTransaction"
     */
    public String getTransactionType();

    /**
     * Gets the number of messages that will be prefetched.
     *
     * @return number of prefetched messages
     */
    public int getPrefetchCount();

    /**
     * Returns the time at which the channel was created.
     *
     * @return long representing the time
     */
    public long getCreatedTime();

    /**
     * Indicates if the channel is closed by client.
     *
     * @return true if channel is closed, false otherwise
     */
    public boolean isClosed();

    /**
     * Indicates if flow is enabled for the channel.
     *
     * @return true if flow is enabled. false otherwise
     */
    public boolean isFlowEnabled();
}
