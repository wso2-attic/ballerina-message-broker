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

package io.ballerina.messaging.broker.core.events;

import io.ballerina.messaging.broker.common.EventConstants;
import io.ballerina.messaging.broker.core.QueueHandler;
import io.ballerina.messaging.broker.eventing.EventSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link BrokerEventManager}.
 */
public class DefaultBrokerEventManager implements BrokerEventManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBrokerEventManager.class);
    private EventSync eventSync;

    public DefaultBrokerEventManager(EventSync eventSync) {
        this.eventSync = eventSync;
        logger.info("Default Event Manager Declared");
    }

    public void queueCreated(QueueHandler queueHandler) {
        Map<String, String> properties = new HashMap<>();
        String queueName = queueHandler.getUnmodifiableQueue().getName();
        String isAutoDelete = String.valueOf(queueHandler.getUnmodifiableQueue().isAutoDelete());
        String isDurable = String.valueOf(queueHandler.getUnmodifiableQueue().isDurable());
        properties.put("queueName", queueName);
        properties.put("autoDelete", isAutoDelete);
        properties.put("durable", isDurable);
        eventSync.publish(EventConstants.QUEUE_CREATED, properties);
    }
}
