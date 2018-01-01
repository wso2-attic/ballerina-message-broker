package org.wso2.broker.core.store.dao.impl;

import io.netty.buffer.ByteBuf;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.ContentChunk;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.Metadata;
import org.wso2.broker.core.store.DbOperation;
import org.wso2.broker.core.store.dao.MessageDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import javax.sql.DataSource;

/**
 * Implements functionality required to manipulate messages in the storage.
 */
public class MessageDaoImpl extends MessageDao {

    public MessageDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void persist(Collection<Message> messageList) throws BrokerException {

        Connection connection = null;
        PreparedStatement metadataStmt = null;
        PreparedStatement contentStmt = null;
        PreparedStatement insertToQueueStmt = null;

        try {
            connection = getConnection();
            metadataStmt = connection.prepareStatement(RDBMSConstants.PS_INSERT_METADATA);
            contentStmt = connection.prepareStatement(RDBMSConstants.PS_INSERT_CONTENT);
            insertToQueueStmt = connection.prepareStatement(RDBMSConstants.PS_INSERT_INTO_QUEUE);
            for (Message message : messageList) {
                Metadata metadata = message.getMetadata();
                metadataStmt.setLong(1, metadata.getInternalId());
                metadataStmt.setString(2, metadata.getExchangeName());
                metadataStmt.setString(3, metadata.getRoutingKey());
                ByteBuf byteBuf = metadata.getRawMetadata();
                byte[] bytes = new byte[byteBuf.readableBytes()];
                byteBuf.getBytes(0, bytes);
                metadataStmt.setBytes(4, bytes);
                metadataStmt.addBatch();

                for (ContentChunk chunk : message.getContentChunks()) {
                    contentStmt.setLong(1, metadata.getInternalId());
                    contentStmt.setLong(2, chunk.getOffset());
                    bytes = new byte[chunk.getBytes().readableBytes()];
                    chunk.getBytes().getBytes(0, bytes);
                    contentStmt.setBytes(3, bytes);
                    contentStmt.addBatch();
                }

                long id = metadata.getInternalId();
                for (String queueName : metadata.getAttachedQueues()) {
                    insertToQueueStmt.setLong(1, id);
                    insertToQueueStmt.setString(2, queueName);
                    insertToQueueStmt.addBatch();
                }
            }
            metadataStmt.executeBatch();
            contentStmt.executeBatch();
            insertToQueueStmt.executeBatch();

            connection.commit();
        } catch (SQLException e) {
            throw new BrokerException("Error persisting messages.", e);
        } finally {
            close(metadataStmt);
            close(contentStmt);
            close(insertToQueueStmt);
            close(connection);
        }
    }

    @Override
    public void detachFromQueue(Collection<DbOperation> dbOperations) throws BrokerException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_DELETE_FROM_QUEUE);
            for (DbOperation dbOperation: dbOperations) {
                statement.setLong(1, dbOperation.getMessageId());
                statement.setString(2, dbOperation.getQueueName());
                statement.addBatch();
            }

            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            throw new BrokerException("Error detaching messages from queues.", e);
        } finally {
            close(connection, statement);
        }
    }

    @Override
    public void delete(Collection<Long> internalIdList) throws BrokerException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_DELETE_MESSAGE);
            for (Long internalId: internalIdList) {
                statement.setLong(1, internalId);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            throw new BrokerException("Error occurred while deleting messages", e);
        } finally {
            close(connection, statement);
        }
    }

    @Override
    public void readAll(String queueName) throws BrokerException {
//        Connection connection = null;
//        PreparedStatement statement = null;
//        ResultSet resultSet = null;
//        Statement selectMetadata = null;
//        try {
//            connection = getConnection();
//            statement = connection.prepareStatement(RDBMSConstants.PS_SELECT_MESSAGES_FOR_QUEUE);
//            statement.setString(1, queueName);
//            resultSet = statement.executeQuery();
//            List<Long> messageList = new ArrayList<>();
//            while (resultSet.next()) {
//                messageList.add(resultSet.getLong(1));
//            }
//
//            if (!messageList.isEmpty()) {
//                StringBuilder sql =
//                        new StringBuilder("SELECT MESSAGE_ID, EXCHANGE_NAME, ROUTING_KEY, MESSAGE_METADATA "
//                                + "WHERE MESSAGE_ID IN (");
//                sql.append(messageList.get(0));
//                for (int i = 1; i < messageList.size(); i++) {
//                    sql.append(',' + messageList.get(i));
//                }
//                sql.append(')');
//                selectMetadata = connection.createStatement();
//                ResultSet metadataResultSet = selectMetadata.executeQuery(sql.toString());
//
//                while (metadataResultSet.next()) {
//                    long messageId = metadataResultSet.getLong(1);
//                    String exchangeName = metadataResultSet.getString(2);
//                    String routingKey = metadataResultSet.getString(3);
//                    byte[] bytes = metadataResultSet.getBytes(4);
//
//                    new Metadata(messageId, routingKey, exchangeName, )
//                }
//
//            }
//
//
//        } catch (SQLException e) {
//            throw new BrokerException("Error occurred while reading messages", e);
//        } finally {
//            close(connection, statement, resultSet);
//        }
    }

}
