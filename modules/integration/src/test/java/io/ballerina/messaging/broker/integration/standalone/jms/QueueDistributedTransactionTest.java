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

package io.ballerina.messaging.broker.integration.standalone.jms;

import io.ballerina.messaging.broker.core.rest.model.QueueMetadata;
import io.ballerina.messaging.broker.core.transaction.XidImpl;
import io.ballerina.messaging.broker.integration.util.BrokerRestApiClient;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

/**
 * Queue based tests for distributed transactions.
 */
public class QueueDistributedTransactionTest {

    private ClientHelper.InitialContextBuilder initialContextBuilder;
    private BrokerRestApiClient restApiClient;

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname", "broker-rest-port"})
    @BeforeMethod
    public void setUp(String port,
                      String adminUsername,
                      String adminPassword,
                      String brokerHostname,
                      String restPort) throws Exception {
        initialContextBuilder = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port);
        restApiClient = new BrokerRestApiClient(adminUsername, adminPassword, restPort, brokerHostname);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        restApiClient.close();
    }

    @Test
    public void testPublisherWithCommit() throws NamingException, JMSException, XAException {

        String queueName = "testPublisherWithCommit";
        String testMessage = "testPublisherWithCommit-Message";
        InitialContext initialContext = initialContextBuilder.withXaConnectionFactory()
                                                             .withQueue(queueName)
                                                             .build();

        XAConnectionFactory xaConnectionFactory =
                (XAConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);

        XAConnection xaConnection = xaConnectionFactory.createXAConnection();
        XASession xaSession = xaConnection.createXASession();
        XAResource xaResource = xaSession.getXAResource();

        Session session = xaSession.getSession();
        Queue queue = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(queue);
        xaConnection.start();

        XidImpl xid = new XidImpl(0, "branchId_1".getBytes(), "globalId_1".getBytes());
        xaResource.start(xid, XAResource.TMNOFLAGS);
        producer.send(session.createTextMessage(testMessage));
        xaResource.end(xid, XAResource.TMSUCCESS);

        int prepareOK = xaResource.prepare(xid);
        Assert.assertEquals(prepareOK, XAResource.XA_OK, "Prepare phase should return XA_OK");

        xaResource.commit(xid, false);

        // Test by consuming the committed message.
        ConnectionFactory connectionFactory =
                (ConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        Session receivingSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = receivingSession.createConsumer(receivingSession.createQueue(queueName));
        connection.start();
        TextMessage message = (TextMessage) consumer.receive(3000);

        Assert.assertNotNull(message, "Didn't receive a message");
        Assert.assertEquals(message.getText(), testMessage, "Received message should match sent message");

        session.close();
        xaConnection.close();
    }

    @Test
    public void testPublisherWithRollback() throws NamingException, JMSException, XAException, IOException {
        String queueName = "testPublisherWithRollback";
        String testMessage = "testPublisherWithRollback-Message";
        InitialContext initialContext = initialContextBuilder.withXaConnectionFactory()
                                                             .withQueue(queueName)
                                                             .build();

        XAConnectionFactory xaConnectionFactory =
                (XAConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);

        XAConnection xaConnection = xaConnectionFactory.createXAConnection();
        XASession xaSession = xaConnection.createXASession();
        XAResource xaResource = xaSession.getXAResource();

        Session session = xaSession.getSession();
        Queue queue = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(queue);
        xaConnection.start();

        XidImpl xid = new XidImpl(0, "branchId_1".getBytes(), "globalId_1".getBytes());
        xaResource.start(xid, XAResource.TMNOFLAGS);
        producer.send(session.createTextMessage(testMessage));
        xaResource.end(xid, XAResource.TMSUCCESS);

        int prepareOK = xaResource.prepare(xid);
        Assert.assertEquals(prepareOK, XAResource.XA_OK, "Prepare phase should return XA_OK");

        xaResource.rollback(xid);

        // Check whether the message is published to queue.
        QueueMetadata queueMetadata = restApiClient.getQueueMetadata(queueName);

        Assert.assertEquals((int) queueMetadata.getSize(), 0, "Queue should be empty");

        session.close();
        xaConnection.close();
    }

    @Test
    public void testConsumerWithCommit() throws Exception {
        String queueName = "testConsumerWithCommit";
        String testMessage = "testConsumerWithCommit-Message";
        InitialContext initialContext = initialContextBuilder.withXaConnectionFactory()
                                                             .withQueue(queueName)
                                                             .build();
        // Setup XA connection
        XAConnectionFactory xaConnectionFactory =
                (XAConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);

        XAConnection xaConnection = xaConnectionFactory.createXAConnection();
        XASession xaSession = xaConnection.createXASession();
        XAResource xaResource = xaSession.getXAResource();

        Session session = xaSession.getSession();
        Queue queue = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(queue);
        MessageConsumer consumer = session.createConsumer(queue);
        xaConnection.start();
        producer.send(session.createTextMessage(testMessage));

        XidImpl xid = new XidImpl(0, "branchId_1".getBytes(), "globalId_1".getBytes());
        xaResource.start(xid, XAResource.TMNOFLAGS);
        TextMessage message = (TextMessage) consumer.receive(2000);
        xaResource.end(xid, XAResource.TMSUCCESS);

        int prepareOK = xaResource.prepare(xid);
        Assert.assertEquals(prepareOK, XAResource.XA_OK, "Prepare phase should return XA_OK.");

        xaResource.commit(xid, false);

        session.close();
        xaConnection.close();

        Assert.assertNotNull(message, "Sent message should be consumed by the consumer.");
        Assert.assertEquals(message.getText(), testMessage, "Received message should match the sent message.");

        // Check whether the message is published to queue.
        QueueMetadata queueMetadata = restApiClient.getQueueMetadata(queueName);
        Assert.assertEquals((int) queueMetadata.getSize(), 0, "Queue should be empty");

    }

    @Test
    public void testConsumerWithRollback() throws Exception {
        String queueName = "testConsumerWithRollback";
        String testMessage = "testConsumerWithRollback-Message";
        InitialContext initialContext = initialContextBuilder.withXaConnectionFactory()
                                                             .withQueue(queueName)
                                                             .build();
        // Setup XA connection
        XAConnectionFactory xaConnectionFactory =
                (XAConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);

        XAConnection xaConnection = xaConnectionFactory.createXAConnection();
        XASession xaSession = xaConnection.createXASession();
        XAResource xaResource = xaSession.getXAResource();

        Session session = xaSession.getSession();
        Queue queue = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(queue);
        MessageConsumer consumer = session.createConsumer(queue);
        xaConnection.start();
        producer.send(session.createTextMessage(testMessage));

        XidImpl xid = new XidImpl(0, "branchId_1".getBytes(), "globalId_1".getBytes());
        xaResource.start(xid, XAResource.TMNOFLAGS);
        TextMessage message = (TextMessage) consumer.receive(2000);
        xaResource.end(xid, XAResource.TMSUCCESS);

        int prepareOK = xaResource.prepare(xid);
        Assert.assertEquals(prepareOK, XAResource.XA_OK, "Prepare phase should return XA_OK.");

        xaResource.rollback(xid);

        session.close();
        xaConnection.close();

        Assert.assertNotNull(message, "Sent message should be consumed by the consumer.");
        Assert.assertEquals(message.getText(), testMessage, "Received message should match the sent message.");

        // Check whether the message is published to queue.
        QueueMetadata queueMetadata = restApiClient.getQueueMetadata(queueName);
        Assert.assertEquals((int) queueMetadata.getSize(), 1, "Queue should be non empty");


    }
}
