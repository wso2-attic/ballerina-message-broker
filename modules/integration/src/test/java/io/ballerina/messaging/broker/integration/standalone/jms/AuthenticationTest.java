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
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;

/**
 * Class for testing client connection authentication
 */
public class AuthenticationTest {

    @Parameters({ "broker-port", "admin-username", "admin-password" })
    @Test(description = "Test user with valid credentials")
    public void testValidClientConnection(String port, String adminUsername, String adminPassword) throws Exception {
        String topicName = "MyTopic1";
        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, adminPassword, "localhost", port).withTopic(topicName).build();
        TopicConnectionFactory connectionFactory = (TopicConnectionFactory) initialContext
                .lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();
        connection.close();
    }

    @Parameters({ "broker-port", "test-username", "test-password" })
    @Test(description = "Test valid user password with special characters")
    public void testPasswordWithSpecialCharacters(String port, String testUsername, String testPassword) throws
            Exception {
        String topicName = "MyTopic1";
        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(testUsername, testPassword, "localhost", port).withTopic(topicName).build();
        TopicConnectionFactory connectionFactory = (TopicConnectionFactory) initialContext
                .lookup(ClientHelper.CONNECTION_FACTORY);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();
        connection.close();
    }

    @Parameters({ "broker-port", "admin-username" })
    @Test(description = "Test user with invalid credentials",
          expectedExceptions = JMSException.class)
    public void testInvalidClientConnection(String port, String adminUsername) throws Exception {
        String topicName = "MyTopic1";
        InitialContext initialContext = ClientHelper
                .getInitialContextBuilder(adminUsername, "invalidPassword", "localhost", port).withTopic(topicName)
                .build();
        TopicConnectionFactory connectionFactory = (TopicConnectionFactory) initialContext
                .lookup(ClientHelper.CONNECTION_FACTORY);
        connectionFactory.createTopicConnection();
    }

}
