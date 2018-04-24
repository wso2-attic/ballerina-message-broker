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
import io.ballerina.messaging.broker.core.rest.model.ExchangeMetadata;
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

/**
 * Test class to validate discretionary access control (DAC) for exchanges by different users
 */
public class RdbmsDiscretionaryAccessControlForExchangesTest {

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

    @Parameters({"admin-username", "admin-password", "test-username", "test-password"})
    @Test(priority = 1, description = "grant get permission to test user and retrieve exchange details")
    public void testGetExchangesByTestUser(String adminUsername, String adminPassword, String testUsername,
                                           String testPassword) throws IOException {

        String exchangeName = "DacExchange";
        String exchangeType = "direct";

        brokerRestApiClient.createExchange(exchangeName, exchangeType, true);

        addUserGroupToExchange("get", exchangeName, testUsername, adminUsername, adminPassword);

        HttpGet httpGet = new HttpGet(apiBasePath + "/exchanges/" + exchangeName);
        ClientHelper.setAuthHeader(httpGet, testUsername, testPassword);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
        String responsePayloadString = EntityUtils.toString(response.getEntity());
        ExchangeMetadata exchangeMetadata = objectMapper.readValue(responsePayloadString, ExchangeMetadata.class);

        Assert.assertEquals(exchangeMetadata.getName(), exchangeName, "Exchange name mismatch.");
        Assert.assertEquals(exchangeMetadata.getType(), exchangeType, "Exchange type mismatch.");
        Assert.assertEquals(exchangeMetadata.isDurable(), Boolean.TRUE, "Exchange durability mismatch");

    }

    @Parameters({"admin-username", "admin-password", "test-username", "test-password", "user1-username"})
    @Test(priority = 2,
            description = "grant permission to test user and test user grant permission to user1")
    public void testGrantPermissionExchangesByTestUser(String adminUsername, String adminPassword, String testUsername,
                                                       String testPassword, String user1Username) throws IOException {
        String exchangeName = "DacExchange";
        addUserGroupToExchange("grantPermission", exchangeName, testUsername, adminUsername, adminPassword);

        UserGroupList userGroupList = new UserGroupList();
        userGroupList.getUserGroups().add(user1Username);

        HttpPost httpPost = new HttpPost(apiBasePath + ExchangesApiDelegate.EXCHANGES_API_PATH
                + "/" + exchangeName + "/permissions/actions/publish/groups");
        ClientHelper.setAuthHeader(httpPost, testUsername, testPassword);
        String value = objectMapper.writeValueAsString(userGroupList);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                "Incorrect status code.");
    }


    @Parameters({"admin-username", "admin-password", "test-username", "test-password"})
    @Test(priority = 3, description = "grant delete permission to test user and delete an exchange")
    public void testDeleteExchangesByTestUser(String adminUsername, String adminPassword, String testUsername,
                                              String testPassword) throws IOException {
        String exchangeName = "DacExchange";
        addUserGroupToExchange("delete", exchangeName, testUsername, adminUsername, adminPassword);

        HttpDelete httpDelete = new HttpDelete(apiBasePath + "/exchanges/" + exchangeName);
        ClientHelper.setAuthHeader(httpDelete, testUsername, testPassword);
        CloseableHttpResponse response = client.execute(httpDelete);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
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
