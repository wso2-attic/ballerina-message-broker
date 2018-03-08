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
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.utils.BrokerClientException;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.client.utils.Utils;
import io.ballerina.messaging.broker.integration.util.TestConstants;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.FileUtils;

import java.io.File;
import java.io.IOException;

import static io.ballerina.messaging.broker.integration.util.TestConstants.CLI_ROOT_COMMAND;

/**
 * Test class containing tests of 'init' command.
 */
public class InitCmdTest extends CliTestParent {
    private static final String TEMP_CONFIG_DIR = "target/config/";
    private static final String INVALID_CONFIG_PATH = "src/test/resources/config/cli-config-invalid.yaml";

    @BeforeClass
    public void init() {
        // create a temp directory
        File file = new File(TEMP_CONFIG_DIR);
        file.mkdir();
    }

    @AfterClass
    public void reset() throws IOException {
        // delete the temp directory
        FileUtils.deleteDir(new File(TEMP_CONFIG_DIR));
    }

    @Test(description = "test command 'init --help'",
          groups = "StreamReading")
    public void testInitCmdHelp() {
        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_INIT, "--help" };
        String expectedLog =
                "Initialize the Broker CLI Client by providing HTTP connection details and user " + "credentials";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(description = "test corrupted configuration file",
          groups = "StreamReading")
    public void testCorruptedConfig() {
        System.setProperty(TestConstants.CLI_CONFIG_SYSTEM_PROPERTY, INVALID_CONFIG_PATH);

        String[] cmd = { CLI_ROOT_COMMAND, Constants.CMD_LIST, Constants.CMD_EXCHANGE };
        String expectedLog = "Error in the CLI client configuration";

        Main.main(cmd);

        evalStreamContent(PrintStreamHandler.readErrStream(), expectedLog, cmd);
    }

    @Test(description = "test command 'init'")
    public void testInitCmdNoParam() {
        System.setProperty(TestConstants.CLI_CONFIG_SYSTEM_PROPERTY, TEMP_CONFIG_DIR + "cli-config1.yaml");

        String[] cmd = { CLI_ROOT_COMMAND, "init" };

        Main.main(cmd);
        Configuration configuration = Utils.getConfiguration("admin");

        Assert.assertNotNull(configuration, "configuration file is not correctly created");

        Assert.assertEquals(configuration.getHostname(), "127.0.0.1", "hostname is invalid in the configuration file");
        Assert.assertEquals(configuration.getPort(), 9000, "port is invalid in the configuration file");
        Assert.assertEquals(configuration.getUsername(), "admin", "username is invalid in the configuration file");
        Assert.assertEquals(configuration.getPassword(), "admin", "password is invalid in the configuration file");
    }

    @Test(description = "test command 'init --hostname 192.168.100.1 --port 9090 --username admin_user "
            + "--password  admin123'")
    public void testInitCmdWithParam() {
        System.setProperty(TestConstants.CLI_CONFIG_SYSTEM_PROPERTY, TEMP_CONFIG_DIR + "cli-config2.yaml");

        String[] cmd = {
                CLI_ROOT_COMMAND, Constants.CMD_INIT, "--host", "192.168.100.1", "--port", "9090", "--username",
                "admin_user", "--password", "admin123"
        };

        Main.main(cmd);
        Configuration configuration = Utils.getConfiguration(null);

        Assert.assertNotNull(configuration, "configuration file is not correctly created");

        Assert.assertEquals(configuration.getHostname(), "192.168.100.1",
                "hostname is invalid in the configuration file");
        Assert.assertEquals(configuration.getPort(), 9090, "port is invalid in the configuration file");
        Assert.assertEquals(configuration.getUsername(), "admin_user", "username is invalid in the configuration file");
        Assert.assertEquals(configuration.getPassword(), "admin123", "password is invalid in the configuration file");
    }


    @Test(description = "test configuration 'without password' invoked without providing a command password")
    public void testNoPasswordParam() {
        System.setProperty(TestConstants.CLI_CONFIG_SYSTEM_PROPERTY, InitCmdTest.TEMP_CONFIG_DIR + "cli-config3.yaml");

        String[] cmd = { CLI_ROOT_COMMAND, "init" };
        String expectedExpMessage = "User password is not provided.";

        boolean doesContain = false;

        Main.main(cmd);
        // haven't used testNG builtin exception support because BrokerClientException doesn't support
        // getDetailedMessage method
        try {
            Utils.getConfiguration(null);
        } catch (BrokerClientException ex) {
            doesContain = ex.getMessages().stream().anyMatch(msg -> msg.contains(expectedExpMessage));
        }

        Assert.assertTrue(doesContain, "no exception is thrown or exception message is invalid");
    }

}
