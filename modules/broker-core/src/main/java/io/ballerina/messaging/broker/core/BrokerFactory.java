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
 */

package io.ballerina.messaging.broker.core;

import javax.security.auth.Subject;

/**
 * Generic broker factory to get the broker object based on implementation
 */
public interface BrokerFactory {

    /**
     * Get {@link Broker} implementation based on authorization enabled or disabled
     *
     * @param subject entity has {@link io.ballerina.messaging.broker.auth.UsernamePrincipal}
     * @return {@link Broker} object
     */
    Broker getBroker(Subject subject);
}
