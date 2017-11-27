/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.amqp;

import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Consumer;
import org.wso2.broker.core.Message;

/**
 * AMQP based message consumer
 */
public class AmqpConsumer implements Consumer {

    private final String queueName;

    private final String consumerTag;

    private final boolean isExclusive;

    public AmqpConsumer(String queueName, String consumerTag, boolean isExclusive) {
        this.queueName = queueName;
        this.consumerTag = consumerTag;
        this.isExclusive = isExclusive;
    }

    @Override
    public void send(Message message, long deliveryTag) throws BrokerException {

    }

    @Override
    public String getSubscribedQueue() {
        return queueName;
    }

    @Override
    public void close() throws BrokerException {

    }

    @Override
    public String getConsumerTag() {
        return consumerTag;
    }

    @Override
    public boolean isExclusive() {
        return isExclusive;
    }
}
