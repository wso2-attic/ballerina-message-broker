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
package io.ballerina.messaging.broker.client.cmd.impl.delete;

import com.beust.jcommander.Parameters;
import io.ballerina.messaging.broker.client.cmd.AbstractCmd;
import io.ballerina.messaging.broker.client.utils.Utils;

/**
 * Command representing the resource deletion.
 */
@Parameters(commandDescription = "Delete resources in the Broker")
public class DeleteCmd extends AbstractCmd {

    public DeleteCmd(String rootCommand) {
        super(rootCommand);
    }

    @Override
    public void execute() {
        if (!help) {
            throw Utils.createUsageException("a command is expected after 'delete'", rootCommand);
        }
        processHelpLogs();
    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  " + rootCommand + " delete [command]\n");
    }
}
