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
package io.ballerina.messaging.broker.client.cmd;

import com.beust.jcommander.Parameter;

import java.io.PrintStream;

/**
 * Abstract class to hold common flags/commands.
 */
public abstract class AbstractCmd implements MBClientCmd {

    protected static final PrintStream OUT_STREAM = System.err;

    @Parameter(names = { "--help", "-h" },
               help = true)
    protected boolean help;

    @Parameter(names = { "--verbose", "-v" })
    protected boolean verbose;

    /**
     * Print help messages of the concrete command object.
     */
    protected void processHelpLogs() {
        StringBuilder builder = new StringBuilder();
        printLongDesc(builder);
        builder.append("\n");
        printUsage(builder);
        OUT_STREAM.println(builder.toString());
    }
}
