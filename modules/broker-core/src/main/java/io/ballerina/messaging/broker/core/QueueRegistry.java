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

import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.core.store.dao.QueueDao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry object which contains all the queues of the broker.
 */
public final class QueueRegistry {

    private final Map<String, QueueHandler> queueHandlerMap;

    private final QueueDao queueDao;

    private final QueueHandlerFactory queueHandlerFactory;

    public QueueRegistry(QueueDao queueDao, QueueHandlerFactory queueHandlerFactory) throws BrokerException {
        this.queueHandlerMap = new HashMap<>();
        this.queueDao = queueDao;
        this.queueHandlerFactory = queueHandlerFactory;
        retrieveQueuesFromDao();
    }

    QueueHandler getQueueHandler(String queueName) {
        return queueHandlerMap.get(queueName);
    }

    boolean addQueue(String queueName, boolean passive, boolean durable, boolean autoDelete) throws BrokerException {
        QueueHandler queueHandler = queueHandlerMap.get(queueName);

        if (passive) {
            if (Objects.isNull(queueHandler)) {
                throw new BrokerException("Queue [ " + queueName + " ] doesn't exists. Passive parameter "
                                                  + "is set, hence not creating the queue.");
            } else {
                return false;
            }
        } else {
            if (Objects.isNull(queueHandler)) {
                if (durable) {
                    queueHandler = queueHandlerFactory.createDurableQueueHandler(queueName, autoDelete);
                    queueDao.persist(queueHandler.getUnmodifiableQueue());
                } else {
                    queueHandler = queueHandlerFactory.createNonDurableQueueHandler(queueName, autoDelete);
                }
                queueHandlerMap.put(queueName, queueHandler);
                return true;
            } else if (queueHandler.getUnmodifiableQueue().isDurable() != durable
                       || queueHandler.getUnmodifiableQueue().isAutoDelete() != autoDelete) {
                throw new BrokerException(
                        "Existing queue [ " + queueName + " ] does not match given parameters.");
            } else {
                return false;
            }
        }
    }

    int removeQueue(String queueName, boolean ifUnused, boolean ifEmpty) throws BrokerException,
                                                                                ValidationException,
                                                                                ResourceNotFoundException {
        QueueHandler queueHandler = queueHandlerMap.get(queueName);
        if (queueHandler == null) {
            throw new ResourceNotFoundException("Queue [ " + queueName + " ] Not found");
        }

        if (ifUnused && !queueHandler.isUnused()) {
            throw new ValidationException("Cannot delete queue. Queue [ " + queueName
                    + " ] has active consumers and the ifUnused parameter is set.");
        } else if (ifEmpty && !queueHandler.isEmpty()) {
            throw new ValidationException("Cannot delete queue. Queue [ " + queueName
                    + " ] is not empty and the ifEmpty parameter is set.");
        } else {
            queueHandlerMap.remove(queueName);
            queueDao.delete(queueHandler.getUnmodifiableQueue());
            return queueHandler.releaseResources();
        }
    }

    private void retrieveQueuesFromDao() throws BrokerException {
            queueDao.retrieveAll((name) -> {
                QueueHandler handler = queueHandlerFactory.createDurableQueueHandler(name, false);
                queueHandlerMap.putIfAbsent(name, handler);
            });
    }

    public Collection<QueueHandler> getAllQueues() {
        return queueHandlerMap.values();
    }

    /**
     * Method to reload queues on becoming the active node.
     *
     * @throws BrokerException if an error occurs loading messages from the database
     */
    void reloadQueuesOnBecomingActive() throws BrokerException {
        queueHandlerMap.clear();
        retrieveQueuesFromDao();
    }
}
