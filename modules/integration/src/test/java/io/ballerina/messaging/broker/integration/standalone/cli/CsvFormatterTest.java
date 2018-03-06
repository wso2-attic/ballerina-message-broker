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
 * Test cases for CSV output formatter.
 */
public class CsvFormatterTest extends CliTestParent {

    @Test(groups = "StreamReading",
          description = "test CSV output formatter for exchanges related info")
    public void testCsvExchangeInfoFormatting() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_EXCHANGE, "--output", "csv" };
        String expectedLog = "\"amq.direct\",direct,true";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
          description = "test CSV output formatter for queues related info")
    public void testCsvQueueInfoFormatting() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_QUEUE, "--output", "csv" };
        String expectedLog = "\"amq.dlq\",0,2147483647,0,true,false";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
          description = "test CSV output formatter for binding related info")
    public void testCsvBindingInfoFormatting() {
        String[] cmd = {
                CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_BINDING, "-e", "amq.dlx", "--output", "csv"
        };
        String expectedLog = "\"amq.dlq\",\"amq.dlq\"";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
          description = "test CSV output formatter invalid formatter type error")
    public void testCsvInvalidFormatterType() {
        String[] cmd = {
                CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_EXCHANGE, "--output", "abc" };
        String expectedLog = "invalid output formatter type provided: abc";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }
}
