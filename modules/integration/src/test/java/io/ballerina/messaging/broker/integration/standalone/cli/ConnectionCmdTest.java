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

import io.ballerina.messaging.broker.client.Main;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static io.ballerina.messaging.broker.integration.util.TestConstants.CLI_ROOT_COMMAND;

public class ConnectionCmdTest extends CliTestParent {

    @Parameters({"broker-hostname", "broker-rest-port"})
    @BeforeClass
    public void setUp(String brokerHost, String port) throws Exception {
    }

    @BeforeMethod
    public void setup() {
    }

    @AfterClass
    public void tearDown() throws Exception {
    }

    @AfterMethod
    public void afterMethod() {
    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test(groups = "StreamReading",
            description = "test command 'close channel --connection [connection-id] --channel [channel-id]'")
    public void testCloseChannel(String username, String password, String hostName, String port) throws Exception {

        int channelCount = 3;
        //Create 3 connections each having 0, 1 and 2 channels respectively
        List<Connection> connections = new ArrayList<>();
        connections.add(createConnection(channelCount, username, password, hostName, port));


        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_CLOSE, Constants.CMD_CHANNEL, "--connection", "1",
                "--channel", "1"};
        String[] expectedLog = new String[]{"Forceful disconnection", "channel", "accepted"};

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
        closeConnections(connections);
    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test(groups = "StreamReading",
            description = "test command 'close connection [connection-id]'")
    public void testCloseConnection(String username, String password, String hostName, String port) throws Exception {

        int channelCount = 3;
        //Create 3 connections each having 0, 1 and 2 channels respectively
        List<Connection> connections = new ArrayList<>();
        connections.add(createConnection(channelCount, username, password, hostName, port));


        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_CLOSE, Constants.CMD_CONNECTION, "1"};
        String[] expectedLog = new String[]{"Forceful disconnection of connection", "accepted"};

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
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
     * @throws JMSException if an error occurs while closing connection
     */
    private void closeConnections(List<Connection> connectionList) throws JMSException {
        for (Connection connection : connectionList) {
            connection.close();
        }
    }

    /**
     * Common method to be used in evaluating the stream content for test cases.
     *
     * @param streamContent Content of the stream.
     * @param expectedStrings      expected message to be included in the stream.
     * @param command       executed command.
     */
    void evalStreamContent(String streamContent, String[] expectedStrings, String[] command) {

        // build onFailure message
        StringBuilder sb = new StringBuilder();
        sb.append("error when executing command: " + String.join(" ", command) + "\n");
        sb.append("expected: \n" + expectedStrings + "\n");
        sb.append("stream content: \n" + streamContent);

        for (String expected : expectedStrings) {
            Assert.assertTrue(streamContent.contains(expected), sb.toString());
        }
    }
}
