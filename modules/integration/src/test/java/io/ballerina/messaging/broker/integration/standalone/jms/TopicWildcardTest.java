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
import org.testng.annotations.BeforeClass;
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

/**
 * Test class for topic wildcards.
 */
public class TopicWildcardTest {

    private String port;

    @Parameters({ "broker-port"})
    @BeforeClass
    public void setup(String port) throws Exception {
        this.port = port;
    }

    @Test
    public void testSubscriberPublisherForHierarchicalTopic() throws Exception {
        assertNotNullWithPublishSubscribeForTopics("Sports.cricket.100s", "Sports.cricket.100s");

        assertNullWithPublishSubscribeForTopics("sports.cricket", "sports");
        assertNullWithPublishSubscribeForTopics("sports", "sports.cricket");
        assertNullWithPublishSubscribeForTopics("cricket", "sports.cricket");
    }

    @Test
    public void testSubscriberPublisherWithSingleWordWildcard() throws Exception {
        assertNotNullWithPublishSubscribeForTopics("sports.cricket", "sports.*");
        assertNotNullWithPublishSubscribeForTopics("sports.cricket", "*.cricket");

        assertNullWithPublishSubscribeForTopics("sports", "sports.*");
        assertNullWithPublishSubscribeForTopics("sports.cricket.batsmen", "sports.*");
        assertNullWithPublishSubscribeForTopics("sports.cricket.batsmen", "*.batsmen");
    }

    @Test
    public void testSubscriberPublisherWithMultipleWordWildcard() throws Exception {
        assertNotNullWithPublishSubscribeForTopics("sports", "sports.#");
        assertNotNullWithPublishSubscribeForTopics("sports.cricket", "sports.#");
        assertNotNullWithPublishSubscribeForTopics("sports.cricket.batsmen.100s", "sports.#");

        assertNullWithPublishSubscribeForTopics("sports.cricket", "cricket.#");
    }

    @Test
    public void testSubscriberPublisherWithMultipleWildcards() throws Exception {
        assertNotNullWithPublishSubscribeForTopics("sports.cricket.batsmen.100s", "*.cricket.#");
        assertNotNullWithPublishSubscribeForTopics("sports.cricket.batsmen", "*.*.batsmen");

        assertNullWithPublishSubscribeForTopics("sports.cricket.batsmen", "*.*.cricket");
        assertNullWithPublishSubscribeForTopics("sports.cricket", "batsmen.cricket.#");
    }

    @Test
    public void testMultipleSubscriberPublisherWithWildcards() throws Exception {
        String publishTopicName = "sports.cricket.batsmen";
        String subscribeTopicOneName = "sports.cricket.*";
        String subscribeTopicTwoName = "sports.#";

        int numberOfMessages = 100;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder("admin", "admin", "localhost", port)
                .withTopic(publishTopicName)
                .withTopic(subscribeTopicOneName)
                .withTopic(subscribeTopicTwoName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        // Initialize subscriber 1
        TopicSession subscriberSession1 = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic subscriberDestination1 = (Topic) initialContext.lookup(subscribeTopicOneName);
        TopicSubscriber subscriber1 = subscriberSession1.createSubscriber(subscriberDestination1);
        // Initialize subscriber 2
        TopicSession subscriberSession2 = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic subscriberDestination2 = (Topic) initialContext.lookup(subscribeTopicTwoName);
        TopicSubscriber subscriber2 = subscriberSession2.createSubscriber(subscriberDestination2);

        TopicSession publisherSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic publisherDestination = (Topic) initialContext.lookup(publishTopicName);
        TopicPublisher publisher = publisherSession.createPublisher(publisherDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            publisher.publish(publisherSession.createTextMessage("Test message " + i));
        }

        publisherSession.close();

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = subscriber1.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }
        for (int i = 0; i < numberOfMessages; i++) {
            Message message = subscriber2.receive(5000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }

        subscriberSession1.close();
        subscriberSession2.close();
        connection.close();
    }

    private void assertNotNullWithPublishSubscribeForTopics(String publishTopicName,
                                                            String subscribeTopicName) throws Exception {

        int numberOfMessages = 100;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder("admin", "admin", "localhost", port)
                .withTopic(publishTopicName)
                .withTopic(subscribeTopicName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        TopicSession subscriberSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic subscriberDestination = (Topic) initialContext.lookup(subscribeTopicName);
        TopicSubscriber subscriber = subscriberSession.createSubscriber(subscriberDestination);

        TopicSession publisherSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic publisherDestination = (Topic) initialContext.lookup(publishTopicName);
        TopicPublisher publisher = publisherSession.createPublisher(publisherDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            publisher.publish(publisherSession.createTextMessage("Test message " + i));
        }

        publisherSession.close();

        for (int i = 0; i < numberOfMessages; i++) {
            Message message = subscriber.receive(1000);
            Assert.assertNotNull(message, "Message #" + i + " was not received");
        }

        subscriberSession.close();
        connection.close();
    }

    private void assertNullWithPublishSubscribeForTopics(String publishTopicName,
                                                         String subscribeTopicName) throws Exception {

        int numberOfMessages = 100;

        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder("admin", "admin", "localhost", port)
                .withTopic(publishTopicName)
                .withTopic(subscribeTopicName)
                .build();

        TopicConnectionFactory connectionFactory
                = (TopicConnectionFactory) initialContext.lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        TopicSession subscriberSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic subscriberDestination = (Topic) initialContext.lookup(subscribeTopicName);
        TopicSubscriber subscriber = subscriberSession.createSubscriber(subscriberDestination);

        TopicSession publisherSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic publisherDestination = (Topic) initialContext.lookup(publishTopicName);
        TopicPublisher publisher = publisherSession.createPublisher(publisherDestination);

        for (int i = 0; i < numberOfMessages; i++) {
            publisher.publish(publisherSession.createTextMessage("Test message " + i));
        }

        publisherSession.close();

        Message message = subscriber.receive(1000);
        Assert.assertNull(message, "A message was received where no message was expected");

        subscriberSession.close();
        connection.close();
    }
}
