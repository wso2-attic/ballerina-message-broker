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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class containing tests of 'create' command.
 */
public class CreateCmdTest extends CliTestParent {

    @Test(groups = "StreamReading",
          description = "test command 'create'")
    public void testCreate() {
        String[] cmd = { "create" };
        String expectedLog = "a command is expected after 'create'";
        String errorMessage = "error when executing 'create' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'create --help'")
    public void testCreateHelp() {
        String[] cmd = { "create", "--help" };
        String expectedLog = "Create a resource in MB";
        String errorMessage = "error when executing 'create --help' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }
}
