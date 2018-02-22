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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Representation of a listener listening on node state changes defining basic functionality on node state change.
 */
public class BasicHaListener implements HaListener {

    private HaListener haListener;

    /**
     * Whether or not the node represented by the listener is the active node.
     */
    private AtomicBoolean active = new AtomicBoolean(false);

    /**
     * Boolean to keep track of whether a relevant start method was explicitly called for the {@link #haListener} -
     * component implementing the {@link HaListener} interface.
     */
    private volatile boolean startCalled = false;

    public BasicHaListener(HaListener haListener) {
        this.haListener = haListener;
    }

    /**
     * Default implementation of method to be invoked once the node state changes to active.
     */
    public void activate() {
        active.set(true);
        haListener.activate();
    }

    /**
     * Default implementation of method to be invoked once the node state changes to passive.
     */
    public void deactivate() {
        active.set(false);
        haListener.deactivate();
    }

    /**
     * Method to retrieve if the listener registered corresponds to the active node.
     *
     * @return true if the listener registered corresponds to the active node
     */
    public boolean isActive() {
        return active.get();
    }

    /**
     * Method to retrieve whether start was called explicitly.
     *
     * @return true if start was called explicitly
     */
    public boolean isStartCalled() {
        return startCalled;
    }

    /**
     * Method to indicate that start was called explicitly.
     */
    public void setStartCalled() {
        this.startCalled = true;
    }
}
