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
 * AMQP channel view representation exposes the common details regarding an AMQP channel.
 */
public class AmqpChannelWrapper implements AmqpChannelView {

    private final AmqpChannelView channel;

    public AmqpChannelWrapper(AmqpChannelView channel) {
        this.channel = channel;
    }

    @Override
    public int getChannelId() {
        return channel.getChannelId();
    }

    @Override
    public int getConsumerCount() {
        return channel.getConsumerCount();
    }

    @Override
    public int getUnackedMessageCount() {
        return channel.getUnackedMessageCount();
    }

    @Override
    public int getDeliveryPendingMessageCount() {
        return channel.getDeliveryPendingMessageCount();
    }

    @Override
    public String getTransactionType() {
        return channel.getTransactionType();
    }

    @Override
    public int getPrefetchCount() {
        return channel.getPrefetchCount();
    }

    @Override
    public long getCreatedTime() {
        return channel.getCreatedTime();
    }

    @Override
    public boolean isClosed() {
        return channel.isClosed();
    }

    @Override
    public boolean isFlowEnabled() {
        return channel.isFlowEnabled();
    }
}
