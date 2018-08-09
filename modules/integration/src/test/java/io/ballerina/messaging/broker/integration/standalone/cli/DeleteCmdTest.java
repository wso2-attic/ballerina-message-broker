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
package io.ballerina.messaging.broker.integration.standalone.cli;

import io.ballerina.messaging.broker.client.Main;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.integration.util.ClientHelper;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.util.Objects;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;


import static io.ballerina.messaging.broker.integration.util.TestConstants.CLI_ROOT_COMMAND;

/**
 * Test class containing tests of 'delete' command.
 */
public class DeleteCmdTest extends CliTestParent {

    @Test(groups = "StreamReading",
          description = "test command 'delete'")
    public void testDelete() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_DELETE };
        String expectedLog = "a command is expected after 'delete'";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
          description = "test command 'delete --help'")
    public void testDeleteHelp() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_DELETE, "--help" };
        String expectedLog = "Delete resources in the Broker";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Parameters({"admin-username", "admin-password", "broker-hostname", "broker-port"})
    @Test(groups = "StreamReading",
            description = "test command 'delete messages --queue [queueName]'")
    public void testDeleteMessages(String username, String password, String hostName, String port) throws Exception {

        String queueName = "cliDeleteMessagesQueue";
        int numberOfMessages = 1000;
        publishMessagesToQueue(username, password, hostName, port, queueName, numberOfMessages);

        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_DELETE, Constants.CMD_MESSAGES, Constants.QUEUE_FLAG,
                queueName};
        String expectedLog = "Messages deleted successfully. Number of messages deleted=" + numberOfMessages;
        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
            description = "test command 'delete messages --queue [queueName]' for non-existent queue")
    public void testDeleteMessagesOfNonExistentQueue() throws Exception {
        String queueName = "cliDeleteMessagesNonExistentQueue";
        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_DELETE, Constants.CMD_MESSAGES, Constants.QUEUE_FLAG,
                queueName};
        String expectedLog = "Queue " + queueName + " doesn't exist.";
        Main.main(cmd);
        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
            description = "test command 'delete messages --queue [queueName]'")
    public void testDeleteMessagesHelp() throws Exception {

        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_DELETE, Constants.CMD_MESSAGES, Constants.HELP_FLAG};
        String[] expectedLog = new String[]{"Delete messages in a queue in the Broker",
                "Usage:",
                "  broker-admin delete messages [flag]*",
                "Flags:",
                "  --queue, -q  name of the queue (default: null)",
                "Global Flags:",
                "  --help,-h      ask for help",
                "  --password,-p  Password",
                "  --verbose,-v   enable verbose mode"};

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    private void publishMessagesToQueue(String username, String password, String hostName, String port,
                                        String queueName, int numberOfMessages) throws NamingException, JMSException {
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(username, password, hostName, port)
                .withQueue(queueName)
                .build();
        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = null;
        Session producerSession = null;
        MessageProducer producer = null;
        try {

            connection = connectionFactory.createConnection();

            connection.start();

            // publish 100 messages
            producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = producerSession.createQueue(queueName);
            producer = producerSession.createProducer(queue);

            for (int i = 0; i < numberOfMessages; i++) {
                producer.send(producerSession.createTextMessage("Test message " + i));
            }
        } finally {
            if (Objects.nonNull(connection)) {
                if (Objects.nonNull(producerSession)) {
                    if (Objects.nonNull(producer)) {

                        producer.close();
                    }
                    producerSession.close();
                }
                connection.close();
            }
        }
    }
}
