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
package io.ballerina.messaging.broker.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import io.ballerina.messaging.broker.client.cmd.CommandFactory;
import io.ballerina.messaging.broker.client.cmd.MBClientCmd;
import io.ballerina.messaging.broker.client.cmd.impl.RootCmd;
import io.ballerina.messaging.broker.client.utils.BrokerClientException;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.client.utils.Utils;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Main Class of the Broker CLI Client.
 *
 * Main method of this class should run with required arguments to execute a command.
 */
public class Main {

    private static PrintStream outStream = System.err;


    public static void main(String... argv) {

        // exit if no args
        if (argv.length == 0) {
            return;
        }
        String rootCommand = argv[0];


        // if no command is given, display root level help
        if (argv.length == 1) {
            argv[0] = "--help";
        }

        // remove the root command and pass following arguments forward
        if (argv.length > 1) {
            argv = Arrays.copyOfRange(argv, 1, argv.length);
        }

        // 1. Build the parse tree
        JCommander parserTreeRoot = buildCommanderTree(rootCommand);

        try {
            // 2. Parse the input
            parseInput(parserTreeRoot, rootCommand, argv);

            // 3. Traverse the parse tree and execute the matched command
            findLeafCommand(parserTreeRoot).execute();

        } catch (BrokerClientException e) {
            printBrokerClientException(e, outStream);
        }
    }

    /**
     * Build the parser tree with JCommander instance for each command.
     *
     * Make sure to add 'one and only one' MBClientCmd instance for each JCommander instance.
     *
     * @param rootCommand root command used in the script.
     * @return root JCommander of the tree.
     */
    private static JCommander buildCommanderTree(String rootCommand) {
        // Building the parser tree
        CommandFactory commandFactory = new CommandFactory(rootCommand);
        // root command
        RootCmd rootCmd = commandFactory.createRootCommand();
        JCommander jCommanderRoot = new JCommander(rootCmd);
        rootCmd.setSelfJCommander(jCommanderRoot);

        // add to root jCommander
        addChildCommand(jCommanderRoot, Constants.CMD_INIT, commandFactory.createInitCommand());
        JCommander jCommanderList = addChildCommand(jCommanderRoot, Constants.CMD_LIST,
                commandFactory.createListCommand());
        JCommander jCommanderCreate = addChildCommand(jCommanderRoot, Constants.CMD_CREATE,
                commandFactory.createCreateCommand());
        JCommander jCommanderDelete = addChildCommand(jCommanderRoot, Constants.CMD_DELETE,
                commandFactory.createDeleteCommand());
        JCommander jCommanderGrant = addChildCommand(jCommanderRoot, Constants.CMD_GRANT,
                                                      commandFactory.createGrantCommand());
        JCommander jCommanderRevoke = addChildCommand(jCommanderRoot, Constants.CMD_REVOKE,
                                                     commandFactory.createRevokeCommand());
        JCommander jCommanderTransfer = addChildCommand(jCommanderRoot, Constants.CMD_TRANSFER,
                                                      commandFactory.createTransferCommand());

        // secondary level commands
        // add list sub-commands
        addChildCommand(jCommanderList, Constants.CMD_EXCHANGE, commandFactory.createListExchangeCommand());
        addChildCommand(jCommanderList, Constants.CMD_QUEUE, commandFactory.createListQueueCommand());
        addChildCommand(jCommanderList, Constants.CMD_BINDING, commandFactory.createListBindingCommand());
        addChildCommand(jCommanderList, Constants.CMD_CONSUMER, commandFactory.createListConsumerCommand());

        // add create sub-commands
        addChildCommand(jCommanderCreate, Constants.CMD_EXCHANGE, commandFactory.createCreateExchangeCommand());
        addChildCommand(jCommanderCreate, Constants.CMD_QUEUE, commandFactory.createCreateQueueCommand());
        addChildCommand(jCommanderCreate, Constants.CMD_BINDING, commandFactory.createCreateBindingCommand());

        // add delete sub-commands
        addChildCommand(jCommanderDelete, Constants.CMD_EXCHANGE, commandFactory.createDeleteExchangeCommand());
        addChildCommand(jCommanderDelete, Constants.CMD_QUEUE, commandFactory.createDeleteQueueCommand());

        // add grant sub-commands
        addChildCommand(jCommanderGrant, Constants.CMD_QUEUE, commandFactory.createGrantQueueCommand());
        addChildCommand(jCommanderGrant, Constants.CMD_EXCHANGE, commandFactory.createGrantExchangeCommand());

        // add revoke sub-commands
        addChildCommand(jCommanderRevoke, Constants.CMD_QUEUE, commandFactory.createRevokeQueueCommand());
        addChildCommand(jCommanderRevoke, Constants.CMD_EXCHANGE, commandFactory.createRevokeExchangeCommand());

        // add transfer sub-commands
        addChildCommand(jCommanderTransfer, Constants.CMD_QUEUE, commandFactory.createTransferQueueCommand());
        addChildCommand(jCommanderTransfer, Constants.CMD_EXCHANGE, commandFactory.createTransferExchangeCommand());

        return jCommanderRoot;
    }

    /**
     * Process the given input through the parser tree.
     *
     * This can throw runtime exceptions through {@link BrokerClientException} when an error occurs in parsing the
     * input.
     *
     * @param jCommanderRoot root node of the tree.
     * @param rootCommand root command used in the script.
     * @param argv           input as a array of Strings.
     */
    private static void parseInput(JCommander jCommanderRoot, String rootCommand, String... argv) {
        try {
            // Parse
            jCommanderRoot.parse(argv);
        } catch (MissingCommandException e) {
            String errorMsg = "unknown command '" + e.getUnknownCommand() + "'";
            throw Utils.createUsageException(errorMsg, rootCommand);
        } catch (ParameterException e) {
            // todo: consider whether we can make the error logs more specific
            throw Utils.createUsageException(e.getMessage(), rootCommand);
        }
    }

    /**
     * When it needs to find matching command, traversing is done through a recursive algorithm.
     *
     * This can throw runtime exceptions through {@link BrokerClientException} when an error occurs while executing
     * the command.
     *
     * @param jCommander commander in current level.
     * @return matched command instance.
     */
    private static MBClientCmd findLeafCommand(JCommander jCommander) {
        String commandText = jCommander.getParsedCommand();
        if (Objects.isNull(commandText)) {
            return (MBClientCmd) jCommander.getObjects().get(0);
        }
        return findLeafCommand(jCommander.getCommands().get(commandText));
    }

    /**
     * Add a subCommand to its parent commander.
     *
     * Adding a command to a JCommander instance will create a new JCommander instance for the new command, and it
     * will be added into the parents command map. This method will extract the child commander and keep it for
     * future reference (to add another layer of sub-commands) and the new command will also get added into the
     * commandMap to refer at traversing stage.
     *
     * @param parentCommander parent commander instance.
     * @param commandName     name of this command.
     * @param commandObject   annotated command instance.
     * @return jCommander instance created for the new command.
     */
    private static JCommander addChildCommand(JCommander parentCommander, String commandName,
            MBClientCmd commandObject) {
        parentCommander.addCommand(commandName, commandObject);
        JCommander childCommander = parentCommander.getCommands().get(commandName);
        commandObject.setSelfJCommander(childCommander);
        return childCommander;
    }

    private static void printBrokerClientException(BrokerClientException e, PrintStream outStream) {
        List<String> errorMessages = e.getMessages();
        errorMessages.forEach(outStream::println);
    }
}
