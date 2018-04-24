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
import io.ballerina.messaging.broker.core.rest.model.ChangeOwnerRequest;
import io.ballerina.messaging.broker.integration.util.BrokerRestApiClient;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import io.ballerina.messaging.broker.integration.util.HttpClientHelper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
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
 * Test class to validate the mandatory access control (MAC) for grants by different users.
 */
public class RdbmsMandatoryAccessControlGrantTest {

    private String apiBasePath;

    private CloseableHttpClient client;

    private ObjectMapper objectMapper;

    private String adminUsername;
    private String adminPassword;
    private String testUsername;
    private String testPassword;
    private String user1Username;

    private String exchangeName = "GrantExchange";
    private String exchangeType = "direct";
    private String queueName = "GrantQueue";

    private BrokerRestApiClient brokerRestApiClient;

    @Parameters({"broker-hostname", "broker-rest-port", "admin-username", "admin-password",
            "test-username", "test-password", "user1-username"})
    @BeforeClass
    public void setUp(String brokerHost, String port, String adminUsername, String adminPassword,
                      String testUsername, String testPassword, String user1Username)
            throws Exception {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.testUsername = testUsername;
        this.testPassword = testPassword;
        this.user1Username = user1Username;
        apiBasePath = HttpClientHelper.getRestApiBasePath(brokerHost, port);
        objectMapper = new ObjectMapper();
        brokerRestApiClient = new BrokerRestApiClient(adminUsername, adminPassword, port, brokerHost);
        brokerRestApiClient.createExchange(exchangeName, exchangeType, true);
        brokerRestApiClient.createQueue(queueName, true, false);
    }

    @BeforeMethod
    public void setup() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        client = HttpClientHelper.prepareClient();
    }

    @AfterClass
    public void tearDown() throws Exception {
        apiBasePath = null;
        client.close();
        brokerRestApiClient.close();
    }

    @AfterMethod
    public void afterMethod() throws IOException {
        client.close();
    }

    @Test(description = "Change owner of an exchange by a user who has resources:grant scope")
    public void testChangeOwnerOfExchangeByAdminUser() throws Exception {

        ChangeOwnerRequest request = new ChangeOwnerRequest().owner(user1Username);

        HttpPut httpPut = new HttpPut(apiBasePath + ExchangesApiDelegate.EXCHANGES_API_PATH
                + "/" + exchangeName + "/permissions/owner/");
        ClientHelper.setAuthHeader(httpPut, adminUsername, adminPassword);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPut.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPut);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT,
                "Incorrect status code");

    }

    @Test(description = "Change owner an exchange by a user who does not have resources:grant scope")
    public void testChangeOwnerOfExchangeByTestUser() throws Exception {

        ChangeOwnerRequest request = new ChangeOwnerRequest().owner(user1Username);

        HttpPut httpPut = new HttpPut(apiBasePath + ExchangesApiDelegate.EXCHANGES_API_PATH
                + "/" + exchangeName + "/permissions/owner/");
        ClientHelper.setAuthHeader(httpPut, testUsername, testPassword);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPut.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPut);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN,
                "Incorrect status code");
    }

    @Test(description = "Change owner of a queue by a user who has resources:grant scope")
    public void testChangeOwnerOfQueueByAdminUser() throws Exception {

        ChangeOwnerRequest request = new ChangeOwnerRequest().owner(user1Username);

        HttpPut httpPut = new HttpPut(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                + "/" + queueName + "/permissions/owner/");
        ClientHelper.setAuthHeader(httpPut, adminUsername, adminPassword);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPut.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPut);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT,
                "Incorrect status code");

    }

    @Test(description = "Change owner a queue by a user who does not have resources:grant scope")
    public void testChangeOwnerOfQueueByTestUser() throws Exception {

        ChangeOwnerRequest request = new ChangeOwnerRequest().owner(user1Username);

        HttpPut httpPut = new HttpPut(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                + "/" + queueName + "/permissions/owner/");
        ClientHelper.setAuthHeader(httpPut, testUsername, testPassword);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPut.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPut);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN,
                "Incorrect status code");
    }
}
