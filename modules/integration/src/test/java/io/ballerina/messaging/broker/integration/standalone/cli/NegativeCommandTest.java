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
 * Test class containing tests of invalid commands.
 */
public class NegativeCommandTest extends CliTestParent {

    @Test(groups = "StreamReading",
          description = "test command 'list abc'")
    public void testListInvalidResource() {
        String[] cmd = { "list", "abc" };
        String expectedLog = "unknown command 'abc'";
        String errorMessage = "error message on, list unknown resource type is invalid";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'create abc'")
    public void testCreateInvalidResource() {
        String[] cmd = { "create", "abc" };
        String expectedLog = "unknown command 'abc'";
        String errorMessage = "error message on, create unknown resource type is invalid";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'delete abc'")
    public void testDeleteInvalidResource() {
        String[] cmd = { "delete", "abc" };
        String expectedLog = "unknown command 'abc'";
        String errorMessage = "error message on, delete unknown resource is type invalid";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'list exchange ex1 ex2'")
    public void testMultipleMainValues() {
        String[] cmd = { "list", "exchange", "ex1", "ex2" };
        String expectedLog = "Only one main parameter allowed but found several: \"ex1\" and \"ex2\"";
        String errorMessage = "error message on, multiple main parameters provided";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }
}
