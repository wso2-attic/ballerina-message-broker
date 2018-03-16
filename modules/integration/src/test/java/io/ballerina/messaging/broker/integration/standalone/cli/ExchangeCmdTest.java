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
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static io.ballerina.messaging.broker.integration.util.TestConstants.CLI_ROOT_COMMAND;

/**
 * Test class containing tests of 'exchange' command.
 */
public class ExchangeCmdTest extends CliTestParent {

    @Test(groups = "StreamReading",
          description = "test command 'list exchange'")
    public void testListExchange() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_EXCHANGE };
        String expectedLog = "amq.topic";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
    }

    @Test(dependsOnMethods = "testListExchange",
          groups = "StreamReading",
          description = "test command 'create exchanges sampleEx1'")
    public void testCreateExchange() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_CREATE, Constants.CMD_EXCHANGE, "sampleEx1" };
        String[] checkCmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_EXCHANGE, "sampleEx1" };
        String expectedLog = "sampleEx1";

        Main.main(cmd);
        Main.main(checkCmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
    }

    @Test(dependsOnMethods = "testCreateExchange",
          groups = "StreamReading",
          description = "test command 'delete exchanges sampleEx1'")
    public void testDeleteExchange() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_DELETE, Constants.CMD_EXCHANGE, "sampleEx1" };
        String[] checkCmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_EXCHANGE };
        String notExpectedLog = "sampleEx1";

        Main.main(cmd);
        Main.main(checkCmd);

        // This is a special test case. This has to checked for its non existence.

        // build onFailure message
        StringBuilder sb = new StringBuilder();
        sb.append("error when executing command: " + String.join(" ", cmd) + "\n");
        sb.append("not expected: " + notExpectedLog + "\n");
        sb.append("stream content: " + PrintStreamHandler.readOutStream());

        Assert.assertTrue(!PrintStreamHandler.readOutStream().contains(notExpectedLog), sb.toString());
    }

    @Test(dependsOnMethods = "testListExchange",
          groups = "StreamReading",
          description = "test command 'create exchange sampleEx2 -d -t topic'")
    public void testCreateExchangeWithFlags() {
        String[] cmd = {
                CLI_ROOT_COMMAND, Constants.CMD_CREATE, Constants.CMD_EXCHANGE, "sampleEx2", "-d", "-t", "topic"
        };
        String[] checkCmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_EXCHANGE, "sampleEx2" };
        String expectedLog = "sampleEx2";

        Main.main(cmd);
        Main.main(checkCmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
          description = "test command 'list exchange --help'")
    public void testListExchangeHelp() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_EXCHANGE, "--help" };
        String expectedLog = "List exchange(s) in the Broker";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
          description = "test command 'create exchange --help'")
    public void testCreateExchangeHelp() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_CREATE, Constants.CMD_EXCHANGE, "--help" };
        String expectedLog = "Create an exchange in the Broker with parameters";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
          description = "test command 'delete exchange --help'")
    public void testDeleteExchangeHelp() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_DELETE, Constants.CMD_EXCHANGE, "--help" };
        String expectedLog = "Delete an exchange in the Broker";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
          description = "test command 'grant exchange'")
    public void testGrantRevokeExchange() {
        String exchangeName = "testGrantExchange";
        String[] createCmd = { CLI_ROOT_COMMAND, Constants.CMD_CREATE, Constants.CMD_EXCHANGE, exchangeName, "-d" };

        String[] grantCmd = { CLI_ROOT_COMMAND, Constants.CMD_GRANT, Constants.CMD_EXCHANGE, exchangeName, "-a", "get",
                              "-g", "manager" };

        String[] checkCmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_EXCHANGE, exchangeName };

        Main.main(createCmd);
        Main.main(grantCmd);
        Main.main(checkCmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), "get: manager", grantCmd);

        PrintStreamHandler.resetStreams();

        String[] revokeCmd = {
                CLI_ROOT_COMMAND, Constants.CMD_REVOKE, Constants.CMD_EXCHANGE, exchangeName, "-a", "get", "-g",
                "manager"
        };

        Main.main(revokeCmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), "User group successfully removed.", revokeCmd);
    }

    @Test(groups = "StreamReading", description = "test command 'transfer exchange'")
    @Parameters({ "test-username"})
    public void testChangeOwnerExchange(String testUser) {
        String exchangeName = "testChangeOwnerExchange";
        String[] createCmd = { CLI_ROOT_COMMAND, Constants.CMD_CREATE, Constants.CMD_EXCHANGE, exchangeName, "-d" };

        String[] changeOwnerCmd = {
                CLI_ROOT_COMMAND, Constants.CMD_TRANSFER, Constants.CMD_EXCHANGE, exchangeName, "-n", testUser
        };

        String[] checkCmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_EXCHANGE, exchangeName };

        Main.main(createCmd);
        Main.main(changeOwnerCmd);
        Main.main(checkCmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), "owner   : " + testUser, changeOwnerCmd);
    }
}
