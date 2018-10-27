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

import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.eventing.EventSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.Metrics;

import java.util.Map;
import java.util.Objects;

/**
 * Implementation of BrokerPublisher for {@link EventSync}.
 */
public class EventPublisher implements EventSync {

    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);
    private CorePublisher exchangePublisher = new NullCorePublisher();
    private Broker broker = null;
    private String name = "brokerpublisher";

    @Override
    public String getName() {
        return name;
    }

    public void publish(int id, Map<String, String> properties) {

        exchangePublisher.publishNotification(id, properties);

    }

    public void setBroker(Broker broker) {

        this.broker = broker;
    }

    public void activate() {

        if (Objects.nonNull(this.broker)) {
            this.exchangePublisher = new DefaultCorePublisher(this.broker);
        }
    }

    public void deactivate() {
        this.exchangePublisher = new NullCorePublisher();
    }

}
