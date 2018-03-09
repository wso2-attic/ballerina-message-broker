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

import io.ballerina.messaging.broker.core.transaction.XidImpl;
import io.ballerina.messaging.broker.integration.util.BrokerRestApiClient;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import javax.jms.MessageProducer;
import javax.jms.Topic;
import javax.jms.XATopicConnection;
import javax.jms.XATopicConnectionFactory;
import javax.jms.XATopicSession;
import javax.naming.InitialContext;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Test invalid commit operations requests.
 */
public class TopicDtxCommitNegativeTest {

    private ClientHelper.InitialContextBuilder initialContextBuilder;
    private BrokerRestApiClient restApiClient;
    private XAResource xaResource;
    private XATopicSession xaSession;
    private XATopicConnection xaConnection;
    private Xid xid;

    @BeforeClass
    public void initXid() {
        this.xid = new XidImpl(0, "branchId".getBytes(), "globalId".getBytes());
    }

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
        xaResource = null;
        xaSession = null;
        xaConnection = null;
    }

    @AfterMethod
    public void tearDown() throws Exception {
        restApiClient.close();
        InitialContext initialContext = initialContextBuilder.withXaConnectionFactory().build();
        XATopicConnectionFactory xaTopicConnectionFactory =
                (XATopicConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);
        XATopicConnection xaTopicConnection = xaTopicConnectionFactory.createXATopicConnection();
        xaSession = xaTopicConnection.createXATopicSession();

        xaResource = xaSession.getXAResource();
        xaResource.rollback(xid);
        xaSession.close();
        xaConnection.close();
    }

    @Test(expectedExceptions = XAException.class,
            expectedExceptionsMessageRegExp = "Error while committing dtx session.*")
    public void testOnePhaseCommitAfterPrepare() throws Exception {

        String topicName = "testSubscriberWithCommit";
        String testMessage = "testSubscriberWithCommit-Message";
        InitialContext initialContext = initialContextBuilder.withXaConnectionFactory().withTopic(topicName).build();

        XATopicConnectionFactory xaTopicConnectionFactory =
                (XATopicConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);
        xaConnection = xaTopicConnectionFactory.createXATopicConnection();
        xaSession = xaConnection.createXATopicSession();
        xaResource = xaSession.getXAResource();

        Topic topic = (Topic) initialContext.lookup(topicName);
        MessageProducer producer = xaSession.createProducer(topic);
        xaConnection.start();
        xaResource.start(xid, XAResource.TMNOFLAGS);
        producer.send(xaSession.createTextMessage(testMessage));
        xaResource.end(xid, XAResource.TMSUCCESS);

        int response = xaResource.prepare(xid);
        Assert.assertEquals(response, XAResource.XA_OK, "Prepare stage failed.");

        xaResource.commit(xid, true);
    }

    @Test(expectedExceptions = XAException.class,
            expectedExceptionsMessageRegExp = "Error while committing dtx session.*")
    public void testTwoPhaseCommitWithoutPrepare() throws Exception {
        String topicName = "testSubscriberWithCommit";
        String testMessage = "testSubscriberWithCommit-Message";
        InitialContext initialContext = initialContextBuilder.withXaConnectionFactory().withTopic(topicName).build();

        XATopicConnectionFactory xaTopicConnectionFactory =
                (XATopicConnectionFactory) initialContext.lookup(ClientHelper.XA_CONNECTION_FACTORY);
        xaConnection = xaTopicConnectionFactory.createXATopicConnection();
        xaSession = xaConnection.createXATopicSession();
        xaResource = xaSession.getXAResource();

        Topic topic = (Topic) initialContext.lookup(topicName);
        MessageProducer producer = xaSession.createProducer(topic);
        xaConnection.start();
        xaResource.start(xid, XAResource.TMNOFLAGS);
        producer.send(xaSession.createTextMessage(testMessage));
        xaResource.end(xid, XAResource.TMSUCCESS);

        xaResource.commit(xid, false);
    }
}
