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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;

/**
 * Abstract class representing default implementations of HA strategy.
 */
public abstract class AbstractHaStrategy implements HaStrategy {

    /**
     * Class logger.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractHaStrategy.class);

    /**
     * Listeners listening on notifications for node state changes.
     */
    private TreeSet<PrioritizedHaListener> prioritizedHaListeners = new TreeSet<>();

    /**
     * Executor service to asynchronously notify listeners.
     */
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * {@inheritDoc}
     */
    public void registerListener(HaListener haListener, int priority) {
        prioritizedHaListeners.add(new PrioritizedHaListener(haListener, priority));
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterListener(HaListener haListener) {
        Iterator<PrioritizedHaListener> prioritizedHaListenerIterator = prioritizedHaListeners.iterator();
        PrioritizedHaListener prioritizedHaListener;
        while (prioritizedHaListenerIterator.hasNext()) {
            prioritizedHaListener = prioritizedHaListenerIterator.next();
            if (prioritizedHaListener.haListener.equals(haListener)) {
                prioritizedHaListenerIterator.remove();
            }
        }
    }

    /**
     * Method to notify becoming the active node to all listeners.
     */
    @SuppressFBWarnings(
            value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
            justification = "Return future is ignored since the execution needs be done asynchronously.")
    protected void notifyBecameActiveNode() {
        LOGGER.info("Current node became the ACTIVE node");
        for (PrioritizedHaListener prioritizedHaListener : prioritizedHaListeners) {
            executorService.submit(prioritizedHaListener.haListener::activate);
        }
    }

    /**
     * Method to notify becoming the passive node to all listeners.
     */
    @SuppressFBWarnings(
            value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
            justification = "Return future is ignored since the execution needs be done asynchronously.")
    protected void notifyBecamePassiveNode() {
        LOGGER.info("Current node became a PASSIVE node");
        Iterator<PrioritizedHaListener> prioritizedHaListenerIterator = prioritizedHaListeners.descendingIterator();
        while (prioritizedHaListenerIterator.hasNext()) {
            executorService.submit(prioritizedHaListenerIterator.next().haListener::deactivate);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        executorService.shutdown();
    }

    /**
     * {@link HaListener} with specified priority.
     */
    private static class PrioritizedHaListener implements Comparable<PrioritizedHaListener> {

        private HaListener haListener;

        private int priority;

        PrioritizedHaListener(HaListener haListener, int priority) {
            this.haListener = haListener;
            this.priority = priority;
        }

        @Override
        public int compareTo(@Nonnull PrioritizedHaListener prioritizedHaListener) {
            return priority - prioritizedHaListener.priority;
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || !(object instanceof PrioritizedHaListener)) {
                return false;
            }
            PrioritizedHaListener prioritizedHaListener = (PrioritizedHaListener) object;
            return haListener.equals(prioritizedHaListener.haListener);
        }

        @Override
        public int hashCode() {
            return haListener.hashCode();
        }

    }

}
