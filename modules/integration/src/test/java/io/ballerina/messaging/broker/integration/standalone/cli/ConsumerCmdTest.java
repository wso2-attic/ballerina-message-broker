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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Objects;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static io.ballerina.messaging.broker.integration.util.TestConstants.CLI_ROOT_COMMAND;

/**
 * Test class containing tests of 'consumer' command.
 */
public class ConsumerCmdTest extends CliTestParent {

    private static final String QUEUE_NAME = "MyCliQueue";

    private static final int NO_OF_CONSUMERS = 5;

    // JMS resources
    private Connection connection;
    private Session session;
    private MessageConsumer[] messageConsumers = new MessageConsumer[NO_OF_CONSUMERS];

    @Test(groups = "StreamReading",
          description = "test command 'list consumer <queue_name>'")
    public void testListExchange() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_CONSUMER, QUEUE_NAME };
        // this a name of a column. This will only be printed if there are 1 or more consumers
        String expectedLog = "flowEnabled";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
    }

    @Parameters({ "broker-port" })
    @BeforeClass
    public void connectConsumers(String port) throws NamingException, JMSException {

        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder("admin", "admin", "localhost", port).withQueue(QUEUE_NAME).build();

        ConnectionFactory connectionFactory = (ConnectionFactory) initialContextForQueue
                .lookup(ClientHelper.CONNECTION_FACTORY);
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(QUEUE_NAME);

        for (int i = 0; i < NO_OF_CONSUMERS; i++) {
            messageConsumers[i] = session.createConsumer(subscriberDestination);
        }

    }

    @AfterClass
    private void destroyJmsResources() throws JMSException {
        for (MessageConsumer messageConsumer : messageConsumers) {
            if (Objects.nonNull(messageConsumer)) {
                messageConsumer.close();
            }
        }
        if (Objects.nonNull(session)) {
            session.close();
        }
        if (Objects.nonNull(connection)) {
            connection.close();
        }
    }

}
