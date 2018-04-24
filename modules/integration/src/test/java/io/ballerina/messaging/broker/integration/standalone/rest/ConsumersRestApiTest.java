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
import io.ballerina.messaging.broker.core.rest.model.ConsumerMetadata;
import io.ballerina.messaging.broker.core.rest.model.Error;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import io.ballerina.messaging.broker.integration.util.HttpClientHelper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.naming.InitialContext;

/**
 * Test consumer api
 */
public class ConsumersRestApiTest {

    private String apiBasePath;

    private CloseableHttpClient client;

    private ObjectMapper objectMapper;


    @Parameters({"broker-hostname", "broker-rest-port"})
    @BeforeClass
    public void setUp(String brokerHost, String port) throws Exception {
        apiBasePath = HttpClientHelper.getRestApiBasePath(brokerHost, port);
        objectMapper = new ObjectMapper();
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

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test
    public void testRetrieveConsumerList(String username, String password,
                                         String hostname, String port) throws Exception {
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
        QueueReceiver receiver1 = queueSession.createReceiver(queue);
        QueueReceiver receiver2 = queueSession.createReceiver(queue);

        HttpGet httpGet = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                                              + "/" + queueName + "/consumers");
        ClientHelper.setAuthHeader(httpGet, username, password);

        CloseableHttpResponse response = client.execute(httpGet);
        String body = EntityUtils.toString(response.getEntity());

        ConsumerMetadata[] consumers = objectMapper.readValue(body, ConsumerMetadata[].class);

        Assert.assertEquals(consumers.length, 2, "Number of consumers returned is incorrect.");

        receiver1.close();
        receiver2.close();
        queueSession.close();
        connection.close();
    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test
    public void testSpecificConsumerRetrieval(String username, String password,
                                              String hostname, String port) throws Exception {
        String queueName = "testSpecificConsumerRetrieval";

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

        HttpGet getAllConsumers = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                                              + "/" + queueName + "/consumers");
        ClientHelper.setAuthHeader(getAllConsumers, username, password);

        CloseableHttpResponse response = client.execute(getAllConsumers);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String body = EntityUtils.toString(response.getEntity());

        ConsumerMetadata[] consumers = objectMapper.readValue(body, ConsumerMetadata[].class);

        Assert.assertTrue(consumers.length > 0, "Number of consumers returned is incorrect.");

        int id = consumers[0].getId();
        HttpGet getConsumer = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/"
                                                  + queueName + "/consumers/" + id);
        ClientHelper.setAuthHeader(getConsumer, username, password);

        response = client.execute(getConsumer);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String consumerString = EntityUtils.toString(response.getEntity());
        ConsumerMetadata consumerMetadata = objectMapper.readValue(consumerString, ConsumerMetadata.class);

        Assert.assertEquals(consumerMetadata.getId().intValue(), id, "incorrect message id");

        receiver.close();
        queueSession.close();
        connection.close();
    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test
    public void testNonExistingConsumer(String username, String password,
                                        String hostname, String port) throws Exception {

        String queueName = "testNonExistingConsumer";

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
        QueueReceiver receiver1 = queueSession.createReceiver(queue);

        HttpGet getAllConsumers = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                                              + "/" + queueName + "/consumers");
        ClientHelper.setAuthHeader(getAllConsumers, username, password);

        CloseableHttpResponse response = client.execute(getAllConsumers);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                            "Incorrect status code");
        String consumerArray = EntityUtils.toString(response.getEntity());
        ConsumerMetadata[] consumers = objectMapper.readValue(consumerArray, ConsumerMetadata[].class);

        Assert.assertEquals(consumers.length, 1, "There should be a single consumer");
        int id = consumers[0].getId();
        receiver1.close();

        HttpGet getConsumer = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                                              + "/" + queueName + "/consumers/" + String.valueOf(id));
        ClientHelper.setAuthHeader(getConsumer, username, password);

        response = client.execute(getConsumer);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);

        String errorMessage = EntityUtils.toString(response.getEntity());
        Error error = objectMapper.readValue(errorMessage, Error.class);

        Assert.assertFalse(error.getMessage().isEmpty(), "Error message should be non empty.");
    }

    @Parameters({"admin-username", "admin-password"})
    @Test
    public void testConsumerInNonExistingQueue(String username, String password) throws Exception {
        String queueName = "testConsumerInNonExistingQueue";
        HttpGet httpGet = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                                              + "/" + queueName + "/consumers");
        ClientHelper.setAuthHeader(httpGet, username, password);

        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND,
                            "Incorrect status code");
        String body = EntityUtils.toString(response.getEntity());
        Error error = objectMapper.readValue(body, Error.class);

        Assert.assertFalse(error.getMessage().isEmpty(), "Error message shouldn't be empty.");

    }
}
