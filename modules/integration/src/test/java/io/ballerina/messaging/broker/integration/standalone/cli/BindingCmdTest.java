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
 * Test class containing tests of 'binding' command.
 */
public class BindingCmdTest extends CliTestParent {

    @Test(groups = "StreamReading",
          description = "test command 'create binding routing-key -e exchange1 -q queue1'")
    public void tesCreateBinding() {
        String[] xCreate = { CLI_ROOT_COMMAND, Constants.CMD_CREATE, Constants.CMD_EXCHANGE, "exchange1" };
        String[] qCreate = { CLI_ROOT_COMMAND, Constants.CMD_CREATE, Constants.CMD_QUEUE, "queue1" };
        String[] bind = {
                CLI_ROOT_COMMAND, Constants.CMD_CREATE, Constants.CMD_BINDING, "routing-key", "-e", "exchange1", "-q",
                "queue1"
        };
        String expectedLog = "Binding created";

        Main.main(xCreate);
        Main.main(qCreate);
        Main.main(bind);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, bind);
    }

    @Test(dependsOnMethods = "tesCreateBinding",
          groups = "StreamReading",
          description = "test command 'list binding -e exchange1'")
    public void tesListBinding() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_BINDING, "-e", "exchange1" };
        String expectedLog = "queue1";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
    }

}
