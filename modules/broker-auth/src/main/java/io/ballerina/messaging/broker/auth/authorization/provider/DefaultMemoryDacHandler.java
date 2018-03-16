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

package io.ballerina.messaging.broker.auth.authorization.provider;

import io.ballerina.messaging.broker.auth.AuthServerException;
import io.ballerina.messaging.broker.auth.authorization.UserStore;
import io.ballerina.messaging.broker.common.StartupContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This implementation used in the broker auth configuration with default auth resources
 */
public class DefaultMemoryDacHandler extends MemoryDacHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMemoryDacHandler.class);

    @Override
    public void initialize(StartupContext startupContext, UserStore userStore, Map<String, String> properties) {
        super.initialize(startupContext, userStore, properties);
        try {
            persistDefaultAuthResources();
        } catch (AuthServerException e) {
            LOGGER.error("Error occurred while persisting auth resources.", e);
        }
    }

    private void persistDefaultAuthResources() throws AuthServerException {
        addResource("exchange", "<<default>>", "admin");
        addResource("exchange", "amq.direct", "admin");
        addResource("exchange", "amq.topic", "admin");
        addResource("queue", "amq.dlq", "admin");
    }
}
