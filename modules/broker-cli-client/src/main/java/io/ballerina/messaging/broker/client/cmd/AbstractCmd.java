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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.PrintStream;
import java.util.Map;

/**
 * Abstract class to hold common flags/commands.
 */
public abstract class AbstractCmd implements MBClientCmd {

    protected static final PrintStream ERR_STREAM = System.err;

    protected String rootCommand;

    /**
     * Holds the reference of the JCommander runtime instance associated with this command instance.
     */
    protected JCommander selfJCommander;

    @Parameter(names = { "--help", "-h" },
               help = true, hidden = true)
    protected boolean help;

    @Parameter(names = { "--verbose", "-v" }, hidden = true)
    protected boolean verbose;

    /**
     * Constructor, set the root command when creation.
     *
     * @param rootCommand root command loaded from the script.
     */
    protected AbstractCmd(String rootCommand) {
        this.rootCommand = rootCommand;
    }

    public void setSelfJCommander(JCommander selfJCommander) {
        this.selfJCommander = selfJCommander;
    }

    /**
     * Print help messages of the concrete command object.
     */
    protected void processHelpLogs() {
        StringBuilder sb = new StringBuilder();
        appendCommandDescription(selfJCommander, sb);
        appendUsage(sb);
        sb.append("\n");
        appendChildCommandsInfo(selfJCommander, sb);
        appendFlagsInfo(selfJCommander, sb);
        appendGlobalFlagsInfo(sb);
        ERR_STREAM.println(sb.toString());
    }

    /**
     * Extract command description from the jCommander instance and append it to the StringBuilder
     *
     * @param jCommander respective JCommander instance
     * @param sb StringBuilder instance
     */
    static void appendCommandDescription(JCommander jCommander, StringBuilder sb) {
        MBClientCmd selfCommand = (MBClientCmd) jCommander.getObjects().get(0);

        // if no description is provided, return from here
        if (selfCommand.getClass().getAnnotations().length == 0) {
            return;
        }

        Parameters parameters = (Parameters) selfCommand.getClass().getAnnotations()[0];
        String commandDescription = parameters.commandDescription();
        sb.append(commandDescription);
        sb.append("\n\n");
    }

    /**
     * Extract child commands info from JCommander and append those to the given StringBuilder
     *
     * @param jCommander respective JCommander instance
     * @param sb StringBuilder instance
     */
    static void appendChildCommandsInfo(JCommander jCommander, StringBuilder sb) {
        Map<String, JCommander> commandMap =  jCommander.getCommands();

        // if no command is available, return from here
        if (commandMap.isEmpty()) {
            return;
        }

        sb.append("Commands:\n");
        jCommander.getCommands().keySet().forEach(key -> {
            sb.append("  ");
            sb.append(key);
            sb.append("\t");
            sb.append(jCommander.getCommandDescription(key));
            sb.append("\n");
        });
        sb.append("\n");
    }

    /**
     * Extract flags info from the JCommander instance and append those to the provided StringBuilder instance
     *
     * @param jCommander respective JCommander instance
     * @param sb StringBuilder instance
     */
    static void appendFlagsInfo(JCommander jCommander, StringBuilder sb) {
        boolean isFlagsExists = jCommander.getParameters()
                .stream()
                .anyMatch(param -> !param.getParameter().hidden());

        // if no hidden flag is there, return from here
        if (!isFlagsExists) {
            return;
        }

        sb.append("Flags:\n");
        jCommander.getParameters()
                .stream()
                .filter(param -> !param.getParameter().hidden())
                .forEach(param -> {
                    sb.append("  ");
                    sb.append(param.getNames());
                    sb.append("\t");
                    sb.append(param.getDescription());
                    sb.append(" (Default: ");
                    sb.append(param.getDefault());
                    sb.append(")\n");
                });
        sb.append("\n");
    }

    /**
     * Append global level flags info to the provided StringBuilder instance
     *
     * @param sb StringBuilder instance which logs should be appended into
     */
    static void appendGlobalFlagsInfo(StringBuilder sb) {
        sb.append("Global Flags:\n");
        sb.append("  --verbose, -v\t");
        sb.append("enable verbose mode (Default: false)\n");
        sb.append("  --help, -h\t");
        sb.append("ask for help\n");
    }
}
