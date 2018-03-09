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

import io.ballerina.messaging.broker.integration.util.TestConstants;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Parent class for the CLI test classes.
 */
public class CliTestParent {

    @BeforeMethod
    public void setConfigPath() {
        // set the config file path
        System.setProperty(TestConstants.CLI_CONFIG_SYSTEM_PROPERTY, TestConstants.CLI_CONFIG_LOCATION);
    }

    @AfterMethod
    public void resetStream() {
        // reset the print stream after each test
        PrintStreamHandler.resetStreams();
        System.clearProperty(TestConstants.CLI_CONFIG_SYSTEM_PROPERTY);
    }

    /**
     * Common method to be used in evaluating the stream content for test cases.
     *
     * @param streamContent Content of the stream.
     * @param expected expected message to be included in the stream.
     * @param command executed command.
     */
    void evalStreamContent(String streamContent, String expected, String[] command) {

        // build onFailure message
        StringBuilder sb = new StringBuilder();
        sb.append("error when executing command: " + String.join(" ", command) + "\n");
        sb.append("expected: \n" + expected + "\n");
        sb.append("stream content: \n" + streamContent);

        Assert.assertTrue(streamContent.contains(expected), sb.toString());
    }
}
