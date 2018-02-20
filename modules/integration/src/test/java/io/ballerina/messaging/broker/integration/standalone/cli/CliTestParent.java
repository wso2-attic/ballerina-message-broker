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
import io.ballerina.messaging.broker.integration.util.TestConstants;
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
        PrintStreamHandler.resetStream();
        // clear commands map
        Main.clearCommandsMap();
    }
}
