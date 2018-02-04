package org.wso2.broker.core.store.dao.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wso2.broker.common.data.types.FieldTable;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.ContentChunk;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.Metadata;
import org.wso2.broker.core.metrics.BrokerMetricManager;
import org.wso2.broker.core.store.DbOperation;
import org.wso2.broker.core.store.dao.MessageDao;
import org.wso2.carbon.metrics.core.Timer.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * Implements functionality required to manipulate messages in the storage.
 */
public class MessageDaoImpl extends MessageDao {

    private final BrokerMetricManager metricManager;

    public MessageDaoImpl(DataSource dataSource, BrokerMetricManager metricManager) {
        super(dataSource);
        this.metricManager = metricManager;
    }

    @SuppressFBWarnings(
            value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "Return value of context.stop() is not required.")
    @Override
    public void persist(Collection<Message> messageList) throws BrokerException {

        Connection connection = null;
        PreparedStatement metadataStmt = null;
        PreparedStatement contentStmt = null;
        PreparedStatement insertToQueueStmt = null;
        Context context = metricManager.startMessageWriteTimer();
        try {
            connection = getConnection();
            metadataStmt = connection.prepareStatement(RDBMSConstants.PS_INSERT_METADATA);
            contentStmt = connection.prepareStatement(RDBMSConstants.PS_INSERT_CONTENT);
            insertToQueueStmt = connection.prepareStatement(RDBMSConstants.PS_INSERT_INTO_QUEUE);

            for (Message message : messageList) {
                Metadata metadata = prepareMetadata(metadataStmt, message);
                prepareContent(contentStmt, message, metadata);
                prepareQueueAttachments(insertToQueueStmt, metadata);
            }
            metadataStmt.executeBatch();
            contentStmt.executeBatch();
            insertToQueueStmt.executeBatch();
            connection.commit();

        } catch (SQLException e) {
            throw new BrokerException("Error persisting messages.", e);
        } finally {
            context.stop();
            close(metadataStmt);
            close(contentStmt);
            close(insertToQueueStmt);
            close(connection);
        }
    }

    private void prepareQueueAttachments(PreparedStatement insertToQueueStmt, Metadata metadata) throws SQLException {
        long id = metadata.getInternalId();
        for (String queueName : metadata.getAttachedQueues()) {
            insertToQueueStmt.setLong(1, id);
            insertToQueueStmt.setString(2, queueName);
            insertToQueueStmt.addBatch();
        }
    }

    private void prepareContent(PreparedStatement contentStmt, Message message, Metadata metadata) throws SQLException {
        byte[] bytes;

        for (ContentChunk chunk : message.getContentChunks()) {
            contentStmt.setLong(1, metadata.getInternalId());
            contentStmt.setLong(2, chunk.getOffset());
            bytes = new byte[chunk.getBytes().readableBytes()];
            chunk.getBytes().getBytes(0, bytes);
            contentStmt.setBytes(3, bytes);
            contentStmt.addBatch();
        }
    }

    private Metadata prepareMetadata(PreparedStatement metadataStmt, Message message) throws SQLException {
        Metadata metadata = message.getMetadata();
        metadataStmt.setLong(1, metadata.getInternalId());
        metadataStmt.setString(2, metadata.getExchangeName());
        metadataStmt.setString(3, metadata.getRoutingKey());
        metadataStmt.setLong(4, metadata.getContentLength());
        long size = metadata.getProperties().getSize() + metadata.getHeaders().getSize();
        byte[] bytes = new byte[(int) size];
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);

        try {
            buffer.resetWriterIndex();

            metadata.getProperties().write(buffer);
            metadata.getHeaders().write(buffer);

            metadataStmt.setBytes(5, bytes);
            metadataStmt.addBatch();
        } finally {
            buffer.release();
        }
        return metadata;
    }

    @Override
    public void detachFromQueue(Collection<DbOperation> dbOperations) throws BrokerException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_DELETE_FROM_QUEUE);
            for (DbOperation dbOperation : dbOperations) {
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

    @SuppressFBWarnings(
            value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "Return value of context.stop() is not required.")
    @Override
    public void delete(Collection<Long> internalIdList) throws BrokerException {
        Connection connection = null;
        PreparedStatement statement = null;
        Context context = metricManager.startMessageDeleteTimer();

        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_DELETE_MESSAGE);
            for (Long internalId : internalIdList) {
                statement.setLong(1, internalId);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            throw new BrokerException("Error occurred while deleting messages", e);
        } finally {
            context.stop();
            close(connection, statement);
        }
    }

    @SuppressFBWarnings(
            value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "Return value of context.stop() is not required.")
    @Override
    public Collection<Message> readAll(String queueName) throws BrokerException {
        Connection connection = null;
        Map<Long, Message> messageMap = new HashMap<>();
        Context context = metricManager.startMessageReadTimer();

        try {
            connection = getConnection();
            List<Long> messageList = getMessagesIdsForQueue(connection, queueName);

            if (!messageList.isEmpty()) {
                String idList = getSQLFormattedIdList(messageList);
                populateMessageWithMetadata(connection, idList, messageList, messageMap);
                populateContent(connection, idList, messageList, messageMap);
            }
            return messageMap.values();
        } catch (SQLException e) {
            throw new BrokerException("Error occurred while reading messages", e);
        } finally {
            close(connection);
            context.stop();
        }
    }

    private List<Long> getMessagesIdsForQueue(Connection connection, String queueName) throws SQLException {
        ArrayList<Long> messageList = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(RDBMSConstants.PS_SELECT_MESSAGES_FOR_QUEUE);
            statement.setString(1, queueName);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                messageList.add(resultSet.getLong(1));
            }
            return messageList;
        } finally {
            close(resultSet);
            close(statement);
        }
    }

    private String getSQLFormattedIdList(List<Long> messageList) {
        StringBuilder paramList = new StringBuilder();
        paramList.append("?");

        for (int i = 1; i < messageList.size(); i++) {
            paramList.append(",?");
        }
        return paramList.toString();
    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private void populateMessageWithMetadata(Connection connection,
                                             String idListAsString, List<Long> idList,
                                             Map<Long, Message> messageMap) throws SQLException, BrokerException {
        String metadataSql = "SELECT MESSAGE_ID, EXCHANGE_NAME, ROUTING_KEY, CONTENT_LENGTH, MESSAGE_METADATA "
                + " FROM MB_METADATA WHERE MESSAGE_ID IN (" + idListAsString + ") ORDER BY MESSAGE_ID";


        PreparedStatement selectMetadata = null;
        ResultSet metadataResultSet = null;

        try {
            selectMetadata = connection.prepareStatement(metadataSql);
            for (int i = 0; i < idList.size(); i++) {
                selectMetadata.setLong(i + 1, idList.get(i));
            }

            metadataResultSet = selectMetadata.executeQuery();
            while (metadataResultSet.next()) {
                long messageId = metadataResultSet.getLong(1);
                String exchangeName = metadataResultSet.getString(2);
                String routingKey = metadataResultSet.getString(3);
                long contentLength = metadataResultSet.getLong(4);
                byte[] bytes = metadataResultSet.getBytes(5);
                ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
                try {
                    Metadata metadata = new Metadata(messageId, routingKey, exchangeName, contentLength);
                    metadata.setProperties(FieldTable.parse(buffer));
                    metadata.setHeaders(FieldTable.parse(buffer));
                    Message message = new Message(metadata);
                    messageMap.put(messageId, message);
                } catch (Exception e) {
                    throw new BrokerException("Error occurred while parsing metadata properties", e);
                } finally {
                    buffer.release();
                }
            }
        } finally {
            close(metadataResultSet);
            close(selectMetadata);
        }
    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private void populateContent(Connection connection, String idList,
                                 List<Long> messageList, Map<Long, Message> messageMap) throws SQLException {

        PreparedStatement selectContent = null;
        ResultSet contentResultSet = null;

        try {
            selectContent = connection.prepareStatement(
                    "SELECT MESSAGE_ID, CONTENT_OFFSET, MESSAGE_CONTENT FROM MB_CONTENT "
                            + "WHERE MESSAGE_ID IN(" + idList + ")");

            for (int i = 0; i < messageList.size(); i++) {
                selectContent.setLong(i + 1, messageList.get(i));
            }

            contentResultSet = selectContent.executeQuery();

            while (contentResultSet.next()) {
                long messageId = contentResultSet.getLong(1);
                int offset = contentResultSet.getInt(2);
                byte[] bytes = contentResultSet.getBytes(3);

                Message message = messageMap.get(messageId);
                if (message != null) {
                    message.addChunk(new ContentChunk(offset, Unpooled.copiedBuffer(bytes)));
                }
            }
        } finally {
            close(contentResultSet);
            close(selectContent);
        }
    }
}
