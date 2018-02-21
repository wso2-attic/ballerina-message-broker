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

package io.ballerina.messaging.broker.coordination;

import io.ballerina.messaging.broker.common.StartupContext;

/**
 * Representation of the HA strategy.
 */
public interface HaStrategy {

    /**
     * Setup based on {@link StartupContext} if required (eg:- read configs).
     *
     * @param startupContext the startup context from which registered services can be retrieved
     */
    void setup(StartupContext startupContext) throws Exception;

    /**
     * Meant to be invoked when the HA strategy should start.
     */
    void start();

    /**
     * Meant to be invoked when the HaStrategy should stop.
     */
    void stop();

    /**
     * Method to identify if the local node is the active node.
     */
    boolean isActiveNode();

    /**
     * Method to register a listener to listen on notifications for node state changes with priority level.
     *
     * @param haListener the {@link HaListener} to register
     * @param priority   the priority level when notifying node state changes to the haListener
     */
    void registerListener(HaListener haListener, int priority);

    /**
     * Method to unregister a listener registered to listen on notifications for node state changes.
     *
     * @param haListener the {@link HaListener} to unregister
     */
    void unregisterListener(HaListener haListener);

}
