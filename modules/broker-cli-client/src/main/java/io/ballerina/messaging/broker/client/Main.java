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
import io.ballerina.messaging.broker.client.cmd.MBClientCmd;
import io.ballerina.messaging.broker.client.cmd.impl.InitCmd;
import io.ballerina.messaging.broker.client.cmd.impl.RootCmd;
import io.ballerina.messaging.broker.client.cmd.impl.create.CreateCmd;
import io.ballerina.messaging.broker.client.cmd.impl.create.CreateExchangeCmd;
import io.ballerina.messaging.broker.client.cmd.impl.delete.DeleteCmd;
import io.ballerina.messaging.broker.client.cmd.impl.delete.DeleteExchangeCmd;
import io.ballerina.messaging.broker.client.cmd.impl.list.ListCmd;
import io.ballerina.messaging.broker.client.cmd.impl.list.ListExchangeCmd;
import io.ballerina.messaging.broker.client.utils.BrokerClientException;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.client.utils.Utils;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Main Class of the Broker CLI Client.
 *
 * Main method of this class should run with required arguments to execute a command.
 */
public class Main {

    private static PrintStream outStream = System.err;

    /**
     * This map will contain each Command instance against its JCommander instance.
     * Map will get populated at the time of building the parser tree and will be referred at the traversal.
     */
    private static Map<JCommander, MBClientCmd> commandsMap = new HashMap<>();

    public static void main(String... argv) {

        // 1. Build the parse tree
        JCommander parserTreeRoot = buildCommanderTree();

        try {
            // 2. Parse the input
            parseInput(parserTreeRoot, argv);

            // 3. Traverse the parse tree and execute the matched command
            findLeafCommand(parserTreeRoot).execute();

        } catch (BrokerClientException e) {
            printBrokerClientException(e, outStream);
        }
    }

    /**
     * Build the parser tree with JCommander instance for each command.
     *
     * @return root JCommander of the tree.
     */
    private static JCommander buildCommanderTree() {
        // Building the parser tree
        // root command
        RootCmd rootCmd = new RootCmd();
        JCommander jCommanderRoot = new JCommander(rootCmd);

        commandsMap.put(jCommanderRoot, rootCmd);

        // add to root jCommander
        addChildCommand(jCommanderRoot, Constants.CMD_INIT, new InitCmd());
        JCommander jCommanderList = addChildCommand(jCommanderRoot, Constants.CMD_LIST, new ListCmd());
        JCommander jCommanderCreate = addChildCommand(jCommanderRoot, Constants.CMD_CREATE, new CreateCmd());
        JCommander jCommanderDelete = addChildCommand(jCommanderRoot, Constants.CMD_DELETE, new DeleteCmd());

        // secondary level commands
        // add list sub-commands
        addChildCommand(jCommanderList, Constants.CMD_EXCHANGE, new ListExchangeCmd());

        // add create sub-commands
        addChildCommand(jCommanderCreate, Constants.CMD_EXCHANGE, new CreateExchangeCmd());

        // add delete sub-commands
        addChildCommand(jCommanderDelete, Constants.CMD_EXCHANGE, new DeleteExchangeCmd());

        return jCommanderRoot;
    }

    /**
     * Process the given input through the parser tree.
     *
     * This can throw runtime exceptions through {@link BrokerClientException} when an error occurs in parsing the
     * input.
     *
     * @param jCommanderRoot root node of the tree.
     * @param argv           input as a array of Strings.
     */
    private static void parseInput(JCommander jCommanderRoot, String... argv) {
        try {
            // Parse
            jCommanderRoot.parse(argv);
        } catch (MissingCommandException e) {
            String errorMsg = "unknown command '" + e.getUnknownCommand() + "'";
            throw Utils.createUsageException(errorMsg);
        } catch (ParameterException e) {
            // todo: consider whether we can make the error logs more specific
            throw Utils.createUsageException(e.getMessage());
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
            return commandsMap.get(jCommander);
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
        commandsMap.put(childCommander, commandObject);
        return childCommander;
    }

    private static void printBrokerClientException(BrokerClientException e, PrintStream outStream) {
        List<String> errorMessages = e.getMessages();
        errorMessages.forEach(outStream::println);
    }

    /**
     * Added for the testing purposes.
     * Since the commandsMap is static, before each test it should be cleared.
     */
    public static void clearCommandsMap() {
        commandsMap.clear();
    }
}
