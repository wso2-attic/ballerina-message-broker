/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.messaging.integration;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.wso2.messaging.integration.util.ClientHelper;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MultipleTopicSubscriberTestCase {

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testMultipleTopicSubscribersOnSameSession(String port,
                                                          String adminUsername,
                                                          String adminPassword,
                                                          String brokerHostname) throws NamingException, JMSException {
        String queueName = "testMultipleSelectorConsumersForTopic";
        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(queueName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        TopicSession subscriberSession = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        Topic topic = (Topic) initialContext.lookup(queueName);

        // Subscribe with selectors.
        TopicSubscriber consumer1 = subscriberSession.createSubscriber(topic);
        TopicSubscriber consumer2 = subscriberSession.createSubscriber(topic);
        TopicSubscriber consumer3 = subscriberSession.createSubscriber(topic);

        // publish messages with property.
        TopicSession producerSession = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        TopicPublisher producer = producerSession.createPublisher(topic);

        TextMessage textMessage;
        String consumerMessage = "testMessage";
        for (int i = 0; i < 1000; i++) {
            textMessage = producerSession.createTextMessage(consumerMessage);
            producer.send(textMessage);
        }

        // Receive and test messages.
        Message message;
        for (int i = 0; i < 1000; i++) {
            message = consumer1.receive(5000);
            Assert.assertNotNull(message, "Message " + i + " not received for consumer 1.");
            textMessage = (TextMessage) message;
            Assert.assertEquals(textMessage.getText(),
                                consumerMessage,
                                "Incorrect message " + i + " received for consumer 1.");

            message = consumer2.receive(5000);
            Assert.assertNotNull(message, "Message " + i + " not received for consumer 2.");
            textMessage = (TextMessage) message;
            Assert.assertEquals(textMessage.getText(),
                                consumerMessage,
                                "Incorrect message " + i + " received for consumer 2.");

            message = consumer3.receive(5000);
            Assert.assertNotNull(message, "Message " + i + " not received for consumer 3.");
            textMessage = (TextMessage) message;
            Assert.assertEquals(textMessage.getText(),
                                consumerMessage,
                                "Incorrect message " + i + " received for consumer 3.");
        }

        producer.close();
        consumer1.close();
        consumer2.close();
        consumer3.close();

        connection.close();
    }
}
