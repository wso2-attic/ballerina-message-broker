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

package io.ballerina.messaging.broker.integration.standalone.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.messaging.broker.core.rest.QueuesApiDelegate;
import io.ballerina.messaging.broker.core.rest.model.Error;
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
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Test queues admin service api
 */
public class QueuesRestApiTest {

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

    @Parameters({"admin-username", "admin-password"})
    @Test
    public void testPositiveCreateQueue(String username, String password) throws IOException {
        String queueName = "testPositiveCreateQueue";
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

    @Parameters({"admin-username", "admin-password"})
    @Test
    public void testDuplicateQueueCreation(String username, String password) throws IOException {
        QueueCreateRequest request = new QueueCreateRequest()
                .name("testDuplicateQueueCreation").durable(false).autoDelete(false);
        HttpPost httpPost = new HttpPost(apiBasePath + "/queues");
        ClientHelper.setAuthHeader(httpPost, username, password);

        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED, "Incorrect status code");

        response = client.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST, "Incorrect status "
                + "code");

        String body = EntityUtils.toString(response.getEntity());
        Error error = objectMapper.readValue(body, Error.class);
        Assert.assertFalse(error.getMessage().isEmpty(), "Response body shouldn't be empty");
    }


    @Parameters({"admin-username", "admin-password"})
    @Test
    public void testQueueRetrieval(String username, String password) throws IOException {
        HttpGet httpGet = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH);
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Incorrect status code");

        String body = EntityUtils.toString(response.getEntity());

        QueueMetadata[] queueMetadata = objectMapper.readValue(body, QueueMetadata[].class);

        Assert.assertTrue(queueMetadata.length > 0, "Queue metadata list shouldn't be empty.");
    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test
    public void testSpecificQueueRetrieval(String username, String password, String hostname, String port)
            throws JMSException, NamingException, IOException {

        String queueName = "testSpecificQueueRetrieval";

        // Create a durable queue using a JMS client
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(username, password, hostname, port)
                .withQueue(queueName)
                .build();

        QueueConnectionFactory connectionFactory
                = (QueueConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        QueueConnection connection = connectionFactory.createQueueConnection();
        connection.start();

        QueueSession queueSession = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
        Queue queue = queueSession.createQueue(queueName);
        QueueReceiver receiver = queueSession.createReceiver(queue);

        // Test queue retrieval through REST API
        HttpGet httpGet = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/" + queueName);
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Incorrect status code.");

        String body = EntityUtils.toString(response.getEntity());
        QueueMetadata queueMetadata = objectMapper.readValue(body, QueueMetadata.class);

        Assert.assertEquals(queueMetadata.getName(), queueName, "Incorrect queue name.");
        Assert.assertEquals(queueMetadata.getConsumerCount().intValue(), 1, "JMS consumer should be present.");
        Assert.assertTrue(queueMetadata.isDurable());
        Assert.assertEquals(queueMetadata.getSize().intValue(), 0, "Queue should be empty.");
        Assert.assertFalse(queueMetadata.isAutoDelete());

        receiver.close();
        queueSession.close();
        connection.close();
    }

    @Parameters({"admin-username", "admin-password"})
    @Test
    public void testDeleteQueue(String username, String password) throws IOException {
        String queueName = "testDeleteQueue";

        // Create a queue to delete.
        QueueCreateRequest request = new QueueCreateRequest()
                .name(queueName).durable(false).autoDelete(false);

        HttpPost httpPost = new HttpPost(apiBasePath + "/queues");
        ClientHelper.setAuthHeader(httpPost, username, password);

        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED);

        // Delete the queue.
        HttpDelete httpDelete = new HttpDelete(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/" + queueName);
        ClientHelper.setAuthHeader(httpDelete, username, password);
        response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Parameters({"admin-username", "admin-password"})
    @Test
    public void testPurgeQueue(String username, String password) throws IOException {
        String queueName = "QueuesRestApiTestTestPurgeQueue";

        // Create a queue to delete.
        QueueCreateRequest request = new QueueCreateRequest()
                .name(queueName).durable(false).autoDelete(false);

        HttpPost httpPost = new HttpPost(apiBasePath + "/queues");
        ClientHelper.setAuthHeader(httpPost, username, password);

        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED);

        // Delete the queue.
        HttpDelete httpDelete = new HttpDelete(
                apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/" + queueName + "/messages");
        ClientHelper.setAuthHeader(httpDelete, username, password);
        response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    @Parameters({"admin-username", "admin-password"})
    @Test
    public void testNegativeDeleteQueue(String username, String password) throws IOException {
        String queueName = "testNegativeDeleteQueue";
        HttpDelete httpDelete = new HttpDelete(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/" + queueName);
        ClientHelper.setAuthHeader(httpDelete, username, password);
        CloseableHttpResponse response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);
    }
}
