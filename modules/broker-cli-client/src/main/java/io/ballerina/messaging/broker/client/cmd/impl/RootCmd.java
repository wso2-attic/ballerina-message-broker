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

import io.ballerina.messaging.broker.client.cmd.AbstractCmd;
import io.ballerina.messaging.broker.client.utils.Utils;

/**
 * Representation of the root command of the CLI client.
 */
public class RootCmd extends AbstractCmd {

    @Override
    public void execute() {
        if (!help) {
            throw Utils.createUsageException("a command is expected after 'mb'");
        }
        processHelpLogs();
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Welcome to Broker Command Line Interface\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        // todo: make the descriptions more detailed and retrieve it from each command implementation,
        // todo: add flags information
        out.append("Usage:\n");
        out.append("  mb [CMD] [TYPE] [NAME]? [OPTION]*\n");
        out.append("* Available commands\n");
        out.append("  create\n");
        out.append("  list\n");
        out.append("  delete\n");
        out.append("* Available resource types\n");
        out.append("  exchange\n");
        out.append("* For more information about each command and each resource type\n");
        out.append("  mb [CMD] --help\n");
        out.append("  mb [CMD] [TYPE] --help\n");
        out.append("* Initialization command\n");
        out.append("  mb init --help\n");
    }
}
