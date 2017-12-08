package org.wso2.broker.core.store.dao.impl;

import org.wso2.broker.core.Message;
import org.wso2.broker.core.store.dao.MessageDao;

/**
 * Implements functionality required to manipulate messages in the storage.
 */
public class MessageDaoImpl implements MessageDao {

    @Override
    public void persist(Message message) {

    }

    @Override
    public void detachFromQueue(String queueName, Long messageId) {

    }

    @Override
    public void delete(Long messageId) {

    }

    @Override
    public void readAll(String queueName) {

    }

}
