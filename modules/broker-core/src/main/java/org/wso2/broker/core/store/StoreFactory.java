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
import org.wso2.broker.core.metrics.BrokerMetricManager;
import org.wso2.broker.core.store.dao.DaoFactory;
import org.wso2.broker.core.store.dao.SharedMessageStore;

import javax.sql.DataSource;

/**
 * Factory class for store backed objects.
 */
public class StoreFactory {

    private final DaoFactory daoFactory;
    private final BrokerMetricManager metricManager;

    public StoreFactory(DataSource dataSource, BrokerMetricManager metricManager) {
        daoFactory = new DaoFactory(dataSource);
        this.metricManager = metricManager;
    }

    public ExchangeRegistry getExchangeRegistry() {
        return new ExchangeRegistry(daoFactory.createExchangeDao(), daoFactory.createBindingDao());
    }

    public SharedMessageStore getSharedMessageStore(int bufferSize, int maxDbBatchSize) {
        return new SharedMessageStore(daoFactory.createMessageDao(), bufferSize, maxDbBatchSize);
    }

    public QueueRegistry getQueueRegistry(SharedMessageStore messageStore) throws BrokerException {
        return new QueueRegistry(daoFactory.createQueueDao(), new QueueHandlerFactory(messageStore, metricManager));
    }
}
