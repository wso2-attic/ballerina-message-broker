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

import java.util.regex.Pattern;

import static io.ballerina.messaging.broker.integration.util.TestConstants.CLI_ROOT_COMMAND;

/**
 * Test class containing tests of 'logger' command.
 */
public class LoggerCmdTest extends CliTestParent {

    @Test(groups = "StreamReading",
            description = "test command 'show logger --help'")
    public void testShowLoggerHelp() {
        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_SHOW, Constants.CMD_LOGGER, "--help"};
        String expectedLog = "Show logger(s) in the Broker";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
            description = "test command 'update logger --help'")
    public void testUpdateLoggerHelp() {
        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_UPDATE, Constants.CMD_LOGGER, "--help"};
        String expectedLog = "Update the log level of a logger";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
            description = "test command 'show logger'")
    public void testShowLogger() {
        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_SHOW, Constants.CMD_LOGGER};
        String expectedLog = "io.ballerina.messaging.broker";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);
    }

    @Test(groups = "StreamReading",
            description = "test loggers filtering command 'show logger [logger-name]'")
    public void testPositiveShowLoggerFilter() {
        String filter = "*broker.core.*";
        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_SHOW, Constants.CMD_LOGGER, filter};

        Main.main(cmd);

        String[] output = PrintStreamHandler.readOutStream().split("\n");

        Assert.assertTrue(output.length > 4, "Loggers list cannot be empty");

        boolean properlyFilterd = true;
        Pattern pattern = Pattern.compile(filter.replaceAll("\\*", ".*"));
        for (int i = 3; i < output.length - 1; i++) {
            if (!pattern.matcher(output[i]).matches()) {
                properlyFilterd = false;
                break;
            }
        }

        Assert.assertTrue(properlyFilterd, "Loggers have not been filtered properly");
    }

    @Test(groups = "StreamReading",
            description = "test loggers filtering command 'show logger [logger-name]' with non existing logger name")
    public void testShowLoggerFilteringWithInvalidLoggerName() {
        String filter = "i.blrna.brker.cre.fakebrokriml";
        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_SHOW, Constants.CMD_LOGGER, filter};

        Main.main(cmd);

        String expectedLog = "Not found";
        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);

    }

    @Test(groups = "StreamReading",
            description = "test commuand 'update logger [logger-name] --level [log-level]'")
    public void testPositiveUpdateLogger() {
        String loggerName = "io.ballerina.messaging.broker.core.BrokerImpl";
        String logLevel = "WARN";
        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_UPDATE, Constants.CMD_LOGGER, loggerName, "--level", logLevel};

        Main.main(cmd);

        String expectedLog = "Changed log level";
        evalStreamContent(PrintStreamHandler.readOutStream(), expectedLog, cmd);

    }

    @Test(groups = "StreamReading",
            description = "test commuand 'update logger [logger-name] --level [log-level]' without specifying log "
                          + "level")
    public void testUpdateLoggerWithoutLogLevel() {
        String loggerName = "io.ballerina.messaging.broker.core.BrokerImpl";
        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_UPDATE, Constants.CMD_LOGGER, loggerName};

        Main.main(cmd);

        String expectedLog = "logger name and log level are expected after 'update logger'";
        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);

    }

    @Test(groups = "StreamReading",
            description = "test commuand 'update logger [logger-name] --level [log-level]' with an invalid log level")
    public void testUpdateLoggerWithInvalidLogLevel() {
        String loggerName = "io.ballerina.messaging.broker.core.BrokerImpl";
        String logLevel = "WRN";
        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_UPDATE, Constants.CMD_LOGGER, loggerName, "--level", logLevel};

        Main.main(cmd);

        String expectedLog = "not a valid log level.";
        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);

    }

    @Test(groups = "StreamReading",
            description = "test commuand 'update logger [logger-name] --level [log-level]' with a non existing logger")
    public void testUpdatingNonExistingLogger() {
        String loggerName = "i.blrina.mesging.bkerImpl";
        String logLevel = "WARN";
        String[] cmd = {CLI_ROOT_COMMAND, Constants.CMD_UPDATE, Constants.CMD_LOGGER, loggerName, "--level", logLevel};

        Main.main(cmd);

        String expectedLog = "Logger not found";
        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);

    }

}
