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
import io.ballerina.messaging.broker.core.rest.model.ExchangeCreateRequest;
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

/**
 * Test class to validate the mandatory access control (MAC) for exchanges by different users.
 */
public class RdbmsMandatoryAccessControlForExchangesTest {

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

    @Parameters({"admin-username", "admin-password"})
    @Test(priority = 1, description = "create an exchange by a user who has exchanges:create scope")
    public void testCreateExchangeByAdminUser(String username, String password) throws Exception {

        ExchangeCreateRequest request = new ExchangeCreateRequest()
                .name("adminUserExchange").durable(true).type("direct");

        HttpPost httpPost = new HttpPost(apiBasePath + "/exchanges");
        ClientHelper.setAuthHeader(httpPost, username, password);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_CREATED);
        Assert.assertTrue(response.getFirstHeader(HttpHeaders.LOCATION)
                        .getValue().contains("/exchanges/" + "adminUserExchange"), "Incorrect location header");

    }

    @Parameters({"test-username", "test-password"})
    @Test(description = "create an exchange by a user who does not have exchanges:create scope")
    public void testCreateExchangeByTestUser(String username, String password)
            throws Exception {
        ExchangeCreateRequest request = new ExchangeCreateRequest()
                .name("testUserExchange").durable(false).type("direct");

        HttpPost httpPost = new HttpPost(apiBasePath + "/exchanges");
        ClientHelper.setAuthHeader(httpPost, username, password);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN);

    }

    @Parameters({"admin-username", "admin-password"})
    @Test(description = "retrieve exchange details by a user who has exchanges:get scope")
    public void testGetExchangeByAdminUser(String username, String password) throws Exception {

        HttpGet httpGet = new HttpGet(apiBasePath + "/exchanges");
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

    }

    @Parameters({"test-username", "test-password"})
    @Test(description = "retrieve exchange details by a user who does not have exchanges:get scope")
    public void testGetExchangeByTestUser(String username, String password) throws Exception {

        HttpGet httpGet = new HttpGet(apiBasePath + "/exchanges");
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN);

    }

    @Parameters({"admin-username", "admin-password"})
    @Test(priority = 3, description = "delete an exchange by a user who has exchanges:delete scope")
    public void testDeleteExchangeByAdminUser(String username, String password) throws Exception {

        HttpDelete httpDelete = new HttpDelete(apiBasePath + "/exchanges/" + "adminUserExchange");
        ClientHelper.setAuthHeader(httpDelete, username, password);
        CloseableHttpResponse response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);

        CloseableHttpResponse notFoundResponse = client.execute(httpDelete);

        Assert.assertEquals(notFoundResponse.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);

    }

    @Parameters({"test-username", "test-password"})
    @Test(priority = 2, description = "delete an exchange by a user who does not have exchanges:delete scope")
    public void testDeleteExchangeByTestUser(String username, String password) throws Exception {

        HttpDelete httpDelete = new HttpDelete(apiBasePath + "/exchanges/" + "adminUserExchange");
        ClientHelper.setAuthHeader(httpDelete, username, password);
        CloseableHttpResponse response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN);

    }

}
