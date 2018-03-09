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
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.Parameters;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Abstract class to hold common flags/commands.
 */
public abstract class AbstractCmd implements MBClientCmd {

    protected static final PrintStream ERR_STREAM = System.err;

    private static final int LOGS_PADDING = 2;

    protected String rootCommand;

    /**
     * Holds the reference of the JCommander runtime instance associated with this command instance.
     */
    protected JCommander selfJCommander;

    @Parameter(names = { "--help", "-h" },
               help = true,
               hidden = true,
               description = "ask for help")
    protected boolean help;

    @Parameter(names = { "--verbose", "-v" },
               hidden = true,
               description = "enable verbose mode")
    protected boolean verbose;

    @Parameter(names = { "--password", "-p" },
               hidden = true,
               description = "Password",
               password = true)
    protected String password = null;

    /**
     * Constructor, set the root command when creation.
     *
     * @param rootCommand root command loaded from the script.
     */
    protected AbstractCmd(String rootCommand) {
        this.rootCommand = rootCommand;
    }

    /**
     * Extract command description from the jCommander instance and append it to the StringBuilder.
     *
     * @param jCommander respective JCommander instance.
     * @param sb         StringBuilder instance.
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
     * Extract child commands info from JCommander and append those to the given StringBuilder.
     *
     * @param jCommander respective JCommander instance.
     * @param sb         StringBuilder instance.
     */
    private static void appendChildCommandsInfo(JCommander jCommander, StringBuilder sb) {
        Map<String, JCommander> commandMap = jCommander.getCommands();

        // if no command is available, return from here
        if (commandMap.isEmpty()) {
            return;
        }

        int maxLength = jCommander.getCommands().keySet().stream().mapToInt(String::length).max().orElse(15);

        sb.append("Commands:\n");
        jCommander.getCommands().keySet().forEach(key -> {
            sb.append(String.format("%2s%-" + String.valueOf(maxLength + LOGS_PADDING) + "s", "", key));
            sb.append(jCommander.getCommandDescription(key));
            sb.append("\n");
        });
        sb.append("\n");
    }

    /**
     * Extract flags info from the JCommander instance and append those to the provided StringBuilder instance.
     *
     * @param jCommander respective JCommander instance.
     * @param sb         StringBuilder instance.
     */
    private static void appendFlagsInfo(JCommander jCommander, StringBuilder sb) {
        List<ParameterDescription> params = jCommander.getParameters().stream()
                .filter(param -> !param.getParameter().hidden()).collect(Collectors.toList());

        // if no hidden flag is there, return from here
        if (params.isEmpty()) {
            return;
        }

        int maxLength = params.stream().mapToInt(param -> param.getNames().length()).max().orElse(15);

        sb.append("Flags:\n");
        params.stream().filter(param -> !param.getParameter().hidden()).forEach(param -> {
            sb.append(String.format("%2s%-" + String.valueOf(maxLength + LOGS_PADDING) + "s", "", param.getNames()));
            sb.append(param.getDescription());
            sb.append(" (default: ");
            sb.append(param.getDefault());
            sb.append(")\n");
        });
        sb.append("\n");
    }

    /**
     * Append global level flags info to the provided StringBuilder instance.
     *
     * @param sb StringBuilder instance which logs should be appended into.
     */
    private static void appendGlobalFlagsInfo(StringBuilder sb) {
        int maxLength = 0;
        Map<String, String> globalFlags = new HashMap<>();

        for (Field field : AbstractCmd.class.getDeclaredFields()) {
            Parameter param = field.getAnnotation(Parameter.class);
            if (Objects.isNull(param)) {
                continue;
            }
            String key = String.join(",", param.names());
            maxLength = Math.max(maxLength, key.length());
            globalFlags.put(key, param.description());
        }

        sb.append("Global Flags:\n");
        int finalMaxLength = maxLength;
        globalFlags.keySet().forEach((flag) -> {
            sb.append(String.format("%2s%-" + String.valueOf(finalMaxLength + LOGS_PADDING) + "s", "", flag));
            sb.append(globalFlags.get(flag));
            sb.append("\n");
        });
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
}
