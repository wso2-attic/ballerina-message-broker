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

package io.ballerina.messaging.broker.core.store;

import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.ExchangeRegistry;
import io.ballerina.messaging.broker.core.MemBackedQueueHandlerFactory;
import io.ballerina.messaging.broker.core.QueueRegistry;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.metrics.BrokerMetricManager;
import io.ballerina.messaging.broker.core.store.dao.impl.NullBindingDao;
import io.ballerina.messaging.broker.core.store.dao.impl.NullExchangeDao;
import io.ballerina.messaging.broker.core.store.dao.impl.NullQueueDao;

/**
 * Memory backed store used when broker is operating in in-memory mode.
 */
public class MemBackedStoreFactory implements StoreFactory {
    private final BrokerMetricManager metricManager;
    private final BrokerCoreConfiguration configuration;

    /**
     * Null object used to represent the database access layer in in-memory mode.
     */
    private NullMessageStore messageStore = new NullMessageStore();

    public MemBackedStoreFactory(BrokerMetricManager metricManager,
                                 BrokerCoreConfiguration configuration) {
        this.metricManager = metricManager;
        this.configuration = configuration;
    }

    @Override
    public ExchangeRegistry getExchangeRegistry() {
        return new ExchangeRegistry(new NullExchangeDao(), new NullBindingDao());
    }

    @Override
    public MessageStore getMessageStore() {
        return messageStore;
    }

    @Override
    public QueueRegistry getQueueRegistry() throws BrokerException {
        return new QueueRegistry(new NullQueueDao(), new MemBackedQueueHandlerFactory(metricManager, configuration));
    }
}
