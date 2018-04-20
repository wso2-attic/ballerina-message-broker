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

package io.ballerina.messaging.broker.core.store.dao.impl;

import io.ballerina.messaging.broker.core.ChunkConverter;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.ballerina.messaging.broker.core.metrics.BrokerMetricManager;
import io.ballerina.messaging.broker.core.store.dao.BindingDao;
import io.ballerina.messaging.broker.core.store.dao.ExchangeDao;
import io.ballerina.messaging.broker.core.store.dao.MessageDao;
import io.ballerina.messaging.broker.core.store.dao.QueueDao;

import javax.sql.DataSource;

/**
 * Instantiates different DAO objects required to access persistent storage.
 */
public class DaoFactory {

    private final DataSource dataSource;
    private final BrokerMetricManager metricManager;
    private ChunkConverter chunkConverter;

    public DaoFactory(DataSource dataSource,
                      BrokerMetricManager metricManager,
                      BrokerCoreConfiguration configuration) {
        this.dataSource = dataSource;
        this.metricManager = metricManager;
        int maxPersistedChunkSize = Integer.parseInt(configuration.getMaxPersistedChunkSize());
        chunkConverter = new ChunkConverter(maxPersistedChunkSize);
    }

    public QueueDao createQueueDao() {
        return new QueueDaoImpl(dataSource);
    }

    public MessageDao createMessageDao() {
        return new MessageDaoImpl(new MessageCrudOperationsDao(dataSource, metricManager, chunkConverter),
                                  new DtxCrudOperationsDao(dataSource, chunkConverter));
    }

    public ExchangeDao createExchangeDao() {
        return new ExchangeDaoImpl(dataSource);
    }

    public BindingDao createBindingDao() {
        return new BindingDaoImpl(dataSource);
    }
}
