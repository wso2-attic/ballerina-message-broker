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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.broker.client.cmd.impl;

import org.wso2.broker.client.cmd.AbstractCmd;

import static org.wso2.broker.client.utils.Utils.createUsageException;

/**
 * Representation of the root command of the CLI client.
 */
public class RootCmd extends AbstractCmd {

    @Override
    public void execute() {
        if (!help) {
            throw createUsageException("a command is expected after 'mb'");
        }
        processHelpLogs();
    }

    @Override
    public String getName() {
        return "default-cmd";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("long desc of the root command\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("usage of the root command\n");
    }
}
