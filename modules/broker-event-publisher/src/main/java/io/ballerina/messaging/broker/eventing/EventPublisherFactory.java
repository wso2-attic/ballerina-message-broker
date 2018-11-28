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

import io.ballerina.messaging.broker.common.BrokerClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to load registered EventPublishers.
 */
class EventPublisherFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherFactory.class);

    /**
     * Provides an instance of @{@link EventSync}.
     *
     * @param eventConfiguration the event configuration
     * @return publisher for given configuration
     */
    EventSync getPublisher(EventConfiguration eventConfiguration) throws IllegalAccessException,
            InstantiationException, ClassNotFoundException {
        EventSync publisher;
        String publisherClass = eventConfiguration.getPublisherClassName();
        LOGGER.info("Initializing Event Publisher: {}", publisherClass);
        publisher = BrokerClassLoader.loadClass(publisherClass, EventSync.class);
        return publisher;
    }

}
