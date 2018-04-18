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

import com.lmax.disruptor.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Disruptor exception handler.
 */
public class DbStoreExceptionHandler implements ExceptionHandler<DbOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbStoreExceptionHandler.class);

    @Override
    public void handleEventException(Throwable throwable, long sequence, DbOperation event) {
        event.setExceptionObject(throwable);
    }

    @Override
    public void handleOnStartException(Throwable throwable) {
        LOGGER.error("Error while starting Disruptor ", throwable);
    }

    @Override
    public void handleOnShutdownException(Throwable throwable) {
        LOGGER.error("Error while shutting down Disruptor ", throwable);
    }
}
