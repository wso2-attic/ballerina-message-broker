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
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.ballerina.messaging.broker.core.Binding;
import io.ballerina.messaging.broker.core.rest.model.BindingCreateRequest;
import io.ballerina.messaging.broker.core.rest.model.BindingCreateResponse;
import io.ballerina.messaging.broker.core.rest.model.BindingSetInfo;
import io.ballerina.messaging.broker.core.rest.model.BindingSetInfoBindings;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import io.ballerina.messaging.broker.integration.util.HttpClientHelper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Tests related to bindings api
 */
public class BindingsRestApiTest {

    private String apiBasePath;

    private CloseableHttpClient client;

    private Connection amqpConnection;

    private ObjectMapper objectMapper;

    private String username;

    private String password;

    @Parameters({"broker-hostname", "broker-rest-port"})
    @BeforeClass
    public void setUp(String brokerHost, String restPort) throws Exception {
        apiBasePath = HttpClientHelper.getRestApiBasePath(brokerHost, restPort);
        objectMapper = new ObjectMapper();
    }

    @AfterClass
    public void tearDown() {
        apiBasePath = null;
    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @BeforeMethod
    public void setup(String username, String password, String brokerHost, String brokerPort)
            throws IOException, TimeoutException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        client = HttpClientHelper.prepareClient();
        amqpConnection = ClientHelper.getAmqpConnection(username, password, brokerHost, brokerPort);
        this.username = username;
        this.password = password;
    }

    @AfterMethod
    public void afterMethod() throws IOException {
        client.close();
        amqpConnection.close();
    }

    @Test
    public void testRetrieveAllBindings() throws Exception {

        // Setup
        String exchangeName = "retrieveBindingsTestExchange";
        Channel channel = amqpConnection.createChannel();
        channel.exchangeDeclare(exchangeName, "topic");

        String queue1 = "testRetrieveAllBindings1";
        channel.queueDeclare(queue1, false, false, false, new HashMap<>());

        String queue2 = "testRetrieveAllBindings2";
        channel.queueDeclare(queue2, false, false, false, new HashMap<>());

        String queue3 = "testRetrieveAllBindings3";
        channel.queueDeclare(queue3, false, false, false, new HashMap<>());

        Map<String, Object> arguments = new HashMap<>();
        String filterString = "CorrelationId='12345fde'";
        arguments.put(Binding.JMS_SELECTOR_ARGUMENT.toString(), filterString);

        channel.queueBind(queue1, exchangeName, "topic1", arguments);
        channel.queueBind(queue2, exchangeName, "topic2");
        channel.queueBind(queue3, exchangeName, "topic2");
        // Get response
        HttpGet httpGet = new HttpGet(apiBasePath + "/exchanges/" + exchangeName + "/bindings");
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                            "Invalid status code received.");
        BindingSetInfo[] responseMessages = HttpClientHelper.getResponseMessage(response, BindingSetInfo[].class);

        Assert.assertEquals(responseMessages.length, 2);

        assertResponse(queue1, queue2, queue3, filterString, responseMessages[0]);
        assertResponse(queue1, queue2, queue3, filterString, responseMessages[1]);

        channel.exchangeDelete(exchangeName, false);
        channel.close();
    }

    @Test
    public void testRetrieveFromNonExistingExchange() throws Exception {
        // Get response
        String nonExistingExchangeName = "testRetrieveFromNonExistingExchange";
        HttpGet httpGet = new HttpGet(apiBasePath + "/exchanges/" + nonExistingExchangeName + "/bindings");
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST);
        Error error = HttpClientHelper.getResponseMessage(response, Error.class);

        Assert.assertFalse(error.getMessage().isEmpty());
    }

    private void assertResponse(String queue1, String queue2, String queue3,
                                String filterExpression, BindingSetInfo responseMessage) {
        String routingPattern = responseMessage.getBindingPattern();
        if ("topic1".equals(routingPattern)) {
            BindingSetInfoBindings bindingInfo = responseMessage.getBindings().get(0);
            Assert.assertEquals(bindingInfo.getQueueName(), queue1);
            Assert.assertEquals(bindingInfo.getFilterExpression(), filterExpression);
        } else {
            Assert.assertEquals(routingPattern, "topic2");
            BindingSetInfoBindings bindingInfo1 = responseMessage.getBindings().get(0);
            BindingSetInfoBindings bindingInfo2 = responseMessage.getBindings().get(1);
            if (queue2.equals(bindingInfo1.getQueueName())) {
                Assert.assertEquals(bindingInfo2.getQueueName(), queue3);
            } else {
                Assert.assertEquals(bindingInfo1.getQueueName(), queue3);
                Assert.assertEquals(bindingInfo2.getQueueName(), queue2);
            }
        }
    }

    @Test(dataProvider = "bindingData")
    public void testCreateBinding(String queueName, String bindingPattern) throws IOException, TimeoutException {

        Channel channel = amqpConnection.createChannel();
        channel.queueDeclare(queueName, false, false, false, new HashMap<>());
        String exchangeName = "amq.direct";
        HttpPost httpPost = new HttpPost(apiBasePath + "/queues/" + queueName + "/bindings");
        ClientHelper.setAuthHeader(httpPost, username, password);
        BindingCreateRequest createRequest = new BindingCreateRequest().bindingPattern(bindingPattern)
                                                                       .exchangeName(exchangeName);

        String payloadString = objectMapper.writeValueAsString(createRequest);
        StringEntity stringEntity = new StringEntity(payloadString, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED);

        BindingCreateResponse responseMessage = HttpClientHelper.getResponseMessage(response,
                                                                                    BindingCreateResponse.class);
        Assert.assertEquals(responseMessage.getMessage(), "Binding created.");

        channel.queueUnbind(queueName, exchangeName, bindingPattern);
        channel.close();
    }

    @Test(dataProvider = "bindingData")
    public void testCreateBindingWithInvalidExchange(String queueName, String bindingPattern)
            throws IOException, TimeoutException {
        Channel channel = amqpConnection.createChannel();
        channel.queueDeclare(queueName, false, false, false, new HashMap<>());
        channel.close();
        String exchangeName = "InvalidExchange";
        HttpPost httpPost = new HttpPost(apiBasePath + "/queues/" + queueName + "/bindings");
        ClientHelper.setAuthHeader(httpPost, username, password);
        BindingCreateRequest createRequest = new BindingCreateRequest().bindingPattern(bindingPattern)
                                                                     .exchangeName(exchangeName);

        String payloadString = objectMapper.writeValueAsString(createRequest);
        StringEntity stringEntity = new StringEntity(payloadString, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST);

        Error error = HttpClientHelper.getResponseMessage(response, Error.class);

        Assert.assertFalse(error.getMessage().isEmpty());
    }

    @Test(dataProvider = "bindingData")
    public void testCreateBindingWithFilters(String queueName, String bindingPattern)
            throws IOException, TimeoutException {
        Channel channel = amqpConnection.createChannel();
        channel.queueDeclare(queueName, false, false, false, new HashMap<>());

        String exchangeName = "amq.topic";
        String filter = "CorrelationId = 'testId123'";
        HttpPost httpPost = new HttpPost(apiBasePath + "/queues/" + queueName + "/bindings");
        ClientHelper.setAuthHeader(httpPost, username, password);
        BindingCreateRequest createRequest = new BindingCreateRequest().bindingPattern(bindingPattern)
                                                                       .exchangeName(exchangeName)
                                                                       .filterExpression(filter);

        String payloadString = objectMapper.writeValueAsString(createRequest);
        StringEntity stringEntity = new StringEntity(payloadString, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED);

        BindingCreateResponse responseMessage = HttpClientHelper.getResponseMessage(response,
                                                                                    BindingCreateResponse.class);

        Assert.assertEquals(responseMessage.getMessage(), "Binding created.");

        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put(Binding.JMS_SELECTOR_ARGUMENT.toString(), filter);
        channel.queueUnbind(queueName, exchangeName, bindingPattern, arguments);
        channel.close();
    }

    @DataProvider(name = "bindingData")
    public static Object[][] bindingData() {
        return new Object[][]{
                {"tmpQueue", "myTopic"},
                {"topicQueue", "sports.cricket"},
                };
    }
}
