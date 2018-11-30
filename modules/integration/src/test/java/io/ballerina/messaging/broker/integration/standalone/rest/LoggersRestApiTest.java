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
import io.ballerina.messaging.broker.core.rest.LoggersApiDelegate;
import io.ballerina.messaging.broker.core.rest.model.LoggerMetadata;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import io.ballerina.messaging.broker.integration.util.HttpClientHelper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
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
import java.util.regex.Pattern;

/**
 * Test loggers admin service api
 */
public class LoggersRestApiTest {

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
    public void testLoggerRetrieval(String username, String password) throws IOException {
        HttpGet httpGet = new HttpGet(apiBasePath + LoggersApiDelegate.LOGGERS_API_PATH);
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Incorrect status code");

        String body = EntityUtils.toString(response.getEntity());

        LoggerMetadata[] loggerMetadata = objectMapper.readValue(body, LoggerMetadata[].class);

        Assert.assertTrue(loggerMetadata.length > 0, "Logger metadata list shouldn't be empty.");
    }

    @Parameters({"admin-username", "admin-password"})
    @Test
    public void testFilterdLoggerRetrieval(String username, String password) throws IOException {
        String filterName = "*broker.core*";
        HttpGet httpGet = new HttpGet(apiBasePath + "/loggers/" + filterName);
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Incorrect status code");

        String body = EntityUtils.toString(response.getEntity());

        LoggerMetadata[] loggerMetadatas = objectMapper.readValue(body, LoggerMetadata[].class);

        Assert.assertTrue(loggerMetadatas.length > 0, "Filtered Logger metadata list shouldn't be empty.");

        boolean filtered = true;
        Pattern filterPatten = Pattern.compile(filterName.replaceAll("\\*", ".*"));

        for (LoggerMetadata loggerMetadata : loggerMetadatas) {
            if (!filterPatten.matcher(loggerMetadata.getName()).matches()) {
                filtered = false;
                break;
            }
        }

        Assert.assertTrue(filtered, "Loggers have not filtered properly");
    }

    @Parameters({"admin-username", "admin-password"})
    @Test
    public void testPositiveUpdateLogLevel(String username, String password) throws IOException {
        String loggerName = "io.ballerina.messaging.broker.core.BrokerImpl";
        String loggerLevel = "WARN";
        LoggerMetadata loggerMetadata = new LoggerMetadata().name(loggerName).level(loggerLevel);

        HttpPut httpPut = new HttpPut(apiBasePath + LoggersApiDelegate.LOGGERS_API_PATH);
        ClientHelper.setAuthHeader(httpPut, username, password);
        String value = objectMapper.writeValueAsString(loggerMetadata);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPut.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPut);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        String body = responseHandler.handleResponse(response);

        Assert.assertTrue(body.contains("Changed log level"), "Invalid success message.");


    }

    @Parameters({"admin-username", "admin-password"})
    @Test
    public void testUpdatingInvalidLogger(String username, String password) throws IOException {
        String loggerName = "fake.BrokerImpl";
        String loggerLevel = "WARN";
        LoggerMetadata loggerMetadata = new LoggerMetadata().name(loggerName).level(loggerLevel);

        HttpPut httpPut = new HttpPut(apiBasePath + LoggersApiDelegate.LOGGERS_API_PATH);
        ClientHelper.setAuthHeader(httpPut, username, password);
        String value = objectMapper.writeValueAsString(loggerMetadata);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPut.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPut);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND, "Incorrect status code");

    }

    @Parameters({"admin-username", "admin-password"})
    @Test
    public void testUpdatingToInvalidLogLevel(String username, String password) throws IOException {
        String loggerName = "io.ballerina.messaging.broker.core.BrokerImpl";
        String loggerLevel = "WRN";

        HttpGet httpGet = new HttpGet(apiBasePath + "/loggers/" + loggerName);
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse getResponse = client.execute(httpGet);

        Assert.assertEquals(getResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Incorrect status code");

        String body = EntityUtils.toString(getResponse.getEntity());

        LoggerMetadata[] loggerMetadatas = objectMapper.readValue(body, LoggerMetadata[].class);

        Assert.assertTrue(loggerMetadatas.length == 1, "Logger " + loggerName + " should exits.");


        LoggerMetadata loggerMetadata = new LoggerMetadata().name(loggerName).level(loggerLevel);

        HttpPut httpPut = new HttpPut(apiBasePath + LoggersApiDelegate.LOGGERS_API_PATH);
        ClientHelper.setAuthHeader(httpPut, username, password);
        String value = objectMapper.writeValueAsString(loggerMetadata);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPut.setEntity(stringEntity);

        CloseableHttpResponse putResponse = client.execute(httpPut);

        Assert.assertEquals(putResponse.getStatusLine().getStatusCode(), HttpStatus.SC_BAD_REQUEST, "Incorrect status"
                                                                                                    + " code");

    }

}
