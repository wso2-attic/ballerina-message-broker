/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.core;

import org.wso2.broker.core.store.dao.QueueDao;
import org.wso2.broker.core.store.dao.SharedMessageStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry object which contains all the queues of the broker
 */
final class QueueRegistry {

    private final Map<String, QueueHandler> queueHandlerMap;

    private final QueueDao queueDao;

    private final SharedMessageStore sharedMessageStore;

    QueueRegistry(QueueDao queueDao, SharedMessageStore messageStore) throws BrokerException {
        this.queueHandlerMap = new HashMap<>();
        this.queueDao = queueDao;
        this.sharedMessageStore = messageStore;
        retrieveQueuesFromDao();
    }

    QueueHandler getQueueHandler(String queueName) {
        return queueHandlerMap.get(queueName);
    }

    boolean addQueue(String queueName, boolean passive, boolean durable, boolean autoDelete) throws BrokerException {
        QueueHandler queueHandler = queueHandlerMap.get(queueName);

        if (passive && queueHandler == null) {
            throw new BrokerException("QueueHandler [ " + queueName + " ] doesn't exists. Passive parameter " +
                    "is set, hence not creating the queue.");
        }

        if (queueHandler == null) {
            if (durable) {
                queueHandler = QueueHandler.createDurableQueue(queueName, autoDelete, sharedMessageStore);
                queueDao.persist(queueHandler.getQueue());
            } else {
                queueHandler = QueueHandler.createNonDurableQueue(queueName, 1000, autoDelete);
            }
            queueHandlerMap.put(queueName, queueHandler);
            return true;
        } else if (!passive && (queueHandler.getQueue().isDurable() != durable
                || queueHandler.getQueue().isAutoDelete() != autoDelete)) {
            throw new BrokerException(
                    "Existing QueueHandler [ " + queueName + " ] does not match given parameters.");
        }
        return false;
    }

    boolean removeQueue(String queueName, boolean ifUnused, boolean ifEmpty) throws BrokerException {
        QueueHandler queueHandler = queueHandlerMap.get(queueName);
        if (queueHandler == null) {
            return false;
        }

        if (ifUnused && !queueHandler.isUnused()) {
            throw new BrokerException("Cannot delete queue. Queue [ " + queueName +
                    " ] has active consumers and the ifUnused parameter is set.");
        } else if (ifEmpty && !queueHandler.isEmpty()) {
            throw new BrokerException("Cannot delete queue. Queue [ " + queueName +
                    " ] is not empty and the ifEmpty parameter is set.");
        } else {
            queueHandlerMap.remove(queueName);
            queueHandler.closeAllConsumers();
            queueDao.delete(queueHandler.getQueue());
            return true;
        }
    }

    private void retrieveQueuesFromDao() throws BrokerException {
            queueDao.retrieveAll((name) -> {
                QueueHandler handler = QueueHandler.createDurableQueue(name, false, sharedMessageStore);
                queueHandlerMap.putIfAbsent(name, handler);
            });
    }
}
