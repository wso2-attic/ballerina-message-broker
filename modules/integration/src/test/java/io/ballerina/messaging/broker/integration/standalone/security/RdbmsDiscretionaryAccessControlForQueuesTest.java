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

package io.ballerina.messaging.broker.integration.standalone.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.messaging.broker.core.rest.ExchangesApiDelegate;
import io.ballerina.messaging.broker.core.rest.QueuesApiDelegate;
import io.ballerina.messaging.broker.core.rest.model.QueueMetadata;
import io.ballerina.messaging.broker.core.rest.model.UserGroupList;
import io.ballerina.messaging.broker.integration.util.BrokerRestApiClient;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import io.ballerina.messaging.broker.integration.util.HttpClientHelper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Test class to validate discretionary access control (DAC) for queues by different users.
 */
public class RdbmsDiscretionaryAccessControlForQueuesTest {

    private String apiBasePath;
    private CloseableHttpClient client;
    private ObjectMapper objectMapper;
    private BrokerRestApiClient brokerRestApiClient;

    @Parameters({"broker-hostname", "broker-rest-port"})
    @BeforeClass
    public void setUp(String brokerHost, String port) throws URISyntaxException {
        objectMapper = new ObjectMapper();
        apiBasePath = HttpClientHelper.getRestApiBasePath(brokerHost, port);
    }

    @Parameters({"broker-hostname", "broker-rest-port", "admin-username", "admin-password"})
    @BeforeMethod
    public void setup(String brokerHostname,
                      String port,
                      String adminUsername,
                      String adminPassword) throws URISyntaxException, NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException {
        client = HttpClientHelper.prepareClient();
        brokerRestApiClient = new BrokerRestApiClient(adminUsername, adminPassword, port, brokerHostname);
    }

    @AfterClass
    public void tearDown() throws Exception {
        apiBasePath = null;
        client.close();
    }

    @AfterMethod
    public void afterMethod() throws IOException {
        brokerRestApiClient.close();
        client.close();
    }

    @Parameters({"broker-hostname", "broker-port", "admin-username", "admin-password",
            "test-username", "test-password"})
    @Test(priority = 1,
            description = "create a queue by the admin user and grant consume, publish permissions to test user")
    public void testPublishConsumeQueueByTestUser(String brokerHost, String port, String adminUsername,
                                                  String adminPassword, String testUsername, String testPassword)
            throws IOException, NamingException, JMSException {

        String queueName = "AdminUserDacQueue";

        brokerRestApiClient.createQueue(queueName, true, false);
        brokerRestApiClient.bindQueue(queueName, queueName, "amq.direct");

        addUserGroupToQueue("consume", queueName, testUsername, adminUsername, adminPassword);
        addUserGroupToExchange("publish", "amq.direct", testUsername, adminPassword, adminPassword);

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(testUsername, testPassword, brokerHost, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = (Queue) initialContextForQueue.lookup(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 1;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        producerSession.close();

        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }

        subscriberSession.close();

        connection.close();
    }

    @Parameters({"admin-username", "admin-password", "test-username", "test-password"})
    @Test(priority = 2, description = "grant get permission to test user and retrieve queue details")
    public void testGetQueuesByTestUser(String adminUsername, String adminPassword, String testUsername,
                                        String testPassword) throws IOException {
        String queueName = "AdminUserDacQueue";

        addUserGroupToQueue("get", queueName, testUsername, adminUsername, adminPassword);

        HttpGet httpGet = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/" + queueName);
        ClientHelper.setAuthHeader(httpGet, testUsername, testPassword);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Incorrect status code.");

        String body = EntityUtils.toString(response.getEntity());
        QueueMetadata queueMetadata = objectMapper.readValue(body, QueueMetadata.class);

        Assert.assertEquals(queueMetadata.getName(), queueName, "Incorrect queue name.");
        Assert.assertEquals(queueMetadata.getConsumerCount().intValue(), 0, "JMS consumer should be present.");
        Assert.assertTrue(queueMetadata.isDurable());
        Assert.assertEquals(queueMetadata.getSize().intValue(), 0, "Queue should be empty.");
        Assert.assertFalse(queueMetadata.isAutoDelete());
    }

    @Parameters({"admin-username", "admin-password", "test-username", "test-password", "user1-username"})
    @Test(priority = 3,
            description = "grant permission to test user and test user grant permission to user1")
    public void testGrantPermissionQueuesByTestUser(String adminUsername, String adminPassword, String testUsername,
                                        String testPassword, String user1Username) throws IOException {
        String queueName = "AdminUserDacQueue";

        addUserGroupToQueue("grantPermission", queueName, testUsername, adminUsername, adminPassword);

        UserGroupList userGroupList = new UserGroupList();
        userGroupList.getUserGroups().add(user1Username);

        HttpPost httpPost = new HttpPost(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                + "/" + queueName + "/permissions/actions/consume/groups");
        ClientHelper.setAuthHeader(httpPost, testUsername, testPassword);
        String value = objectMapper.writeValueAsString(userGroupList);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Incorrect status code.");
    }

    @Parameters({"admin-username", "admin-password", "test-username", "test-password"})
    @Test(priority = 4, description = "grant delete permission to test user and delete a queue")
    public void testDeleteQueuesByTestUser(String adminUsername, String adminPassword, String testUsername,
                                        String testPassword) throws IOException {
        String queueName = "AdminUserDacQueue";

        addUserGroupToQueue("delete", queueName, testUsername, adminUsername, adminPassword);

        HttpDelete httpDelete = new HttpDelete(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/" + queueName);
        ClientHelper.setAuthHeader(httpDelete, testUsername, testPassword);
        CloseableHttpResponse response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    private void addUserGroupToQueue(String action, String queueName, String testUserGroup, String userName,
                                     String password) throws IOException {
        UserGroupList userGroupList = new UserGroupList();
        userGroupList.getUserGroups().add(testUserGroup);

        HttpPost httpPost = new HttpPost(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                + "/" + queueName + "/permissions/actions/" + action + "/groups");
        ClientHelper.setAuthHeader(httpPost, userName, password);
        String value = objectMapper.writeValueAsString(userGroupList);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Incorrect status code.");
    }

    private void addUserGroupToExchange(String action, String exchangeName, String testUserGroup, String userName,
                              String password) throws IOException {
        UserGroupList userGroupList = new UserGroupList();
        userGroupList.getUserGroups().add(testUserGroup);

        HttpPost httpPost = new HttpPost(apiBasePath + ExchangesApiDelegate.EXCHANGES_API_PATH
                + "/" + exchangeName + "/permissions/actions/" + action + "/groups");
        ClientHelper.setAuthHeader(httpPost, userName, password);
        String value = objectMapper.writeValueAsString(userGroupList);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Incorrect status code.");
    }
}
