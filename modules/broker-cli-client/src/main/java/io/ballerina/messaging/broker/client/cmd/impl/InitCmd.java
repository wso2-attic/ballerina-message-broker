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
package io.ballerina.messaging.broker.client.cmd.impl;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.ballerina.messaging.broker.client.cmd.AbstractCmd;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.client.utils.Utils;

/**
 * Representation of the broker client initialization command.
 */
@Parameters(commandDescription = "Initialize the MB CLI Client by providing "
        + "HTTP connection details and user credentials")
public class InitCmd extends AbstractCmd {

    @Parameter(names = { "--host", "-H" })
    private String hostname = Constants.DEFAULT_HOSTNAME;

    @Parameter(names = { "--port", "-P" })
    private int port = Constants.DEFAULT_PORT;

    @Parameter(names = { "--username", "-u" })
    private String username = Constants.DEFAULT_USERNAME;

    @Parameter(names = { "--password", "-p" },
               password = true)
    private String password = Constants.DEFAULT_PASSWORD;

    @Override
    public void execute() {
        if (help) {
            processHelpLogs();
            return;
        }
        if (verbose) {
            String message = "Initialize MB CLI client with hostname: " + hostname + ", port: " + port + ", username: "
                    + username;
            OUT_STREAM.println(message);
        }
        Configuration configuration = new Configuration(hostname, port, username, password);
        Utils.createConfigurationFile(configuration);
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Initialize MB admin client with connection details and user credentials.\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  mb init [options]*\n");
        out.append("Example:\n");
        out.append("* Enter MB REST service connection details and user credentials.\n");
        out.append("  mb init -H localhost -P 9000 -u admin -p admin123\n");
    }
}
