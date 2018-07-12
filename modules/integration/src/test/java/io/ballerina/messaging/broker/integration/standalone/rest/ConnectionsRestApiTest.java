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
import io.ballerina.messaging.broker.amqp.rest.model.ConnectionMetadata;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import io.ballerina.messaging.broker.integration.util.HttpClientHelper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.awaitility.Awaitility;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Test connections admin service api.
 */
public class ConnectionsRestApiTest {

    private String apiBasePath;

    private CloseableHttpClient client;

    private ObjectMapper objectMapper;

    private static final String CONNECTIONS_API_PATH = "/transports/amqp/connections";

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
    public void testGetConnections(String username, String password, String hostName, String port) throws Exception {

        int connectionCount = 3;
        //Create 3 connections each having 0, 1 and 2 channels respectively
        List<Connection> connections = new ArrayList<>(connectionCount);
        for (int i = 0; i < connectionCount; i++) {
            connections.add(createConnection(i, username, password, hostName, port));
        }

        ConnectionMetadata[] connectionMetadata = getConnections(username, password);
        Assert.assertEquals(connectionMetadata.length, connectionCount, "Incorrect connection count.");

        //Assert populated data inside the connection
        for (int i = 0; i < connectionCount; i++) {
            Assert.assertNotNull(connectionMetadata[i].getRemoteAddress(), "Remote address cannot be null.");
            Assert.assertNotNull(connectionMetadata[i].getConnectedTime(), "Connected time cannot be null.");
            Assert.assertEquals(connectionMetadata[i].getChannelCount().intValue(), i, "Established channel count is"
                                                                                       + " incorrect.");
        }
        closeConnections(connections);
    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test
    public void testGetConnectionsWithInvalidPassword(String username, String password, String hostName, String port)
            throws Exception {
        Connection connection = createConnection(1, username, password, hostName, port);
        CloseableHttpResponse response = sendGet(username, "invalidPassword");

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_UNAUTHORIZED,
                "Incorrect status code while retrieving connection");
        connection.close();
    }

    @Parameters({"admin-username", "admin-password", "test-username", "test-password", "broker-hostname",
                 "broker-port"})
    @Test
    public void testGetConnectionsWithUnauthorizedUser(String adminUsername, String adminPassword, String username,
                                                       String password, String hostName, String port) throws Exception {
        Connection connection = createConnection(1, adminUsername, adminPassword, hostName, port);
        CloseableHttpResponse response = sendGet(username, password);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN,
                            "Incorrect status code while retrieving connection");
        connection.close();
    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test
    public void testCloseConnections(String username, String password, String hostName, String port) throws Exception {

        int connectionCount = 3;
        //Create 3 connections each having 0, 1 and 2 channels respectively
        List<Connection> connections = new ArrayList<>(connectionCount);
        for (int i = 0; i < connectionCount; i++) {
            connections.add(createConnection(i, username, password, hostName, port));
        }

        ConnectionMetadata[] connectionMetadataBeforeClosing = getConnections(username, password);
        Assert.assertEquals(connectionMetadataBeforeClosing.length, connectionCount,
                "Incorrect connection count before closing connection.");

        //Send delete request
        HttpDelete httpDelete = new HttpDelete(apiBasePath + CONNECTIONS_API_PATH + "/" +
                                               connectionMetadataBeforeClosing[1].getId());
        ClientHelper.setAuthHeader(httpDelete, username, password);
        CloseableHttpResponse connectionCloseResponse = client.execute(httpDelete);
        Assert.assertEquals(connectionCloseResponse.getStatusLine().getStatusCode(), HttpStatus.SC_ACCEPTED,
                "Incorrect status code while closing connections");

        //Assert connection details after delete
        int expectedConnectionCount = connectionCount - 1;
        ConnectionMetadata[] connectionMetadataAfterClosing = waitForConnectionUpdate(expectedConnectionCount,
                                                                                      username, password);
        Assert.assertEquals(connectionMetadataAfterClosing.length, expectedConnectionCount,
                            "Incorrect connection count after closing connection.");

        //Remove closed connection
        connections.remove(1);

        Assert.assertEquals(connectionMetadataAfterClosing[0].getId(), connectionMetadataBeforeClosing[0].getId(),
                            "Connection " + connectionMetadataBeforeClosing[0].getId() + " does not exist.");
        Assert.assertEquals(connectionMetadataAfterClosing[1].getId(), connectionMetadataBeforeClosing[2].getId(),
                            "Connection " + connectionMetadataBeforeClosing[2].getId() + " does not exist.");

        closeConnections(connections);
    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test
    public void testCloseNonExistentConnection(String username, String password, String hostName, String port)
            throws Exception {

        Connection connection = createConnection(2, username, password, hostName, port);
        ConnectionMetadata[] connectionMetadataBeforeClosing = getConnections(username, password);
        Assert.assertEquals(connectionMetadataBeforeClosing.length, 1,
                            "Incorrect connection count before closing connection.");

        //Send delete request with invalid connection identifier
        HttpDelete httpDelete = new HttpDelete(apiBasePath + CONNECTIONS_API_PATH + "/"
                                               + connectionMetadataBeforeClosing[0].getId() + 1);
        ClientHelper.setAuthHeader(httpDelete, username, password);
        CloseableHttpResponse connectionCloseResponse = client.execute(httpDelete);
        Assert.assertEquals(connectionCloseResponse.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND,
                            "Incorrect status code while closing connections");
        connection.close();
    }

    @Parameters({"admin-username", "admin-password", "test-username", "test-password", "broker-hostname",
                 "broker-port"})
    @Test
    public void testCloseConnectionWithUnAuthorizedUSer(String adminUserName, String adminPassword, String
            testUsername, String testPassword, String hostName, String port) throws Exception {

        Connection connection = createConnection(2, adminUserName, adminPassword, hostName, port);
        ConnectionMetadata[] connectionMetadataBeforeClosing = getConnections(adminUserName, adminPassword);
        Assert.assertEquals(connectionMetadataBeforeClosing.length, 1,
                            "Incorrect connection count before closing connection.");

        //Send delete request with invalid connection identifier
        HttpDelete httpDelete = new HttpDelete(apiBasePath + CONNECTIONS_API_PATH + "/"
                                               + connectionMetadataBeforeClosing[0].getId());
        ClientHelper.setAuthHeader(httpDelete, testUsername, testPassword);
        CloseableHttpResponse connectionCloseResponse = client.execute(httpDelete);
        Assert.assertEquals(connectionCloseResponse.getStatusLine().getStatusCode(), HttpStatus.SC_FORBIDDEN,
                            "Incorrect status code while closing connections");
        connection.close();
    }

    /**
     * Creates a AMQP connection with the number of channels specified, registered on top of it.
     *
     * @param numberOfChannels number of channels to be created using the connection
     * @param userName         admin user
     * @param password         admin password
     * @param hostName         localhost
     * @param port             the AMQP port for which the broker listens to
     * @return the created JMS connection
     * @throws NamingException if an error occurs while creating the context/connection factory using given properties.
     * @throws JMSException    if an error occurs while creating/starting the connection/session
     */
    private Connection createConnection(int numberOfChannels, String userName, String password, String hostName,
            String port) throws NamingException, JMSException {

        InitialContext initialContext
                = ClientHelper.getInitialContextBuilder(userName, password, hostName, port).build();

        QueueConnectionFactory connectionFactory
                = (QueueConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        QueueConnection connection = connectionFactory.createQueueConnection();
        connection.start();
        for (int i = 0; i < numberOfChannels; i++) {
            connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
        }
        return connection;
    }

    /**
     * Closes a list of JMS connections.
     *
     * @param connectionList list of connections to be closed.
     * @throws JMSException if an error occurs while closing connection
     */
    private void closeConnections(List<Connection> connectionList) throws JMSException {
        for (Connection connection : connectionList) {
            connection.close();
        }
    }

    private ConnectionMetadata[] getConnections(String username, String password) throws IOException {
        CloseableHttpResponse response = sendGet(username, password);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                            "Incorrect status code while retrieving connection");
        String body = EntityUtils.toString(response.getEntity());
        return objectMapper.readValue(body, ConnectionMetadata[].class);
    }

    private CloseableHttpResponse sendGet(String username, String password) throws IOException {
        HttpGet httpGet = new HttpGet(apiBasePath + CONNECTIONS_API_PATH);
        ClientHelper.setAuthHeader(httpGet, username, password);
        return client.execute(httpGet);
    }

    private ConnectionMetadata[] waitForConnectionUpdate(int expectedConnectionCount, String userName,
                                                         String password) throws Exception {
        final ConnectionMetadata[][] connectionMetadataAfterClosing = new ConnectionMetadata[1][1];
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                  .pollDelay(500, TimeUnit.MILLISECONDS)
                  .until(() -> {
                      connectionMetadataAfterClosing[0] = getConnections(userName, password);
                      return connectionMetadataAfterClosing[0].length == expectedConnectionCount;
                  });
        return connectionMetadataAfterClosing[0];
    }

}
