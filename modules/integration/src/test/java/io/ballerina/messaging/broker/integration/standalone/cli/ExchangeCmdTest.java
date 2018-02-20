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
 * Test class containing tests of 'exchange' command.
 */
public class ExchangeCmdTest extends CliTestParent {

    @Test(groups = "StreamReading",
          description = "test command 'list exchange'")
    public void testListExchange() {
        String[] cmd = { "list", "exchange" };
        String expectedLog = "amq.topic";
        String errorMessage = "error when executing 'list exchange' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }

    @Test(dependsOnMethods = "testListExchange",
          groups = "StreamReading",
          description = "test command 'list exchange sampleEx1'")
    public void testCreateExchange() {
        String[] cmd = { "create", "exchange", "sampleEx1" };
        String[] checkCmd = { "list", "exchange", "sampleEx1" };
        String expectedLog = "sampleEx1";
        String errorMessage = "error when executing 'create exchange <EX_NAME>' command";

        Main.main(cmd);
        Main.main(checkCmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }

    @Test(dependsOnMethods = "testCreateExchange",
          groups = "StreamReading",
          description = "test command 'list exchange sampleEx1'")
    public void testDeleteExchange() {
        String[] cmd = { "delete", "exchange", "sampleEx1" };
        String[] checkCmd = { "list", "exchange" };
        String expectedLog = "sampleEx1";
        String errorMessage = "error when executing 'delete exchange <EX_NAME>' command";

        Main.main(cmd);
        Main.main(checkCmd);

        Assert.assertTrue(!PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }

    @Test(dependsOnMethods = "testListExchange",
          groups = "StreamReading",
          description = "test command 'list exchange sampleEx2 -d -t topic'")
    public void testCreateExchangeWithFlags() {
        String[] cmd = { "create", "exchange", "sampleEx2", "-d", "-t", "topic" };
        String[] checkCmd = { "list", "exchange", "sampleEx2" };
        String expectedLog = "sampleEx2";
        String errorMessage = "error when executing 'create exchange <EX_NAME>' command";

        Main.main(cmd);
        Main.main(checkCmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'list exchange --help'")
    public void testListExchangeHelp() {
        String[] cmd = { "list", "exchange", "--help" };
        String expectedLog = "List exchange(s) in MB";
        String errorMessage = "error when executing 'list exchange --help' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'delete exchange --help'")
    public void testCreateExchangeHelp() {
        String[] cmd = { "create", "exchange", "--help" };
        String expectedLog = "Create an exchange in MB";
        String errorMessage = "error when executing 'create exchange --help' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'delete exchange --help'")
    public void testDeleteExchangeHelp() {
        String[] cmd = { "delete", "exchange", "--help" };
        String expectedLog = "Delete an exchange in MB";
        String errorMessage = "error when executing 'delete exchange --help' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readStream().contains(expectedLog), errorMessage);
    }
}
