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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.ballerina.messaging.broker.common.BaseDao;
import io.ballerina.messaging.broker.common.DaoException;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.ChunkConverter;
import io.ballerina.messaging.broker.core.ContentChunk;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Metadata;
import io.ballerina.messaging.broker.core.store.QueueDetachEventList;
import io.ballerina.messaging.broker.core.transaction.XidImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.sql.DataSource;
import javax.transaction.xa.Xid;

/**
 * DAO class with base message operations needed for distributed transactions.
 */
public class DtxCrudOperationsDao extends BaseDao {

    private ChunkConverter chunkConverter;

    DtxCrudOperationsDao(DataSource dataSource, ChunkConverter chunkConverter) {
        super(dataSource);
        this.chunkConverter = chunkConverter;
    }

    long storeXid(Connection connection, Xid xid) throws SQLException {
        long internalXid = Broker.getNextMessageId();
        PreparedStatement insertStatement = null;
        try {
            insertStatement = connection.prepareStatement(RDBMSConstants.PS_DTX_INSERT_XID);
            insertStatement.setLong(1, internalXid);
            insertStatement.setInt(2, xid.getFormatId());
            insertStatement.setBytes(3, xid.getGlobalTransactionId());
            insertStatement.setBytes(4, xid.getBranchQualifier());

            insertStatement.executeUpdate();
        } finally {
            close(insertStatement);
        }
        return internalXid;
    }

    void prepareEnqueueMessages(Connection connection, long internalXid,
                                Collection<Message> enqueueMessages) throws SQLException {
        PreparedStatement insertMetadataStatement = null;
        PreparedStatement insertContentStatement = null;
        PreparedStatement insertToQueueStatement = null;
        try {
            insertMetadataStatement = connection.prepareStatement(RDBMSConstants.PS_DTX_INSERT_ENQUEUE_METADATA);
            insertContentStatement = connection.prepareStatement(RDBMSConstants.PS_DTX_INSERT_ENQUEUE_CONTENT);
            insertToQueueStatement = connection.prepareStatement(RDBMSConstants.PS_DTX_INSERT_QUEUE_ATTACHMENTS);

            for (Message message : enqueueMessages) {
                prepareMetadataBatch(internalXid, insertMetadataStatement, message);
                prepareContentBatches(internalXid, insertContentStatement, message);
                prepareQueueAttachments(internalXid, insertToQueueStatement, message);
            }

            insertMetadataStatement.executeBatch();
            insertContentStatement.executeBatch();
            insertToQueueStatement.executeBatch();
        } finally {
            close(insertMetadataStatement);
            close(insertContentStatement);
            close(insertToQueueStatement);
        }
    }

    private void prepareContentBatches(long internalXid, PreparedStatement insertContentStatement, Message message)
            throws SQLException {
        List<ContentChunk> contentChunks = message.getContentChunks();
        long contentLength = message.getMetadata().getContentLength();

        List<ContentChunk> convertedChunks = chunkConverter.convert(contentChunks, (int) contentLength);
        for (ContentChunk contentChunk : convertedChunks) {
            insertContentStatement.setLong(1, internalXid);
            insertContentStatement.setLong(2, message.getInternalId());
            insertContentStatement.setLong(3, contentChunk.getOffset());
            insertContentStatement.setBytes(4, contentChunk.getBytes());
            insertContentStatement.addBatch();
        }
    }

    private void prepareMetadataBatch(long internalXid, PreparedStatement insertMetadataStatement, Message message)
            throws SQLException {
        insertMetadataStatement.setLong(1, internalXid);
        insertMetadataStatement.setLong(2, message.getInternalId());
        insertMetadataStatement.setLong(3, message.getExpiryTimestamp());
        insertMetadataStatement.setString(4, message.getMetadata().getExchangeName());
        insertMetadataStatement.setString(5, message.getMetadata().getRoutingKey());
        insertMetadataStatement.setLong(6, message.getMetadata().getContentLength());
        insertMetadataStatement.setBytes(7, message.getMetadata().getPropertiesAsBytes());
        insertMetadataStatement.addBatch();
    }

    private void prepareQueueAttachments(long internalXid, PreparedStatement insertToQueueStmt,
                                         Message message) throws SQLException {
        long id = message.getInternalId();
        for (String queueName : message.getAttachedDurableQueues()) {
            insertToQueueStmt.setLong(1, internalXid);
            insertToQueueStmt.setLong(2, id);
            insertToQueueStmt.setString(3, queueName);
            insertToQueueStmt.addBatch();
        }
    }

    void prepareDetachMessages(Connection connection, long internalXid,
                               Map<String, QueueDetachEventList> detachMessageMap) throws SQLException {
        PreparedStatement insertDetachEvent = null;

        try {
            insertDetachEvent = connection.prepareStatement(RDBMSConstants.PS_DTX_INSERT_DEQUEUE_MAPPING);
            for (Map.Entry<String, QueueDetachEventList> detachEventEntry : detachMessageMap.entrySet()) {
                String queueName = detachEventEntry.getKey();
                QueueDetachEventList detachEvents = detachEventEntry.getValue();
                for (long messageId : detachEvents.getMessageIds()) {
                    insertDetachEvent.setLong(1, internalXid);
                    insertDetachEvent.setLong(2, messageId);
                    insertDetachEvent.setString(3, queueName);
                    insertDetachEvent.addBatch();
                }
            }
            insertDetachEvent.executeBatch();
        } finally {
            close(insertDetachEvent);
        }
    }

    public void copyEnqueueMessages(Connection connection, long internalXid) throws SQLException {
        copyFromPreparedTables(connection, internalXid, RDBMSConstants.PS_DTX_COPY_ENQUEUE_METADATA);
        copyFromPreparedTables(connection, internalXid, RDBMSConstants.PS_DTX_COPY_ENQUEUE_CONTENT);
        copyFromPreparedTables(connection, internalXid, RDBMSConstants.PS_DTX_COPY_ENQUEUE_QUEUE_ATTACHEMENTS);
    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    private void copyFromPreparedTables(Connection connection, long internalXid,
                                        String sqlString) throws SQLException {
        PreparedStatement copyStatement = null;
        try {
            copyStatement = connection.prepareStatement(sqlString);
            copyStatement.setLong(1, internalXid);
            copyStatement.execute();
        } finally {
            close(copyStatement);
        }
    }

    public void removePreparedData(Connection connection, long internalXid) throws SQLException {
        PreparedStatement deleteXidStatement = null;
        try {
            deleteXidStatement = connection.prepareStatement(RDBMSConstants.PS_DTX_DELETE_XID);
            deleteXidStatement.setLong(1, internalXid);
            deleteXidStatement.executeUpdate();
        } finally {
            close(deleteXidStatement);
        }
    }

    public void restoreDequeueMessages(Connection connection, long internalXid) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(RDBMSConstants.PS_DTX_RESTORE_DEQUEUE_MAPPING);
            preparedStatement.setLong(1, internalXid);
            preparedStatement.executeUpdate();
        } finally {
            close(preparedStatement);
        }
    }

    public void retrieveAllXids(Connection connection, Consumer<XidImpl> xidConsumer) throws SQLException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(RDBMSConstants.PS_DTX_SELECT_ALL_XIDS);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                XidImpl xid = new XidImpl(
                        resultSet.getLong(1),
                        resultSet.getInt(2),
                        resultSet.getBytes(3),
                        resultSet.getBytes(4)
                );
                xidConsumer.accept(xid);
            }
        } finally {
            close(resultSet);
            close(statement);
        }
    }

    public Collection<Message> retrieveEnqueuedMessages(Connection connection, long internalXid)
            throws DaoException {
        HashMap<Long, Message> enqueuedMessages = new HashMap<>();
        try {
            populateMetadata(connection, internalXid, enqueuedMessages);
            populateContent(connection, internalXid, enqueuedMessages);
            updateQueueMapping(connection, internalXid, enqueuedMessages);
            return enqueuedMessages.values();
        } catch (Exception e) {
            throw new DaoException("Error occurred while retrieving enqueued dtx messages", e);
        }
    }

    private void updateQueueMapping(Connection connection, long internalXid, HashMap<Long, Message> enqueuedMessages)
            throws SQLException {
        PreparedStatement queueMappingStmt = null;
        ResultSet resultSet = null;

        try {
            queueMappingStmt = connection.prepareStatement(RDBMSConstants.PS_DTX_SELECT_QUEUE_MAPPING);
            queueMappingStmt.setLong(1, internalXid);

            resultSet = queueMappingStmt.executeQuery();
            while (resultSet.next()) {
                long messageId = resultSet.getLong("MESSAGE_ID");
                String queueName = resultSet.getString("QUEUE_NAME");
                Message message = enqueuedMessages.get(messageId);
                message.addAttachedDurableQueue(queueName);
            }
        } finally {
            close(resultSet);
            close(queueMappingStmt);
        }
    }

    private void populateContent(Connection connection, long internalXid, HashMap<Long, Message> enqueuedMessages)
            throws SQLException {
        PreparedStatement retrieveContentStmt = null;
        ResultSet resultSet = null;
        try {
            retrieveContentStmt = connection.prepareStatement(RDBMSConstants.PS_DTX_SELECT_ENQUEUED_CONTENT);
            retrieveContentStmt.setLong(1, internalXid);

            resultSet = retrieveContentStmt.executeQuery();

            while (resultSet.next()) {
                long messageId = resultSet.getLong("MESSAGE_ID");
                Message message = enqueuedMessages.get(messageId);
                long offset = resultSet.getLong("CONTENT_OFFSET");
                byte[] contentBytes = resultSet.getBytes("MESSAGE_CONTENT");
                ByteBuf byteBuf = Unpooled.wrappedBuffer(contentBytes);
                message.addChunk(new ContentChunk(offset, byteBuf));
            }
        } finally {
            close(resultSet);
            close(retrieveContentStmt);
        }
    }

    private void populateMetadata(Connection connection, long internalXid, HashMap<Long, Message> enqueuedMessages)
            throws Exception {
        PreparedStatement retrieveMetadataStmt = null;
        ResultSet resultSet = null;
        try {
            retrieveMetadataStmt = connection.prepareStatement(RDBMSConstants.PS_DTX_SELECT_ENQUEUED_METADATA);
            retrieveMetadataStmt.setLong(1, internalXid);

            resultSet = retrieveMetadataStmt.executeQuery();
            while (resultSet.next()) {
                Metadata metadata = new Metadata(resultSet.getString("ROUTING_KEY"),
                                                 resultSet.getString("EXCHANGE_NAME"),
                                                 resultSet.getLong("CONTENT_LENGTH"),
                                                 resultSet.getBytes("MESSAGE_METADATA")
                );

                Message message = new Message(resultSet.getLong("MESSAGE_ID"), metadata);
                enqueuedMessages.put(message.getInternalId(), message);
            }
        } finally {
            close(resultSet);
            close(retrieveMetadataStmt);
        }
    }
}
