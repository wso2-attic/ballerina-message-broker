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

package io.ballerina.messaging.broker.core;

import io.ballerina.messaging.broker.common.EventSync;
import io.ballerina.messaging.broker.core.store.dao.QueueDao;

import java.util.Objects;

/**
 * Factory for creating queue registry objects.
 */
public class QueueRegistryFactory {

    private QueueDao queueDao;
    private EventSync eventSync;
    private QueueHandlerFactory queueHandlerFactory;

    public QueueRegistryFactory(QueueDao queueDao, QueueHandlerFactory queueHandlerFactory, EventSync eventSync) {

        this.eventSync = eventSync;
        this.queueHandlerFactory = queueHandlerFactory;
        this.queueDao = queueDao;
    }

    /**
     * Create a observable or a non observable queue registry with the give arguments.
     * @return QueueRegistryImpl object
     */
    public QueueRegistry getQueueRegistry() throws BrokerException {
        if (Objects.nonNull(eventSync)) {
            QueueRegistryImpl queueRegistry = new QueueRegistryImpl(queueDao, queueHandlerFactory);
            return new ObservableQueueRegistryImpl(queueRegistry, eventSync);
        } else {
            return new QueueRegistryImpl(queueDao, queueHandlerFactory);
        }
    }

}
