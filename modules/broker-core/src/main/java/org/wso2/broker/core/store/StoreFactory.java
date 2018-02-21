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

package org.wso2.broker.core.store;

import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.ExchangeRegistry;
import org.wso2.broker.core.QueueHandlerFactory;
import org.wso2.broker.core.QueueRegistry;
import org.wso2.broker.core.configuration.BrokerConfiguration;
import org.wso2.broker.core.metrics.BrokerMetricManager;
import org.wso2.broker.core.store.dao.impl.DaoFactory;

import javax.sql.DataSource;

/**
 * Factory class for store backed objects.
 */
public class StoreFactory {

    private final DaoFactory daoFactory;
    private final BrokerMetricManager metricManager;
    private final BrokerConfiguration configuration;

    private SharedMessageStore sharedMessageStore;

    public StoreFactory(DataSource dataSource, BrokerMetricManager metricManager, BrokerConfiguration configuration) {
        daoFactory = new DaoFactory(dataSource, metricManager);
        this.metricManager = metricManager;
        this.configuration = configuration;
        sharedMessageStore = new SharedMessageStore(daoFactory.createMessageDao(), 32768, 1024);
    }

    /**
     * Create exchange registry
     * @return ExchangeRegistry object
     */
    public ExchangeRegistry getExchangeRegistry() {
        return new ExchangeRegistry(daoFactory.createExchangeDao(), daoFactory.createBindingDao());
    }

    /**
     * Create message registry
     * @return SharedMessageStore object
     */
    public SharedMessageStore getSharedMessageStore() {
        return sharedMessageStore;
    }

    /**
     * Create queue registry
     * @return QueueRegistry object
     */
    public QueueRegistry getQueueRegistry(SharedMessageStore messageStore) throws BrokerException {
        return new QueueRegistry(daoFactory.createQueueDao(),
                                 new QueueHandlerFactory(messageStore, metricManager, configuration));
    }
}
