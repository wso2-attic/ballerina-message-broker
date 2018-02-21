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

package io.ballerina.messaging.broker.coordination.rdbms;

import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.coordination.AbstractHaStrategy;
import io.ballerina.messaging.broker.coordination.BrokerHaConfiguration;

import javax.sql.DataSource;

/**
 * The RDBMS coordination based HA strategy implementation.
 */
public class RdbmsHaStrategy extends AbstractHaStrategy implements RdbmsCoordinationListener {

    /**
     * Reference to {@link RdbmsCoordinationStrategy} upon which this HA strategy is based.
     */
    private RdbmsCoordinationStrategy rdbmsCoordinationStrategy;

    /**
     * {@inheritDoc}
     */
    public void setup(StartupContext startupContext) throws Exception {
        DataSource dataSource = startupContext.getService(DataSource.class);
        BrokerHaConfiguration brokerHaConfiguration = startupContext.getService(BrokerHaConfiguration.class);
        rdbmsCoordinationStrategy = new RdbmsCoordinationStrategy(new RdbmsCoordinationDaoImpl(dataSource),
                brokerHaConfiguration.getOptions());
        rdbmsCoordinationStrategy.addCoordinationListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        rdbmsCoordinationStrategy.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActiveNode() {
        return rdbmsCoordinationStrategy.isCoordinator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        super.stop();
        rdbmsCoordinationStrategy.stop();
    }

    public void pause() {
        rdbmsCoordinationStrategy.pause();
    }

    public void resume() {
        rdbmsCoordinationStrategy.resume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void becameCoordinatorNode() {
        notifyBecameActiveNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lostCoordinatorState() {
        notifyBecamePassiveNode();
    }
}
