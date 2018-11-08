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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import javax.jms.ConnectionFactory;

public class ClusterUtils {

    private static final String GET_PID = "lsof -ti tcp:";
    private static final String KILL_PID = "kill -9 ";
    private static final String SHUTDOWN = "kill -s TERM ";
    private static ConnectionFactory connFactory;

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterUtils.class);

    /**
     * Method to kill broker node
     *
     * @param port port of the broker node to be killed
     */
    public static void killBrokerNode(String port) throws IOException {
        Process process = Runtime.getRuntime().exec(GET_PID + port);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String pid = stdInput.readLine();
        Runtime.getRuntime().exec(KILL_PID + pid);
    }

    /**
     * Method to start broker node
     *
     * @param brokerHome path to the broker home to be started
     */
    public static void startBrokerNode(String brokerHome) throws IOException {
        Runtime.getRuntime().exec("sh " + brokerHome + "/bin/broker");
    }

    /**
     * Method to shutdown broker node
     *
     * @param port port of the broker home to be shutdown
     */
    public static void shutdownBrokerNode(String port) throws IOException {
        Process process = Runtime.getRuntime().exec(GET_PID + port);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String pid = stdInput.readLine();
        Runtime.getRuntime().exec(SHUTDOWN + pid);
    }

    /**
     * Method to restart broker node
     *
     * @param brokerHome path to the broker home to be restarted
     * @param portOne    port of the broker home to be restarted
     */
    public static void restartBrokerNode(String brokerHome, String portOne, String hostnameTwo, String portTwo)
            throws IOException {
        shutdownBrokerNode(portOne);
        Awaitility.await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> !ClusterUtils.isPortAvailable(hostnameTwo, Integer.parseInt(portTwo)));
        startBrokerNode(brokerHome);
    }

    /**
     * Method for check port availability
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
}
