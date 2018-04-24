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

package io.ballerina.messaging.broker.integration.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.messaging.broker.core.rest.ExchangesApiDelegate;
import io.ballerina.messaging.broker.core.rest.QueuesApiDelegate;
import io.ballerina.messaging.broker.core.rest.model.BindingCreateRequest;
import io.ballerina.messaging.broker.core.rest.model.ExchangeCreateRequest;
import io.ballerina.messaging.broker.core.rest.model.ExchangeMetadata;
import io.ballerina.messaging.broker.core.rest.model.QueueCreateRequest;
import io.ballerina.messaging.broker.core.rest.model.QueueMetadata;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.testng.Assert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Rest api client class to for testing purposes
 */
public class BrokerRestApiClient {

    private final String userName;

    private final String password;

    private final String apiBasePath;

    private final CloseableHttpClient httpClient;

    private final ObjectMapper objectMapper;

    public BrokerRestApiClient(String userName, String password,
                               String restPort, String hostname) throws URISyntaxException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException {
        this.userName = userName;
        this.password = password;
        this.apiBasePath = HttpClientHelper.getRestApiBasePath(hostname, restPort);
        this.httpClient = HttpClientHelper.prepareClient();
        objectMapper = new ObjectMapper();
    }

    public QueueMetadata getQueueMetadata(String queueName) throws IOException {
        HttpGet httpGet = new HttpGet(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/" + queueName);
        ClientHelper.setAuthHeader(httpGet, userName, password);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        return HttpClientHelper.getResponseMessage(response, QueueMetadata.class);
    }

    public void createQueue(String queueName, boolean durable, boolean autoDelete) throws IOException {
        // create a queue
        QueueCreateRequest request = new QueueCreateRequest()
                .name(queueName).durable(durable).autoDelete(autoDelete);

        HttpPost httpPost = new HttpPost(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH);
        ClientHelper.setAuthHeader(httpPost, userName, password);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED);
    }

    public void bindQueue(String queueName, String bindingPattern, String exchangeName) throws IOException {
        HttpPost httpPost = new HttpPost(apiBasePath + "/queues/" + queueName + "/bindings");
        ClientHelper.setAuthHeader(httpPost, userName, password);
        BindingCreateRequest createRequest = new BindingCreateRequest().bindingPattern(bindingPattern)
                .exchangeName(exchangeName);

        String payloadString = objectMapper.writeValueAsString(createRequest);
        StringEntity stringEntity = new StringEntity(payloadString, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = httpClient.execute(httpPost);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED);
    }

    public void deleteQueue(String queueName) throws IOException {
        HttpDelete httpDelete = new HttpDelete(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH + "/" + queueName);
        ClientHelper.setAuthHeader(httpDelete, userName, password);
        CloseableHttpResponse response = httpClient.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }

    public void createExchange(String exchangeName, String exchangeType, boolean durable) throws IOException {
        // create a queue
        ExchangeCreateRequest request = new ExchangeCreateRequest()
                .name(exchangeName).durable(durable).type(exchangeType);

        HttpPost httpPost = new HttpPost(apiBasePath + ExchangesApiDelegate.EXCHANGES_API_PATH);
        ClientHelper.setAuthHeader(httpPost, userName, password);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED);
    }

    public ExchangeMetadata getExchangeMetadata(String exchangeName) throws IOException {
        HttpGet httpGet = new HttpGet(apiBasePath + ExchangesApiDelegate.EXCHANGES_API_PATH + "/" + exchangeName);
        ClientHelper.setAuthHeader(httpGet, userName, password);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        return HttpClientHelper.getResponseMessage(response, ExchangeMetadata.class);
    }

    public void deleteExchange(String exchangeName) throws IOException {
        HttpDelete httpDelete = new HttpDelete(apiBasePath + ExchangesApiDelegate.EXCHANGES_API_PATH
                                                       + "/" + exchangeName);
        ClientHelper.setAuthHeader(httpDelete, userName, password);
        CloseableHttpResponse response = httpClient.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }
    public void close() throws IOException {
        httpClient.close();
    }
}
