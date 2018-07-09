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
import java.util.ArrayList;
import java.util.List;
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

        HttpGet httpGet = new HttpGet(apiBasePath + CONNECTIONS_API_PATH);
        ClientHelper.setAuthHeader(httpGet, username, password);
        CloseableHttpResponse response = client.execute(httpGet);

        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK, "Incorrect status code");

        String body = EntityUtils.toString(response.getEntity());
        ConnectionMetadata[] connectionMetadata = objectMapper.readValue(body, ConnectionMetadata[].class);
        Assert.assertEquals(connectionMetadata.length, connectionCount, 
                            "Incorrect connection count.");

        //Assert populated data inside the connection
        for (int i = 0; i < connectionCount; i++) {
            Assert.assertNotNull(connectionMetadata[i].getRemoteAddress(), "Remote address cannot be null.");
            Assert.assertNotNull(connectionMetadata[i].getConnectedTime(), "Connected time cannot be null.");
            Assert.assertEquals(connectionMetadata[i].getChannelCount().intValue(), i, "Established channel count is"
                                                                                       + " incorrect");
        }
        closeConnections(connections);
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
}
