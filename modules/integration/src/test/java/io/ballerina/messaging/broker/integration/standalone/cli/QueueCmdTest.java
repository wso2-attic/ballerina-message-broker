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
import io.ballerina.messaging.broker.client.utils.Constants;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.ballerina.messaging.broker.integration.util.TestConstants.CLI_ROOT_COMMAND;

/**
 * Test class containing tests of 'queue' command.
 */
public class QueueCmdTest extends CliTestParent {

    @Test(groups = "StreamReading",
          description = "test command 'list queue'")
    public void testListQueue() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_QUEUE };
        // default dead letter queue should be there
        String expectedLog = "amq.dlq";
        String errorMessage = "error when executing 'list queue' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readOutStream().contains(expectedLog), errorMessage);
    }

    @Test(dependsOnMethods = "testListQueue",
          groups = "StreamReading",
          description = "test command 'create queue sampleQ1'")
    public void testCreateQueue() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_CREATE, Constants.CMD_QUEUE, "sampleQ1" };
        String[] checkCmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_QUEUE, "sampleQ1" };
        String expectedLog = "sampleQ1";
        String errorMessage = "error when executing 'create queue <QUEUE_NAME>' command";

        Main.main(cmd);
        Main.main(checkCmd);

        Assert.assertTrue(PrintStreamHandler.readOutStream().contains(expectedLog), errorMessage);
    }

    @Test(dependsOnMethods = "testCreateQueue",
          groups = "StreamReading",
          description = "test command 'delete queue sampleQ1'")
    public void testDeleteQueue() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_DELETE, Constants.CMD_QUEUE, "sampleQ1" };
        String[] checkCmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_QUEUE };
        String expectedLog = "sampleQ1";
        String errorMessage = "error when executing 'delete queue <QUEUE_NAME>' command";

        Main.main(cmd);
        Main.main(checkCmd);

        Assert.assertTrue(!PrintStreamHandler.readOutStream().contains(expectedLog), errorMessage);
    }

    @Test(dependsOnMethods = "testListQueue",
          groups = "StreamReading",
          description = "test command 'create queue sampleQ2 -d -a'")
    public void testCreateQueueWithFlags() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_CREATE, Constants.CMD_QUEUE, "sampleQ2", "-d", "-a" };
        String[] checkCmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_QUEUE, "sampleQ2" };
        String expectedLog = "sampleQ2";
        String errorMessage = "error when executing 'create queue <QUEUE_NAME> -d -a' command";

        Main.main(cmd);
        Main.main(checkCmd);

        Assert.assertTrue(PrintStreamHandler.readOutStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'list queue --help'")
    public void testListQueueHelp() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_QUEUE, "--help" };
        String expectedLog = "List queue(s) in the Broker";
        String errorMessage = "error when executing 'list queue --help' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readErrStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'create queue --help'")
    public void testCreateQueueHelp() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_CREATE, Constants.CMD_QUEUE, "--help" };
        String expectedLog = "Create a queue in the Broker with parameters";
        String errorMessage = "error when executing 'create queue --help' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readErrStream().contains(expectedLog), errorMessage);
    }

    @Test(groups = "StreamReading",
          description = "test command 'delete queue --help'")
    public void testDeleteQueueHelp() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_DELETE, Constants.CMD_QUEUE, "--help" };
        String expectedLog = "Delete a queue in the Broker";
        String errorMessage = "error when executing 'delete queue --help' command";

        Main.main(cmd);

        Assert.assertTrue(PrintStreamHandler.readErrStream().contains(expectedLog), errorMessage);
    }
}
