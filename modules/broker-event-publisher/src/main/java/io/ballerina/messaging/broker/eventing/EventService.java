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

import java.util.Objects;

/**
 * Used to register an event service to the broker.
 */
public class EventService {

    private final Events events;

    public EventService(StartupContext context) {

        BrokerConfigProvider configProvider = context.getService(BrokerConfigProvider.class);
        events = new Events(new CarbonConfigAdapter(configProvider));

        if (Objects.nonNull(events.getEventSync())) {
            context.registerService(EventSync.class, events.getEventSync());
        }
    }

    public void start() {
        events.activate();
    }

    public void stop() {
        events.deactivate();
    }
}
