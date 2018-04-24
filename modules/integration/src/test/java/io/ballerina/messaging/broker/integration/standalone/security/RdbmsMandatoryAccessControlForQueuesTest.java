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
import io.ballerina.messaging.broker.core.rest.QueuesApiDelegate;
import io.ballerina.messaging.broker.core.rest.model.QueueCreateRequest;
import io.ballerina.messaging.broker.core.rest.model.QueueCreateResponse;
import io.ballerina.messaging.broker.core.rest.model.QueueMetadata;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import io.ballerina.messaging.broker.integration.util.HttpClientHelper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
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
 * Test class to validate the mandatory access control (MAC) for queues by different users.
 */
public class RdbmsMandatoryAccessControlForQueuesTest {

    private String apiBasePath;

    private CloseableHttpClient client;

    private ObjectMapper objectMapper;

    private BasicResponseHandler responseHandler;

    @Parameters({"broker-hostname", "broker-rest-port"})
    @BeforeClass
    public void setUp(String brokerHost, String port) throws Exception {
        apiBasePath = HttpClientHelper.getRestApiBasePath(brokerHost, port);
        objectMapper = new ObjectMapper();
        responseHandler = new BasicResponseHandler();
    }

    @BeforeMethod
    public void setup() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        client = HttpClientHelper.prepareClient();
    }

    @AfterClass
    public void tearDown() throws Exception {
        apiBasePath = null;
        client.close();
    }

    @AfterMethod
    public void afterMethod() throws IOException {
        client.close();
    }

    @Parameters({"broker-hostname", "broker-port", "admin-username", "admin-password"})
    @Test(priority = 1,
            description = "create and publish to a queue by a user who has queues:create and queues:publish scopes")
    public void testCreateAndPublishByAdminUser(String brokerHostname,
                                                     String port,
                                                     String adminUsername,
                                                     String adminPassword) throws NamingException, JMSException {
        String queueName = "testCreateAndPublishScopeByAdminUser";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 1;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        producerSession.close();

        connection.close();
    }

    @Parameters({"broker-hostname", "broker-port", "admin-username", "admin-password"})
    @Test(priority = 2,
            description = "create and consume from a queue by a user who has queues:create and queues:consume scope")
    public void testCreateAndConsumeByAdminUser(String brokerHostname,
                                                     String port,
                                                     String adminUsername,
                                                     String adminPassword) throws NamingException, JMSException {
        String queueName = "testCreateAndPublishScopeByAdminUser";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        int numberOfMessages = 1;
        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        subscriberSession.close();

        connection.close();
    }

    @Parameters({"broker-hostname", "broker-port", "test-username", "test-password"})
    @Test(priority = 1,
            description = "create and publish to a queue by a user who does not have queues:create and " +
                    "queues:publish scopes",
            expectedExceptions = JMSException.class,
            expectedExceptionsMessageRegExp = ".*error code 403: access refused.*")
    public void testCreateAndPublishByTestUser(String brokerHostname,
                                                    String port,
                                                    String testUsername,
                                                    String testPassword) throws NamingException, JMSException {
        String queueName = "testCreateAndPublishScopeByTestUser";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(testUsername, testPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        int numberOfMessages = 1;
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(producerSession.createTextMessage("Test message " + i));
        }
        producerSession.close();

        connection.close();
    }

    @Parameters({"broker-hostname", "broker-port", "test-username", "test-password"})
    @Test(priority = 2, description = "create and consume from a queue by a user who does not have queues:create and " +
            "queues:consume scope",
            expectedExceptions = JMSException.class,
            expectedExceptionsMessageRegExp = ".*error code 403: access refused.*")
    public void testCreateAndConsumeByTestUser(String brokerHostname,
                                                    String port,
                                                    String testUsername,
                                                    String testPassword) throws NamingException, JMSException {
        String queueName = "testCreateAndPublishScopeByTestUser";

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(testUsername, testPassword, brokerHostname, port)
                .withQueue(queueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        Session subscriberSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        int numberOfMessages = 1;
        for (int i = 0; i < numberOfMessages; i++) {
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        subscriberSession.close();

        connection.close();
    }

    @Parameters({"admin-username", "admin-password"})
    @Test(description = "create a queue by a user who has queues:create scope")
    public void testCreateQueueByAdminUser(String username, String password) throws IOException {
        String queueName = "testCreateQueueByAdminUser";
        QueueCreateRequest request = new QueueCreateRequest()
                .name(queueName).durable(false).autoDelete(false);

        HttpPost httpPost = new HttpPost(apiBasePath + "/queues");
        ClientHelper.setAuthHeader(httpPost, username, password);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED);
        Assert.assertTrue(response.getFirstHeader(HttpHeaders.LOCATION)
                        .getValue().contains("/queues/" + queueName),
                "Incorrect location header");

        String body = responseHandler.handleResponse(response);
        QueueCreateResponse queueCreateResponse = objectMapper.readValue(body, QueueCreateResponse.class);

        Assert.assertFalse(queueCreateResponse.getMessage().isEmpty(), "Response body shouldn't be empty");

    }

    @Parameters({"test-username", "test-password"})
    @Test(description = "create a queue by a user who does not have queues:create scope")
    public void testCreateQueueByTestUser(String username, String password) throws IOException {
        String queueName = "testCreateQueueByTestUser";
        QueueCreateRequest request = new QueueCreateRequest()
                .name(queueName).durable(false).autoDelete(false);

        HttpPost httpPost = new HttpPost(apiBasePath + "/queues");
        ClientHelper.setAuthHeader(httpPost, username, password);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN);
    }

    @Parameters({"admin-username", "admin-password"})
    @Test(description = "retrieve queue details by a user who has queues:get scope")
    public void testGetQueueByAdminUser(String username, String password) throws IOException {
        HttpGet httpGet = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH);
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Incorrect status code");

        String body = EntityUtils.toString(response.getEntity());

        QueueMetadata[] queueMetadata = objectMapper.readValue(body, QueueMetadata[].class);

        Assert.assertTrue(queueMetadata.length > 0, "Queue metadata list shouldn't be empty.");
    }

    @Parameters({"test-username", "test-password"})
    @Test(description = "retrieve queue details by a user who does not have queues:get scope")
    public void testGetQueueByTestUser(String username, String password) throws IOException {
        HttpGet httpGet = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH);
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN);
    }

    @Parameters({"admin-username", "admin-password"})
    @Test(priority = 3, description = "delete a queue by a user who has queues:delete scope")
    public void testDeleteQueueByAdminUser(String username, String password) throws IOException {
        String queueName = "testCreateQueueByAdminUser";

        // Delete the queue.
        HttpDelete httpDelete = new HttpDelete(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/" + queueName);
        ClientHelper.setAuthHeader(httpDelete, username, password);
        CloseableHttpResponse response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Parameters({"test-username", "test-password"})
    @Test(priority = 3, description = "delete a queue by a user who does not have queues:delete scope")
    public void testDeleteQueueByTestUser(String username, String password) throws IOException {
        String queueName = "testCreateAndPublishScopeByAdminUser";

        // Delete the queue.
        HttpDelete httpDelete = new HttpDelete(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/" + queueName);
        ClientHelper.setAuthHeader(httpDelete, username, password);
        CloseableHttpResponse response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN);
    }

}
