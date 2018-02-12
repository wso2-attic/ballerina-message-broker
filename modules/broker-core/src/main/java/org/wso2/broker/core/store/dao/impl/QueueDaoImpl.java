package org.wso2.broker.core.store.dao.impl;

import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.Queue;
import org.wso2.broker.core.store.dao.QueueDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

/**
 * Implements functionality required to manage Queues in persistent layer.
 */
public class QueueDaoImpl extends QueueDao {

    public QueueDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void persist(Queue queue) throws BrokerException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_INSERT_QUEUE);
            statement.setString(1, queue.getName());
            statement.setBytes(2, new byte[4]);
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            throw new BrokerException("Error occurred while storing queue " + queue, e);
        } finally {
            close(connection, statement);
        }
    }

    @Override
    public void delete(Queue queue) throws BrokerException {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            statement = connection.prepareStatement(RDBMSConstants.PS_DELETE_QUEUE);
            statement.setString(1, queue.getName());
            statement.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            throw new BrokerException("Error occurred while deleting queue " + queue, e);
        } finally {
            close(connection, statement);
        }
    }

    @Override
    public void retrieveAll(QueueCollector queueNameConsumer) throws BrokerException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(RDBMSConstants.SELECT_ALL_QUEUES);
            while (resultSet.next()) {
                String name = resultSet.getString(1);
                queueNameConsumer.addQueue(name);
            }
        } catch (SQLException e) {
            throw new BrokerException("Error occurred while retrieving all the queues", e);
        } finally {
            close(connection, statement, resultSet);
        }
    }

}
