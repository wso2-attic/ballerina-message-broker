package org.wso2.broker.core.store.dao.impl;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.ContentChunk;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.Metadata;
import org.wso2.broker.core.store.dao.MessageDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Implements functionality required to manipulate messages in the storage.
 */
public class MessageDaoImpl extends MessageDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDaoImpl.class);

    public MessageDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void persist(Message message) throws BrokerException {

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            Metadata metadata = message.getMetadata();
            statement = connection.prepareStatement(RDBMSConstants.PS_INSERT_METADATA);
            statement.setLong(1, metadata.getInternalId());
            statement.setString(2, metadata.getExchangeName());
            statement.setString(3, metadata.getRoutingKey());
            ByteBuf byteBuf = metadata.getRawMetadata();
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(0, bytes);
            statement.setBytes(4, bytes);
            statement.execute();

            close(statement);

            statement = connection.prepareStatement(RDBMSConstants.PS_INSERT_CONTENT);
            for (ContentChunk chunk: message.getContentChunks()) {
                statement.setLong(1, metadata.getInternalId());
                statement.setLong(2, chunk.getOffset());
                bytes = new byte[chunk.getBytes().readableBytes()];
                chunk.getBytes().getBytes(0, bytes);
                statement.setBytes(3, bytes);
                statement.addBatch();
            }
            statement.executeBatch();

            connection.commit();
        } catch (SQLException e) {
            throw new BrokerException("Error persisting message. " + message, e);
        } finally {
            close(connection, statement);
        }
    }

    @Override
    public void detachFromQueue(String queueName, Long messageId) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_DELETE_FROM_QUEUE);
            statement.setLong(1, messageId);
            statement.setString(2, queueName);

            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            LOGGER.error("Error removing entry from queue [ {} ] for message id [ {} ]", queueName, messageId, e);
        } finally {
            close(connection, statement);
        }
    }



    @Override
    public void delete(Long messageId) {

    }

    @Override
    public void readAll(String queueName) {

    }

}
