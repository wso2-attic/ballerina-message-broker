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

import static io.ballerina.messaging.broker.integration.util.TestConstants.CLI_ROOT_COMMAND;

/**
 * Test class containing tests of 'exchange' command.
 */
public class ExchangeCmdTest extends CliTestParent {

    @Test(groups = "StreamReading",
          description = "test command 'list exchange'")
    public void testListExchange() {
        String[] cmd = { CLI_ROOT_COMMAND, "list", "exchange" };
        String expectedLog = "amq.topic";
        String errorMessage = "error when executing 'list exchange' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readOutStream().contains(expectedLog), errorMessage);
    }

    @Test(dependsOnMethods = "testListExchange",
          groups = "StreamReading",
          description = "test command 'list exchange sampleEx1'")
    public void testCreateExchange() {
        String[] cmd = { CLI_ROOT_COMMAND, "create", "exchange", "sampleEx1" };
        String[] checkCmd = { CLI_ROOT_COMMAND, "list", "exchange", "sampleEx1" };
        String expectedLog = "sampleEx1";
        String errorMessage = "error when executing 'create exchange <EX_NAME>' command";

        Main.main(cmd);
        Main.main(checkCmd);

        Assert.assertTrue(PrintStreamHandler.readOutStream().contains(expectedLog), errorMessage);
    }

    @Test(dependsOnMethods = "testCreateExchange",
          groups = "StreamReading",
          description = "test command 'list exchange sampleEx1'")
    public void testDeleteExchange() {
        String[] cmd = { CLI_ROOT_COMMAND, "delete", "exchange", "sampleEx1" };
        String[] checkCmd = { CLI_ROOT_COMMAND, "list", "exchange" };
        String expectedLog = "sampleEx1";
        String errorMessage = "error when executing 'delete exchange <EX_NAME>' command";

        Main.main(cmd);
        Main.main(checkCmd);

        Assert.assertTrue(!PrintStreamHandler.readOutStream().contains(expectedLog), errorMessage);
    }

    @Test(dependsOnMethods = "testListExchange",
          groups = "StreamReading",
          description = "test command 'list exchange sampleEx2 -d -t topic'")
    public void testCreateExchangeWithFlags() {
        String[] cmd = { CLI_ROOT_COMMAND, "create", "exchange", "sampleEx2", "-d", "-t", "topic" };
        String[] checkCmd = { CLI_ROOT_COMMAND, "list", "exchange", "sampleEx2" };
        String expectedLog = "sampleEx2";
        String errorMessage = "error when executing 'create exchange <EX_NAME>' command";

        Main.main(cmd);
        Main.main(checkCmd);

        Assert.assertTrue(PrintStreamHandler.readOutStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'list exchange --help'")
    public void testListExchangeHelp() {
        String[] cmd = { CLI_ROOT_COMMAND, "list", "exchange", "--help" };
        String expectedLog = "List exchange(s) in the Broker";
        String errorMessage = "error when executing 'list exchange --help' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readErrStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'delete exchange --help'")
    public void testCreateExchangeHelp() {
        String[] cmd = { CLI_ROOT_COMMAND, "create", "exchange", "--help" };
        String expectedLog = "Create an exchange in the Broker with parameters";
        String errorMessage = "error when executing 'create exchange --help' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readErrStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'delete exchange --help'")
    public void testDeleteExchangeHelp() {
        String[] cmd = { CLI_ROOT_COMMAND, "delete", "exchange", "--help" };
        String expectedLog = "Delete an exchange in the Broker";
        String errorMessage = "error when executing 'delete exchange --help' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readErrStream().contains(expectedLog), errorMessage);
    }
}
