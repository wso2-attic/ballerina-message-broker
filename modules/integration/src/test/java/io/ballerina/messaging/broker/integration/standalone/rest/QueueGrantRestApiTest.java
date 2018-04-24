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
import io.ballerina.messaging.broker.core.rest.model.ChangeOwnerRequest;
import io.ballerina.messaging.broker.core.rest.model.QueueMetadata;
import io.ballerina.messaging.broker.core.rest.model.ResponseMessage;
import io.ballerina.messaging.broker.core.rest.model.UserGroupList;
import io.ballerina.messaging.broker.integration.util.BrokerRestApiClient;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import io.ballerina.messaging.broker.integration.util.HttpClientHelper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Tests for Queue resource permission grant REST API.
 */
public class QueueGrantRestApiTest {

    private String apiBasePath;

    private CloseableHttpClient client;

    private ObjectMapper objectMapper;

    private String userName;
    private String password;
    private String testUsername;
    private String port;
    private String brokerHost;

    private BrokerRestApiClient brokerRestApiClient;

    @Parameters({"broker-hostname", "broker-rest-port", "admin-username", "admin-password", "test-username"})
    @BeforeClass
    public void setUp(String brokerHost, String port, String userName, String password, String testUsername)
            throws Exception {
        this.userName = userName;
        this.password = password;
        this.testUsername = testUsername;
        this.port = port;
        this.brokerHost = brokerHost;
        apiBasePath = HttpClientHelper.getRestApiBasePath(brokerHost, port);
        objectMapper = new ObjectMapper();
    }

    @BeforeMethod
    public void setup() throws URISyntaxException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        client = HttpClientHelper.prepareClient();
        brokerRestApiClient = new BrokerRestApiClient(userName, password, port, brokerHost);
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

    @Test(dataProvider = "queueDurability")
    public void testChangeOwner(boolean durable) throws Exception {
        String queueName = "QueueOwnerChangeTest";

        brokerRestApiClient.createQueue(queueName, durable, false);

        ChangeOwnerRequest request = new ChangeOwnerRequest().owner(testUsername);

        HttpPut httpPut = new HttpPut(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                                              + "/" + queueName + "/permissions/owner/");
        ClientHelper.setAuthHeader(httpPut, userName, password);
        String value = objectMapper.writeValueAsString(request);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPut.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPut);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_NO_CONTENT,
                            "Incorrect status code");

        QueueMetadata queueMetadata = brokerRestApiClient.getQueueMetadata(queueName);

        Assert.assertEquals(queueMetadata.getOwner(), testUsername, "Incorrect owner.");

        brokerRestApiClient.deleteQueue(queueName);
    }

    @Test(dataProvider = "userGroupRequest")
    public void testAddGroup(boolean durable, String action) throws Exception {
        String queueName = "testAddGroup";
        String testUserGroup = "testGroup";

        brokerRestApiClient.createQueue(queueName, durable, false);

        addUserGroup(action, queueName, testUserGroup);

        QueueMetadata queueMetadata = brokerRestApiClient.getQueueMetadata(queueName);
        Assert.assertEquals(queueMetadata.getPermissions().size(), 1, "Queue should have only 1 permission.");
        Assert.assertEquals(queueMetadata.getPermissions().get(0).getAction(), action, "Action doesn't match.");

        brokerRestApiClient.deleteQueue(queueName);
    }

    private void addUserGroup(String action, String queueName, String testUserGroup) throws IOException {
        UserGroupList userGroupList = new UserGroupList();
        userGroupList.getUserGroups().add(testUserGroup);

        HttpPost httpPost = new HttpPost(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                                              + "/" + queueName + "/permissions/actions/" + action + "/groups");
        ClientHelper.setAuthHeader(httpPost, userName, password);
        String value = objectMapper.writeValueAsString(userGroupList);
        StringEntity stringEntity = new StringEntity(value, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                            "Incorrect status code.");
    }


    @Test (dataProvider = "userGroupRequest")
    public void testDeleteGroup(boolean durable, String action) throws Exception {
        String queueName = "testDeleteGroup";
        String userGroup = "testGroup";
        brokerRestApiClient.createQueue(queueName, durable, false);

        addUserGroup(action, queueName, userGroup);

        HttpDelete httpDelete = new HttpDelete(apiBasePath + QueuesApiDelegate.QUEUES_API_PATH
                                                 + "/" + queueName + "/permissions/actions/" + action
                                                       + "/groups/" + userGroup);
        ClientHelper.setAuthHeader(httpDelete, userName, password);
        CloseableHttpResponse response = client.execute(httpDelete);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                            "Incorrect status code.");
        ResponseMessage responseMessage = HttpClientHelper.getResponseMessage(response, ResponseMessage.class);
        Assert.assertTrue(responseMessage.getMessage().contains("User group successfully removed."));

        brokerRestApiClient.deleteQueue(queueName);
    }

    @DataProvider(name = "queueDurability")
    public static Object[] getQueueDurability() {
        return new Object[]{true, false};
    }

    @DataProvider(name = "userGroupRequest")
    public static Object[][] userGroupRequest() {
        return new Object[][]{
                {true, "get"},
                {true, "delete"},
                {true, "consume"},
                {true, "grantPermission"},
                {false, "get"},
                {false, "delete"},
                {false, "consume"},
                {false, "grantPermission"}
        };
    }
}
