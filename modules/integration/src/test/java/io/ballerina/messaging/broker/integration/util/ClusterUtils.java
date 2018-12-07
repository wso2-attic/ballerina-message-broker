/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.messaging.broker.integration.util;

import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ClusterUtils {

    private static final String queueName = "testQueue";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterUtils.class);

    /**
     * Method to kill broker node
     *
     * @param brokernode brokernode to be killed
     */
    public static void killBrokerNode(String brokernode) throws IOException {
        Runtime.getRuntime().exec("docker kill " + brokernode);
    }

    /**
     * Method to start broker node
     *
     * @param brokernode brokernode to be started
     */
    public static void startBrokerNode(String brokernode) throws IOException {
        Runtime.getRuntime().exec("docker start " + brokernode);
    }

    /**
     * Method to shutdown broker node
     *
     * @param brokernode brokernodeto be shutdown
     */
    public static void shutdownBrokerNode(String brokernode) throws IOException {
        Runtime.getRuntime().exec("docker stop " + brokernode);
    }

    /**
     * Method to restart broker node
     *
     * @param brokernode brokernode to be restarted
     * @param username   username
     * @param password   password
     * @param hostname   hostname
     * @param port       port
     */
    public static void restartBrokerNode(String brokernode, String username, String password, String hostname,
                                         String port)
            throws IOException {
        Runtime.getRuntime().exec("docker stop " + brokernode);
        Awaitility.await().atMost(120, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> isConnectionAvailable(username, password, hostname, port));
        Runtime.getRuntime().exec("docker start " + brokernode);
    }

    /**
     * Method to check port availability
     *
     * @param hostName name of the host
     * @param port     port
     */
    public static boolean isPortAvailable(String hostName, int port) {
        Socket socket = null;
        try {
            socket = new Socket(hostName, port);
            LOGGER.info("Port " + port + " is not available");
            return false;
        } catch (IOException e) {
            LOGGER.info("Port " + port + " is available");
            return true;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOGGER.error("Error in closing socket", e);
                }
            }
        }
    }

    /**
     * Method to check JMS connection
     *
     * @param userName username
     * @param password password
     * @param hostname hostname
     * @param port     port
     * */
    private static boolean isConnectionAvailable(String userName, String password, String hostname, String port) {
        try {
            InitialContext ctx = ClientHelper.getInitialContextBuilder(userName, password, hostname, port)
                    .withQueue(queueName).build();
            ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup(ClientHelper.CONNECTION_FACTORY);
            Connection connection = connectionFactory.createConnection();
            connection.start();
            LOGGER.info("Connection available...");
            return true;
        } catch (NamingException | JMSException e) {
            LOGGER.info("Connection not available...", e);
            return false;
        }
    }
}
