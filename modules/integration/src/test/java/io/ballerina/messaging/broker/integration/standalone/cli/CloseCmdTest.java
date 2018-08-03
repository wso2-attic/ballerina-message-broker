/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.integration.standalone.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.messaging.broker.amqp.rest.model.ConnectionMetadata;
import io.ballerina.messaging.broker.client.Main;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import io.ballerina.messaging.broker.integration.util.HttpClientHelper;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static io.ballerina.messaging.broker.integration.util.TestConstants.CLI_ROOT_COMMAND;

public class CloseCmdTest extends CliTestParent {

    private String apiBasePath;

    private CloseableHttpClient client;

    private ObjectMapper objectMapper;

    private List<Connection> connections;

    private static final String CONNECTIONS_API_PATH = "/transports/amqp/connections";

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseCmdTest.class);

    @Parameters({"broker-hostname", "broker-rest-port"})
    @BeforeClass
    public void setUp(String brokerHost, String port) throws Exception {
        apiBasePath = HttpClientHelper.getRestApiBasePath(brokerHost, port);
        objectMapper = new ObjectMapper();
        connections = new ArrayList<>();
        client = HttpClientHelper.prepareClient();
    }

    @AfterMethod
    public void afterMethod() throws JMSException {
        closeConnections(connections);
    }

    @Test(groups = "StreamReading", description = "test command 'close'")
    public void testCloseCmd() throws Exception {

        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_CLOSE};
        String[] expected = new String[]{"broker-admin: a command is expected after 'close'",
                "Run 'broker-admin --help' for usage."};
        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expected, cmd);
    }

    @Test(groups = "StreamReading", description = "test command 'close --help'")
    public void testCloseCmdWithHelp() throws Exception {

        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_CLOSE, Constants.HELP_FLAG};
        String[] expected = new String[]{"Close a resource in the Broker with parameters",
                "Usage:",
                "broker-admin   broker-admin close [command] [flag]*",
                "Commands:",
                "  channel     Close a channel in the Broker with parameters",
                "  connection  Close a connection in the Broker with parameters",
                "Global Flags:",
                "  --help,-h      ask for help",
                "  --password,-p  Password",
                "  --verbose,-v   enable verbose mode"};
        Main.main(cmd);
        evalStreamContent(PrintStreamHandler.readErrStream(), expected, cmd);
    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test(groups = "StreamReading",
            description = "test command 'close channel --connection [connection-id] --channel [channel-id]'")
    public void testCloseChannel(String username, String password, String hostName, String port) throws Exception {

        int channelCount = 3;
        //Create 3 connections each having 0, 1 and 2 channels respectively
        connections.add(createConnection(channelCount, username, password, hostName, port));

        ConnectionMetadata[] connectionMetadataBeforeClosing = getConnections(username, password);

        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_CLOSE, Constants.CMD_CHANNEL, "1", "--connection",
                connectionMetadataBeforeClosing[0].getId().toString(), Constants.IF_USED_FLAG};
        String expectedLog = "Request accepted for forceful disconnection of channel 1 of connection "
                             + connectionMetadataBeforeClosing[0].getId().toString();

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);

        //Assert connection details after delete
        int expectedChannelCount = channelCount - 1;
        ConnectionMetadata[] connectionMetadataAfterClosing = waitForChannelUpdate(expectedChannelCount, username,
                                                                                   password);
        Assert.assertEquals(connectionMetadataAfterClosing[0].getChannelCount().intValue(), expectedChannelCount,
                            "Incorrect connection count after closing connection.");
    }

    @Test(groups = "StreamReading",
            description = "test command 'close channel --connection [connection-id] --channel [channel-id]' with a "
                          + "channel of a non-existent connection")
    public void testCloseNonExistentChannel() {

        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_CLOSE, Constants.CMD_CHANNEL, "2", "--connection", "1"};
        String expectedLog = "Connection id 1 does not exist.";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading", description = "test command 'close channel --help'")
    public void testCloseChannelHelp() throws Exception {

        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_CLOSE, Constants.CMD_CHANNEL, Constants.HELP_FLAG};
        String[] expectedLog = new String[]{"Close a channel in the Broker with parameters",
                "Usage:",
                "  broker-admin close channel [channel-id] --connection [connection-id]",
                "Flags:",
                "  --connection  Identifier of the connection for which the channel belongs to ",
                "(default: )",
                "Global Flags:",
                "  --help,-h      ask for help",
                "  --password,-p  Password",
                "  --verbose,-v   enable verbose mode"};

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);

    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test(groups = "StreamReading",
            description = "test command 'close connection [connection-id]'")
    public void testCloseConnection(String username, String password, String hostName, String port) throws Exception {

        int channelCount = 3;
        //Create 3 connections each having 0, 1 and 2 channels respectively
        connections.add(createConnection(channelCount, username, password, hostName, port));

        ConnectionMetadata[] connectionMetadataBeforeClosing = getConnections(username, password);

        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_CLOSE, Constants.CMD_CONNECTION,
                connectionMetadataBeforeClosing[0].getId().toString(), Constants.IF_USED_FLAG};
        String expectedLog = "Connection close request submitted successfully";

        Main.main(cmd);
        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);

        //Assert connection details after delete
        int expectedConnectionCount = 0;
        ConnectionMetadata[] connectionMetadataAfterClosing = waitForConnectionUpdate(expectedConnectionCount,
                                                                                      username, password);
        Assert.assertEquals(connectionMetadataAfterClosing.length, expectedConnectionCount,
                            "Incorrect connection count after closing connection.");
    }

    @Test(groups = "StreamReading", description = "test command 'close connection [connection-id]' with a "
                                                  + "non-existent connection")
    public void testCloseNonExistentConnection() {

        //issue command to delete non-existent connection
        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_CLOSE, Constants.CMD_CONNECTION, "1"};
        String expectedLog = "Connection id 1 does not exist.";

        Main.main(cmd);
        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading", description = "test command 'close connection --help'")
    public void testCloseConnectionHelp() throws Exception {

        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_CLOSE, Constants.CMD_CONNECTION, Constants.HELP_FLAG};
        String[] expectedLog = new String[]{"Close a connection in the Broker with parameters",
                "Usage:",
                "  broker-admin close connection [connection-id] [flag]*",
                "Flags:",
                "  --force, -f  If set to true, the connection will be closed from the broker without ",
                "communicating with the amqp client (default: false)",
                "Global Flags:",
                "  --help,-h      ask for help",
                "  --password,-p  Password",
                "  --verbose,-v   enable verbose mode"};

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);

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
            QueueSession session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

            /*
              For each channel, create a number of consumers that is equal to the channel number.
              e.g. if the channel count is 3, channel1 has 1 consumer, channel2 has 2 consumers and channel3 has 3
              consumers
            */
            for (int j = 0; j < i; j++) {
                Queue queue = session.createQueue("queue");
                session.createReceiver(queue);
            }
        }
        return connection;
    }

    /**
     * Closes a list of JMS connections.
     *
     * @param connectionList list of connections to be closed.
     */
    private void closeConnections(List<Connection> connectionList) {
        for (Connection connection : connectionList) {
            try {
                connection.close();
            } catch (JMSException e) {
                LOGGER.warn("Could not close connection " + connection);
            }
        }
    }

    private ConnectionMetadata[] getConnections(String username, String password) throws IOException {
        CloseableHttpResponse response = sendGetConnections(username, password);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK,
                            "Incorrect status code while retrieving connection");
        String body = EntityUtils.toString(response.getEntity());
        return objectMapper.readValue(body, ConnectionMetadata[].class);
    }

    private CloseableHttpResponse sendGetConnections(String username, String password) throws IOException {
        HttpGet httpGet = new HttpGet(apiBasePath + CONNECTIONS_API_PATH);
        ClientHelper.setAuthHeader(httpGet, username, password);
        return client.execute(httpGet);
    }

    private ConnectionMetadata[] waitForConnectionUpdate(int expectedConnectionCount, String userName,
                                                         String password) {
        final ConnectionMetadata[][] connectionMetadataAfterClosing = new ConnectionMetadata[1][1];
        Awaitility.await().atMost(60, TimeUnit.SECONDS)
                  .pollDelay(500, TimeUnit.MILLISECONDS)
                  .until(() -> {
                      connectionMetadataAfterClosing[0] = getConnections(userName, password);
                      return connectionMetadataAfterClosing[0].length == expectedConnectionCount;
                  });
        return connectionMetadataAfterClosing[0];
    }

    private ConnectionMetadata[] waitForChannelUpdate(int expectedChannelCount, String userName,
                                                      String password) {
        final ConnectionMetadata[][] connectionMetadataAfterClosing = new ConnectionMetadata[1][1];
        Awaitility.await().atMost(60, TimeUnit.SECONDS)
                  .pollDelay(500, TimeUnit.MILLISECONDS)
                  .until(() -> {
                      connectionMetadataAfterClosing[0] = getConnections(userName, password);
                      return connectionMetadataAfterClosing[0][0].getChannelCount() == expectedChannelCount;
                  });
        return connectionMetadataAfterClosing[0];
    }
}
