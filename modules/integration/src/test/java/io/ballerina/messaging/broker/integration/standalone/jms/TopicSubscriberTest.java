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

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;

public class TopicSubscriberTest {

    @Parameters({ "broker-port"})
    @Test
    public void testSubscriberPublisher(String port) throws Exception {
        String topicName = "MyTopic1";
        int numberOfMessages = 100;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder("admin", "admin", "localhost", port)
                .withTopic(topicName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        // Initialize subscriber
        TopicSession subscriberSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic subscriberDestination = (Topic) initialContext.lookup(topicName);
        TopicSubscriber subscriber = subscriberSession.createSubscriber(subscriberDestination);

        // publish 100 messages
        TopicSession producerSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicPublisher producer = producerSession.createPublisher(subscriberDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            producer.publish(producerSession.createTextMessage("Test message " + i));
        }

        producerSession.close();

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = subscriber.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }

        connection.close();
    }
}
