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

import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.jms.XATopicConnection;
import javax.jms.XATopicConnectionFactory;
import javax.jms.XATopicSession;
import javax.naming.InitialContext;
import javax.transaction.xa.XAResource;

/**
 * Topic based tests for distributed transactions.
 */
public class TopicDistributedTransactionTest {

    private ClientHelper.InitialContextBuilder initialContextBuilder;
    private BrokerRestApiClient restApiClient;
    private String subscriptionId = "sub-1";

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
    public void testPublisherWithCommit() throws Exception {

        String subscriptionId = "sub-testPublisherWithCommit";
        String topicName = "testPublisherWithCommit";
        String testMessage = "testPublisherWithCommit-Message";
        InitialContext initialContext = initialContextBuilder.withXaConnectionFactory().withTopic(topicName).build();
        Topic topic = (Topic) initialContext.lookup(topicName);

        // Setup XA producer.
        XATopicConnectionFactory xaTopicConnectionFactory =
                (XATopicConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);
        XATopicConnection xaTopicConnection = xaTopicConnectionFactory.createXATopicConnection();
        XATopicSession xaTopicSession = xaTopicConnection.createXATopicSession();
        XAResource xaResource = xaTopicSession.getXAResource();
        MessageProducer producer = xaTopicSession.createProducer(topic);

        // Setup non-transactional consumer.
        TopicSession topicSession = xaTopicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        TopicSubscriber durableSubscriber = topicSession.createDurableSubscriber(topic, subscriptionId);

        xaTopicConnection.start();

        // Send message within XA transaction.
        XidImpl xid = new XidImpl(0, "branchId".getBytes(), "globalId".getBytes());
        xaResource.start(xid, XAResource.TMNOFLAGS);
        producer.send(xaTopicSession.createTextMessage(testMessage));
        xaResource.end(xid, XAResource.TMSUCCESS);

        int response = xaResource.prepare(xid);
        Assert.assertEquals(response, XAResource.XA_OK, "Prepare stage failed.");

        xaResource.commit(xid, false);

        TextMessage message = (TextMessage) durableSubscriber.receive(2000);
        Assert.assertNotNull(message, "Didn't receive a message");
        Assert.assertEquals(message.getText(), testMessage, "Received message content didn't match sent message.");

        topicSession.close();
        xaTopicSession.close();
        xaTopicConnection.close();
    }

    @Test
    public void testPublisherWithRollback() throws Exception {

        String subscriptionId = "sub-testPublisherWithRollback";
        String topicName = "testPublisherWithRollback";
        String testMessage = "testPublisherWithRollback-Message";
        InitialContext initialContext = initialContextBuilder.withXaConnectionFactory().withTopic(topicName).build();
        Topic topic = (Topic) initialContext.lookup(topicName);

        // Setup XA producer.
        XATopicConnectionFactory xaTopicConnectionFactory =
                (XATopicConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);
        XATopicConnection xaTopicConnection = xaTopicConnectionFactory.createXATopicConnection();
        XATopicSession xaTopicSession = xaTopicConnection.createXATopicSession();
        XAResource xaResource = xaTopicSession.getXAResource();
        MessageProducer producer = xaTopicSession.createProducer(topic);

        // Setup non-transactional consumer.
        TopicSession topicSession = xaTopicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        TopicSubscriber durableSubscriber = topicSession.createDurableSubscriber(topic, subscriptionId);
        xaTopicConnection.start();

        // Send message withing a XA transaction.
        XidImpl xid = new XidImpl(0, "branchId".getBytes(), "globalId".getBytes());
        xaResource.start(xid, XAResource.TMNOFLAGS);
        producer.send(xaTopicSession.createTextMessage(testMessage));
        xaResource.end(xid, XAResource.TMSUCCESS);

        int response = xaResource.prepare(xid);
        Assert.assertEquals(response, XAResource.XA_OK, "Prepare stage failed.");

        xaResource.rollback(xid);

        durableSubscriber.close();
        xaTopicSession.close();
        xaTopicConnection.close();
        QueueMetadata queueMetadata = restApiClient.getQueueMetadata("carbon:" + subscriptionId);
        Assert.assertEquals((int) queueMetadata.getSize(), 0, "Queue is not empty");
    }

    @Test
    public void testSubscriberWithCommit() throws Exception {

        String subscriptionId = "sub-testSubscriberWithCommit";
        String topicName = "testSubscriberWithCommit";
        String testMessage = "testSubscriberWithCommit-Message";
        InitialContext initialContext = initialContextBuilder.withXaConnectionFactory().withTopic(topicName).build();
        Topic topic = (Topic) initialContext.lookup(topicName);

        // Create XA consumer.
        XATopicConnectionFactory xaTopicConnectionFactory =
                (XATopicConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);
        XATopicConnection xaTopicConnection = xaTopicConnectionFactory.createXATopicConnection();
        XATopicSession xaTopicSession = xaTopicConnection.createXATopicSession();
        XAResource xaResource = xaTopicSession.getXAResource();
        TopicSubscriber durableSubscriber = xaTopicSession.createDurableSubscriber(topic, subscriptionId);

        // Create non transactional producer.
        TopicSession topicSession = xaTopicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        MessageProducer producer = topicSession.createProducer(topic);

        xaTopicConnection.start();

        producer.send(xaTopicSession.createTextMessage(testMessage));

        // Consume message within a XA transaction.
        XidImpl xid = new XidImpl(0, "branchId".getBytes(), "globalId".getBytes());
        xaResource.start(xid, XAResource.TMNOFLAGS);
        TextMessage message = (TextMessage) durableSubscriber.receive(2000);
        xaResource.end(xid, XAResource.TMSUCCESS);

        int response = xaResource.prepare(xid);
        Assert.assertEquals(response, XAResource.XA_OK, "Prepare stage failed.");

        xaResource.commit(xid, false);

        Assert.assertNotNull(message, "Didn't receive a message");
        Assert.assertEquals(message.getText(), testMessage, "Received message content didn't match sent message.");

        topicSession.close();
        xaTopicSession.close();
        xaTopicConnection.close();

        QueueMetadata queueMetadata = restApiClient.getQueueMetadata("carbon:" + subscriptionId);
        Assert.assertEquals((int) queueMetadata.getSize(), 0, "Queue should be empty.");
    }


    @Test
    public void testSubscriberWithRollback() throws Exception {

        String subscriptionId = "sub-testSubscriberWithRollback";
        String topicName = "testSubscriberWithCommit";
        String testMessage = "testSubscriberWithCommit-Message";
        InitialContext initialContext = initialContextBuilder.withXaConnectionFactory().withTopic(topicName).build();
        Topic topic = (Topic) initialContext.lookup(topicName);

        // Setup XA consumer.
        XATopicConnectionFactory xaTopicConnectionFactory =
                (XATopicConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);
        XATopicConnection xaTopicConnection = xaTopicConnectionFactory.createXATopicConnection();
        XATopicSession xaTopicSession = xaTopicConnection.createXATopicSession();
        XAResource xaResource = xaTopicSession.getXAResource();
        TopicSubscriber durableSubscriber = xaTopicSession.createDurableSubscriber(topic, subscriptionId);

        // Setup non-transactional message publisher.
        TopicSession topicSession = xaTopicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        MessageProducer producer = topicSession.createProducer(topic);

        xaTopicConnection.start();

        producer.send(xaTopicSession.createTextMessage(testMessage));

        // Consume messages within a XA transaction.
        XidImpl xid = new XidImpl(0, "branchId".getBytes(), "globalId".getBytes());
        xaResource.start(xid, XAResource.TMNOFLAGS);
        TextMessage message = (TextMessage) durableSubscriber.receive(2000);
        xaResource.end(xid, XAResource.TMSUCCESS);

        int response = xaResource.prepare(xid);
        Assert.assertEquals(response, XAResource.XA_OK, "Prepare stage failed.");

        xaResource.rollback(xid);

        Assert.assertNotNull(message, "Didn't receive a message");
        Assert.assertEquals(message.getText(), testMessage, "Received message content didn't match sent message.");

        topicSession.close();
        xaTopicSession.close();
        xaTopicConnection.close();

        QueueMetadata queueMetadata = restApiClient.getQueueMetadata("carbon:" + subscriptionId);
        Assert.assertEquals((int) queueMetadata.getSize(), 1, "Queue shouldn't be empty.");
    }
}
