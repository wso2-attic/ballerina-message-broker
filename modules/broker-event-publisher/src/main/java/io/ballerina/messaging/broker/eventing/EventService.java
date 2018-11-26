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

package io.ballerina.messaging.broker.eventing;

import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

/**
 * Used to register an event service to the broker.
 */
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private boolean isEnabled;
    private EventSync eventSync = null;


    public EventService(StartupContext context) throws Exception {
        BrokerConfigProvider configProvider = context.getService(BrokerConfigProvider.class);
        EventConfiguration eventConfiguration = configProvider.getConfigurationObject(EventConfiguration.NAMESPACE,
                                                                                        EventConfiguration.class);

        getEventPublisher(eventConfiguration);

        if (Objects.nonNull(eventSync)) {
            context.registerService(EventSync.class, eventSync);
        }
    }


    /**
     * Used to get the registered event publisher in EventConfiguration.
     *
     * @param eventConfiguration Event Configuration
     */
    private void getEventPublisher(EventConfiguration eventConfiguration) {

        try {
            this.isEnabled = eventConfiguration.isEnabled();
            if (isEnabled) {
                eventSync = new EventPublisherFactory().getPublisher(eventConfiguration);
            }
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            logger.error(e.toString());
            isEnabled = false;
        }
    }

    public void start() {
        if (isEnabled) {
            eventSync.activate();
        }
    }

    public void stop() {
        if (Objects.nonNull(eventSync)) {
            eventSync.deactivate();
        }
    }
}
