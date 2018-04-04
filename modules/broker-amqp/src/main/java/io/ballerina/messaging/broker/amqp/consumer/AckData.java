/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.amqp.consumer;

import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Message;

/**
 * Used to keep record of unacked messages.
 */
public class AckData {
    private final Message message;
    private final String queueName;
    private final ShortString consumerTag;

    public AckData(Message message, String queueName, ShortString consumerTag) {
        this.message = message;
        this.queueName = queueName;
        this.consumerTag = consumerTag;
    }

    /**
     * Getter for message.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Getter for queueName.
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Getter for consumerTag.
     */
    public ShortString getConsumerTag() {
        return consumerTag;
    }
}
