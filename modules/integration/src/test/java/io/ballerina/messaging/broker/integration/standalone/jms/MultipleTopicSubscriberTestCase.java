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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleTopicSubscriberTestCase.class);

    @Parameters({"broker-port", "admin-username", "admin-password", "broker-hostname"})
    @Test
    public void testMultipleTopicSubscribersOnSameSession(String port,
                                                          String adminUsername,
                                                          String adminPassword,
                                                          String brokerHostname)
            throws NamingException, JMSException, InterruptedException {
        String queueName = "testMultipleTopicSubscribersOnSameSession";
        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, brokerHostname, port)
                .withTopic(queueName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        TopicSession subscriberSession = connection.createTopicSession(false, TopicSession.CLIENT_ACKNOWLEDGE);
        Topic topic = (Topic) initialContext.lookup(queueName);

        int numberOfConsumers = 3;
        int messagesPerConsumer = 1000;
        int maxNumberOfMessages = numberOfConsumers * messagesPerConsumer;
        LinkedBlockingQueue<MessageResult> receiveQueue =  new LinkedBlockingQueue<>(maxNumberOfMessages);

        TopicSubscriber consumers[] = new TopicSubscriber[numberOfConsumers];
        int messageCount[] = new int[numberOfConsumers];

        for (int consumerIndex = 0; consumerIndex < numberOfConsumers; consumerIndex++) {
            consumers[consumerIndex] = subscriberSession.createSubscriber(topic);
            int finalConsumerIndex = consumerIndex;
            consumers[consumerIndex].setMessageListener(message -> {
                messageCount[finalConsumerIndex]++;
                try {
                    message.acknowledge();
                } catch (JMSException e) {
                    LOGGER.error("Message acknowledging failed.", e);
                }
                receiveQueue.offer(new MessageResult(message, finalConsumerIndex));
            });
        }

        // publish messages with property.
        TopicSession producerSession = connection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        TopicPublisher producer = producerSession.createPublisher(topic);

        TextMessage textMessage;
        String consumerMessage = "testMessage";
        for (int i = 0; i < messagesPerConsumer; i++) {
            textMessage = producerSession.createTextMessage(consumerMessage);
            producer.send(textMessage);
        }


        for (int i = 0; i < maxNumberOfMessages; i++) {
            MessageResult result = receiveQueue.poll(5, TimeUnit.SECONDS);
            if (result == null) {
                StringBuilder countSummary = new StringBuilder();
                for (int consumerIndex = 0; consumerIndex < numberOfConsumers; consumerIndex++) {
                    countSummary.append("Consumer ")
                                .append(consumerIndex)
                                .append(" received ")
                                .append(messageCount[consumerIndex])
                                .append(" messages, ");
                }

                Assert.fail("Messages stopped receiving after " + i + " iterations. " + countSummary.toString());
            } else {
                TextMessage textMessage1 = (TextMessage) result.getMessage();
                Assert.assertEquals(textMessage1.getText(),
                                    consumerMessage,
                                    "Incorrect message received for consumer " + result.getConsumerId());
            }

        }

        for (int consumerIndex = 0; consumerIndex < numberOfConsumers; consumerIndex++) {
            Assert.assertEquals(messageCount[consumerIndex],
                                messagesPerConsumer,
                                "Message " + messageCount[consumerIndex]
                                        + " received for consumer " + consumerIndex + ".");
        }

        producer.close();
        for (int consumerIndex = 0; consumerIndex < numberOfConsumers; consumerIndex++) {
            consumers[consumerIndex].close();
        }

        connection.close();
    }

    private static class MessageResult {

        private final Message message;
        private final int consumerId;

        public MessageResult(Message message, int consumerId) {
            this.message = message;
            this.consumerId = consumerId;
        }

        public Message getMessage() {
            return message;
        }

        public int getConsumerId() {
            return consumerId;
        }
    }
}
