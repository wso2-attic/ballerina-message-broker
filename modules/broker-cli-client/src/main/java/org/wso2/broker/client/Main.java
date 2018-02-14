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
package org.wso2.broker.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import org.wso2.broker.client.cmd.impl.InitCmd;
import org.wso2.broker.client.cmd.impl.RootCmd;
import org.wso2.broker.client.cmd.impl.create.CreateCmd;
import org.wso2.broker.client.cmd.impl.create.CreateExchangeCmd;
import org.wso2.broker.client.cmd.impl.delete.DeleteCmd;
import org.wso2.broker.client.cmd.impl.delete.DeleteExchangeCmd;
import org.wso2.broker.client.cmd.impl.list.ListCmd;
import org.wso2.broker.client.cmd.impl.list.ListExchangeCmd;
import org.wso2.broker.client.utils.BrokerClientException;

import java.io.PrintStream;
import java.util.List;

import static org.wso2.broker.client.utils.Utils.createUsageException;

/**
 * Main Class of the Broker CLI Client
 * <p>
 * Main method of this class should run with required arguments to execute a command
 */
public class Main {

    private static final String JC_UNKNOWN_OPTION_PREFIX = "Unknown option:";
    private static final String JC_EXPECTED_A_VALUE_AFTER_PARAMETER_PREFIX = "Expected a value after parameter";
    private static final String JC_WAS_PASSED_MAIN_PARAMETER_PREFIX = "Was passed main parameter";
    private static final String JC_NO_MAIN_PARAM_IS_DEFINED = "but no main parameter was defined in your arg class";

    private static PrintStream outStream = System.err;

    public static void main(String... argv) {
        try {
            parseInput(argv);
        } catch (BrokerClientException e) {
            printBrokerClientException(e, outStream);
        }

    }

    private static void parseInput(String... argv) throws BrokerClientException {

        // 1. Building the parser tree
        // root command
        RootCmd rootCmd = new RootCmd();
        JCommander jCommanderRoot = new JCommander(rootCmd);

        // top level commands
        InitCmd initCmd = new InitCmd();
        ListCmd listCmd = new ListCmd();
        CreateCmd createCmd = new CreateCmd();
        DeleteCmd deleteCmd = new DeleteCmd();

        // add to root jCommander
        addSubCommand(jCommanderRoot, initCmd.getName(), initCmd);
        JCommander jCommanderList = addSubCommand(jCommanderRoot, listCmd.getName(), listCmd);
        JCommander jCommanderCreate = addSubCommand(jCommanderRoot, createCmd.getName(), createCmd);
        JCommander jCommanderDelete = addSubCommand(jCommanderRoot, deleteCmd.getName(), deleteCmd);

        // secondary level commands
        // add list sub-commands
        ListExchangeCmd listExchangeCmd = new ListExchangeCmd();
        addSubCommand(jCommanderList, listExchangeCmd.getName(), listExchangeCmd);

        // add create sub-commands
        CreateExchangeCmd createExchangeCmd = new CreateExchangeCmd();
        addSubCommand(jCommanderCreate, createExchangeCmd.getName(), createExchangeCmd);

        // add delete sub-commands
        DeleteExchangeCmd deleteExchangeCmd = new DeleteExchangeCmd();
        addSubCommand(jCommanderDelete, deleteExchangeCmd.getName(), deleteExchangeCmd);

        // 2. Parse input
        try {
            jCommanderRoot.parse(argv);

            if (initCmd.getName().equals(jCommanderRoot.getParsedCommand())) {
                initCmd.execute();
            } else if (listCmd.getName().equals(jCommanderRoot.getParsedCommand())) {
                if (jCommanderList.getParsedCommand() == null) {
                    // if there is no child command is given for 'list', execute the 'list' command
                    listCmd.execute();
                    return;
                }
                // select the respective list command based on resource type
                listExchangeCmd.execute();
            } else if (createCmd.getName().equals(jCommanderRoot.getParsedCommand())) {
                if (jCommanderCreate.getParsedCommand() == null) {
                    // if there is no child command is given for 'create', execute the 'create' command
                    createCmd.execute();
                    return;
                }
                // select the respective create command based on resource type
                createExchangeCmd.execute();
            } else if (deleteCmd.getName().equals(jCommanderRoot.getParsedCommand())) {
                if (jCommanderDelete.getParsedCommand() == null) {
                    // if there is no child command is given for 'delete', execute the 'delete' command
                    deleteCmd.execute();
                    return;
                }
                // select the respective delete command based on resource type
                deleteExchangeCmd.execute();
            } else {
                rootCmd.execute();
            }

        } catch (MissingCommandException e) {
            String errorMsg = "unknown command '" + e.getUnknownCommand() + "'";
            throw createUsageException(errorMsg);
        } catch (ParameterException e) {
            String msg = e.getMessage();
            if (msg == null) {
                throw createUsageException("internal error occurred");

            } else if (msg.startsWith(JC_UNKNOWN_OPTION_PREFIX)) {
                String flag = msg.substring(JC_UNKNOWN_OPTION_PREFIX.length());
                throw createUsageException("unknown flag '" + flag.trim() + "'");

            } else if (msg.startsWith(JC_EXPECTED_A_VALUE_AFTER_PARAMETER_PREFIX)) {
                String flag = msg.substring(JC_EXPECTED_A_VALUE_AFTER_PARAMETER_PREFIX.length());
                throw createUsageException("flag '" + flag.trim() + "' needs an argument");
            } else if (msg.contains(JC_WAS_PASSED_MAIN_PARAMETER_PREFIX)) {
                String flag = msg.substring(JC_WAS_PASSED_MAIN_PARAMETER_PREFIX.length());
                flag = flag.substring(0, flag.length() - JC_NO_MAIN_PARAM_IS_DEFINED.length());
                throw createUsageException("parameter '" + flag.trim() + "' is invalid");
            } else {
                // Make the first character of the error message lower case
                throw createUsageException(msg);
            }
        }
    }

    private static JCommander addSubCommand(JCommander parentCmd, String commandName, Object commandObject) {
        parentCmd.addCommand(commandName, commandObject);

        return parentCmd.getCommands().get(commandName);
    }

    private static void printBrokerClientException(BrokerClientException e, PrintStream outStream) {
        List<String> errorMessages = e.getMessages();
        errorMessages.forEach(outStream::println);
    }
}
