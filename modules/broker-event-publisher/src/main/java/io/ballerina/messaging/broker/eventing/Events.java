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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Used to load the necessary event publisher.
 */
class Events {

    private static final Logger logger = LoggerFactory.getLogger(Events.class);

    private boolean isEnabled;
    private EventSync eventSync;
    private String publisherName;

    Events(ConfigProvider configProvider) {

        EventConfiguration eventsConfig;

        try {
            eventsConfig = configProvider.getConfigurationObject(EventConfiguration.class);
            this.isEnabled = eventsConfig.isEnabled();
            if (isEnabled) {
                publisherName = eventsConfig.getPublisher();
            }

        } catch (ConfigurationException var8) {
            logger.error("Error loading Events Configuration", var8);
        }


        startPublisher();
    }

    private void startPublisher() {
        if (isEnabled) {
            if (Objects.nonNull(publisherName)) {
                ServiceLoader<EventSync> loader = ServiceLoader.load(EventSync.class);
                Iterator<EventSync> iterator = loader.iterator();
                String id;

                while (iterator.hasNext()) {

                    EventSync implementation = iterator.next();
                    id = implementation.getName();

                    if (id.equals(publisherName)) {
                        eventSync = implementation;
                        return;
                    }
                }
                eventSync = null;

                logger.info("You have inserted an invalid event publisher name");

            }
        } else {
            eventSync = null;
        }
    }


    EventSync getEventSync() {

        return this.eventSync;
    }

    void activate() {

        if (isEnabled) {
            eventSync.activate();
        }

    }

    void deactivate() {

        eventSync.deactivate();
    }

}
