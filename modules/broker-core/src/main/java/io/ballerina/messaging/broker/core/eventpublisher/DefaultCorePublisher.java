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
package io.ballerina.messaging.broker.core.eventpublisher;

import io.ballerina.messaging.broker.common.EventConstants;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link CorePublisher}.
 */
public class DefaultCorePublisher implements CorePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCorePublisher.class);
    private Broker broker;
    private String exchangeName = "event";
    private int count;

    DefaultCorePublisher(Broker broker) {

        this.broker = broker;
    }

    @Override
    public void publishNotification(int id, Map<String, String> properties) {

        if (id == EventConstants.MESSAGEPUBLISHEDEVENT) {
            String publishedExchangeName = properties.get("ExchangeName");

            if (publishedExchangeName.equals(exchangeName)) {
                return;
            }

        }

        Map<ShortString, FieldValue> notificationProperties = new HashMap<>();

        for (Map.Entry<String, String> entry : properties.entrySet()) {

            ShortString key = ShortString.parseString(entry.getKey());
            String obj = entry.getValue();
            FieldValue fieldValue = FieldValue.parseShortString(obj);
            notificationProperties.put(key, fieldValue);

        }

        Metadata metadata = new Metadata(getRoutingKey(id, properties), exchangeName, 0);
        metadata.setHeaders(new FieldTable(notificationProperties));

        FieldTable messageProperties = new FieldTable();
        byte property = 0x00;
        messageProperties.add(ShortString.parseString("propertyFlags"), FieldValue.parseShortShortInt(property));
        metadata.setProperties(messageProperties);

        Message notificationMessage = new Message(count++, metadata);

        try {
            broker.publish(notificationMessage);
        } catch (BrokerException e) {
            LOGGER.info(e.toString());
        }

    }

    public String getRoutingKey(int id, Map<String, String> properties) {

        String routingKey;

        if (id == EventConstants.CONSUMERADDEDEVENT) {
            routingKey = "consumer.added";
        } else if (id == EventConstants.MESSAGEPUBLISHEDEVENT) {
            routingKey = "message.published";
        } else if (id == EventConstants.QUEUECREATED) {
            routingKey = "queue.created";

        } else if (id == EventConstants.BINDINGCREATED) {
            routingKey = "binding.created" + properties.get("BindingName");
        } else {
            routingKey = null;
        }
        return routingKey;

    }

    void setExchangeName(String exchangeName) {

        this.exchangeName = exchangeName;
    }

}
