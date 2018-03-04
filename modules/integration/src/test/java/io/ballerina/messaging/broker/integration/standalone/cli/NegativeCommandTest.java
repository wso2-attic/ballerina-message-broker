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
import org.testng.annotations.Test;

import static io.ballerina.messaging.broker.integration.util.TestConstants.CLI_ROOT_COMMAND;

/**
 * Test class containing tests of invalid commands.
 */
public class NegativeCommandTest extends CliTestParent {

    @Test(groups = "StreamReading",
          description = "test command 'list abc'")
    public void testListInvalidResource() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, "abc" };
        String expectedLog = "unknown command 'abc'";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
          description = "test command 'create abc'")
    public void testCreateInvalidResource() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_CREATE, "abc" };
        String expectedLog = "unknown command 'abc'";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
          description = "test command 'delete abc'")
    public void testDeleteInvalidResource() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_DELETE, "abc" };
        String expectedLog = "unknown command 'abc'";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
          description = "test command 'list exchange ex1 ex2'")
    public void testMultipleMainValues() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_EXCHANGE, "ex1", "ex2" };
        String expectedLog = "Only one main parameter allowed but found several: \"ex1\" and \"ex2\"";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }
}
