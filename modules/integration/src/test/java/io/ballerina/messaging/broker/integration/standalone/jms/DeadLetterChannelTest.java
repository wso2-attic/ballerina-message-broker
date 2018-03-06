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

package io.ballerina.messaging.broker.integration.standalone.jms;

import io.ballerina.messaging.broker.integration.util.ClientHelper;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.InitialContext;

public class DeadLetterChannelTest {

    @Parameters({ "broker-hostname", "broker-port", "admin-username", "admin-password" })
    @Test
    public void testDlcWithBasicRecover(String brokerHostname,
                                        String port,
                                        String adminUsername,
                                        String adminPassword) throws Exception {
        String queueName = "testDlcWithBasicRecover";
        String dlcQueueName = "amq.dlq";
        InitialContext initialContextForQueue = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withQueue(queueName)
                .withQueue(dlcQueueName)
                .build();

        ConnectionFactory connectionFactory
                = (ConnectionFactory) initialContextForQueue.lookup(ClientHelper.CONNECTION_FACTORY);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // publish message
        Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = producerSession.createQueue(queueName);
        MessageProducer producer = producerSession.createProducer(queue);

        producer.send(producerSession.createTextMessage("Message for DLC test"));
        producerSession.close();

        // Consume published messages
        Session subscriberSession = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Destination subscriberDestination = (Destination) initialContextForQueue.lookup(queueName);
        MessageConsumer consumer = subscriberSession.createConsumer(subscriberDestination);

        for (int iteration = 0; iteration < 6; iteration++) {
            Message message = consumer.receive(5000);
            Assert.assertNotNull(message, "Message was not received");
            subscriberSession.recover();
        }

        Connection dlcConsumerConnection = connectionFactory.createConnection();
        dlcConsumerConnection.start();
        Session dlcConsumerSession = dlcConsumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer dlcConsumer = dlcConsumerSession.createConsumer((Destination) initialContextForQueue.lookup(
                dlcQueueName));

        Message dlcMessage = dlcConsumer.receive(5000);
        Assert.assertNotNull(dlcMessage, "Dead lettered message was not received" + dlcMessage);
        String originQueue = dlcMessage.getStringProperty("x-origin-queue");
        Assert.assertEquals(originQueue, queueName, "Origin queue name did not match" + dlcMessage);
        String originExchange = dlcMessage.getStringProperty("x-origin-exchange");
        Assert.assertEquals(originExchange, "amq.direct", "Origin exchange name did not match" + dlcMessage);
        String originRoutingKey = dlcMessage.getStringProperty("x-origin-routing-key");
        Assert.assertEquals(originRoutingKey, queueName, "Origin routing key did not match" + dlcMessage);

        dlcConsumerConnection.close();
        connection.close();
    }
}
