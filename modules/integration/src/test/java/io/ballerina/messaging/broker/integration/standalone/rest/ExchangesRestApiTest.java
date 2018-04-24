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
import io.ballerina.messaging.broker.core.rest.model.Error;
import io.ballerina.messaging.broker.core.rest.model.ExchangeCreateRequest;
import io.ballerina.messaging.broker.core.rest.model.ExchangeMetadata;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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
 * Tests related to /exchanges api
 */
public class ExchangesRestApiTest {

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


    @Test(dataProvider = "exchangeData")
    public void testExchangeCreate(String exchangeName, String type) throws Exception {
        ExchangeCreateRequest request = new ExchangeCreateRequest()
                .name(exchangeName).durable(false).type(type);

        HttpPost httpPost = new HttpPost(apiBasePath + "/exchanges");
        ClientHelper.setAuthHeader(httpPost, username, password);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED);
        Assert.assertTrue(response.getFirstHeader(HttpHeaders.LOCATION)
                        .getValue().contains("/exchanges/" + exchangeName),
                            "Incorrect location header");


        Channel channel = amqpConnection.createChannel();

        channel.exchangeDeclarePassive("amq.direct"); // the broker will raise a 404 channel exception if the
        // named exchange does not exist.

        channel.exchangeDelete(exchangeName, true);
    }

    @Test(dataProvider = "exchangeData")
    public void testExchangeRetrieval(String exchangeName, String type) throws Exception {
        Channel channel = amqpConnection.createChannel();
        channel.exchangeDeclare(exchangeName, type);

        HttpGet httpGet = new HttpGet(apiBasePath + "/exchanges/" + exchangeName);
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String responsePayloadString = EntityUtils.toString(response.getEntity());
        ExchangeMetadata exchangeMetadata = objectMapper.readValue(responsePayloadString, ExchangeMetadata.class);

        Assert.assertEquals(exchangeMetadata.getName(), exchangeName, "Exchange name mismatch.");
        Assert.assertEquals(exchangeMetadata.getType(), type, "Exchange type mismatch.");
        Assert.assertEquals(exchangeMetadata.isDurable(), Boolean.FALSE, "Exchange durability mismatch");

        channel.exchangeDelete(exchangeName, true);
    }

    @Test (dataProvider = "exchangeData")
    public void testRetrieveExchangeWithInvalidName(String exchangeName, String type) throws Exception {
        HttpGet httpGet = new HttpGet(apiBasePath + "/exchanges/" + exchangeName);
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);

        Error error = HttpClientHelper.getResponseMessage(response, Error.class);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);
        Assert.assertFalse(error.getMessage().isEmpty(), "Error message shouldn't be empty.");

    }

    @Test (dataProvider = "exchangeData")
    public void testRetrieveAllExchanges(String exchangeName, String type) throws Exception {
        Channel channel = amqpConnection.createChannel();
        channel.exchangeDeclare(exchangeName, type);

        HttpGet httpGet = new HttpGet(apiBasePath + "/exchanges");
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Invalid status code");

        String responsePayloadString = EntityUtils.toString(response.getEntity());

        ExchangeMetadata[] exchanges = objectMapper.readValue(responsePayloadString, ExchangeMetadata[].class);
        Map<String, ExchangeMetadata> exchangeMetadataMap = new HashMap<>(exchanges.length);
        for (ExchangeMetadata exchange : exchanges) {
           exchangeMetadataMap.put(exchange.getName(), exchange);
        }

        validExchangeResponse(exchangeMetadataMap.get(exchangeName), exchangeName, type);

        String expectedExchangeName = "amq.direct";
        String expectedExchangeType = "direct";
        validExchangeResponse(exchangeMetadataMap.get(expectedExchangeName),
                              expectedExchangeName, expectedExchangeType);

        expectedExchangeName = "amq.topic";
        expectedExchangeType = "topic";
        validExchangeResponse(exchangeMetadataMap.get(expectedExchangeName),
                              expectedExchangeName, expectedExchangeType);

        channel.exchangeDelete(exchangeName, true);
        channel.close();

    }

    private void validExchangeResponse(ExchangeMetadata exchangeMetadata,
                                       String expectedExchangeName, String expectedExchangeType) {
        Assert.assertNotNull(exchangeMetadata);
        Assert.assertEquals(exchangeMetadata.getName(), expectedExchangeName);
        Assert.assertEquals(exchangeMetadata.getType(), expectedExchangeType);
    }

    @Test (dataProvider = "exchangeData")
    public void testDeleteExchange(String exchangeName, String type) throws Exception {

        Channel channel = amqpConnection.createChannel();
        channel.exchangeDeclare(exchangeName, type);
        channel.close();

        HttpDelete httpDelete = new HttpDelete(apiBasePath + "/exchanges/" + exchangeName);
        ClientHelper.setAuthHeader(httpDelete, username, password);
        CloseableHttpResponse response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        CloseableHttpResponse notFoundResponse = client.execute(httpDelete);

        Assert.assertEquals(notFoundResponse.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);

    }

    @Test (dataProvider = "exchangeData")
    public void testDeleteExchangeWithBindings(String exchangeName, String type) throws Exception {

        Channel channel = amqpConnection.createChannel();
        channel.exchangeDeclare(exchangeName, type);

        String queueName = "testDeleteExchangeWithBindings";
        channel.queueDeclare(queueName, false, false,
                             false, new HashMap<>());
        channel.queueBind(queueName, exchangeName, queueName);

        // Test delete with ifUnused not set
        HttpDelete httpDelete = new HttpDelete(apiBasePath + "/exchanges/" + exchangeName);
        ClientHelper.setAuthHeader(httpDelete, username, password);
        CloseableHttpResponse response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST);
        Error error = HttpClientHelper.getResponseMessage(response, Error.class);
        Assert.assertFalse(error.getMessage().isEmpty(), "Error message shouldn't be empty.");

        // Test delete with ifUnused set to false
        httpDelete = new HttpDelete(apiBasePath + "/exchanges/" + exchangeName + "?ifUnused=false");
        ClientHelper.setAuthHeader(httpDelete, username, password);
        response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

    }

    @DataProvider(name = "exchangeData")
    public static Object[][] createExchangeData() {
        return new Object[][]{
                {"testExchange", "direct"},
                {"myTopicExchange", "topic"}
        };
    }

}
