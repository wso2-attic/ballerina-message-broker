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

package io.ballerina.messaging.secvault.ciphertool;

import io.ballerina.messaging.secvault.ciphertool.utils.CommandLineParser;
import io.ballerina.messaging.secvault.ciphertool.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The Java class which defines the CipherToolInitializer as a broker tool.
 */
public class CipherToolInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CipherToolInitializer.class);

    public static void main(String[] args) {
        CipherToolInitializer.execute(args);
    }

    /**
     * Execute cipher tool.
     *
     * @param toolArgs arguments for executing cipher tool
     */
    public static void execute(String... toolArgs) {
        CommandLineParser commandLineParser;
        try {
            commandLineParser = Utils.createCommandLineParser(toolArgs);
        } catch (CipherToolException e) {
            printHelpMessage();
            throw new CipherToolRuntimeException("Unable to run CipherTool", e);
        }

        URLClassLoader urlClassLoader = Utils.getCustomClassLoader(commandLineParser.getCustomLibPath());

        String brokerFilePath = System.getProperty(CipherToolConstants.SYSTEM_PARAM_BROKER_CONFIG_FILE);
        String configYamlPath = commandLineParser.getCustomConfigPath().orElse(brokerFilePath);

        String commandName = commandLineParser.getCommandName().orElse("");
        String commandParam = commandLineParser.getCommandParam().orElse("");

        try {

            Path secureVaultConfigPath = Paths.get(configYamlPath);
            Object objCipherTool = Utils.createCipherTool(urlClassLoader, secureVaultConfigPath);
            processCommand(commandName, commandParam, objCipherTool);
            if (LOGGER.isDebugEnabled()) {
                if (commandLineParser.getCommandName().isPresent()) {
                    LOGGER.debug("Command: " + commandName + " executed successfully with configuration file " +
                            "path: " + secureVaultConfigPath.toString());
                } else {
                    LOGGER.debug("Secrets encrypted successfully with configuration file path: " +
                            secureVaultConfigPath.toString());
                }
            }
        } catch (CipherToolException e) {
            throw new CipherToolRuntimeException("Unable to run CipherTool", e);
        }
    }

    /**
     * Process command according to the given command.
     *
     * @param command       command string
     * @param parameter     parameter of the command
     * @param objCipherTool ciphertool instance
     * @throws CipherToolException when an error is thrown during ciphertool execution
     */
    private static void processCommand(String command, String parameter, Object objCipherTool)
            throws CipherToolException {
        Method method;
        try {
            switch (command) {
                case CipherToolConstants.ENCRYPT_TEXT_COMMAND:
                    method = objCipherTool.getClass().getMethod(CipherToolConstants.ENCRYPT_TEXT_METHOD, String.class);
                    method.invoke(objCipherTool, parameter);
                    break;
                case CipherToolConstants.DECRYPT_TEXT_COMMAND:
                    method = objCipherTool.getClass().getMethod(CipherToolConstants.DECRYPT_TEXT_METHOD, String.class);
                    method.invoke(objCipherTool, parameter);
                    break;
                default:
                    method = objCipherTool.getClass().getMethod(CipherToolConstants.ENCRYPT_SECRETS_METHOD);
                    method.invoke(objCipherTool);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new CipherToolException("Failed to execute Cipher Tool command", e);
        }
    }

    /**
     * Prints a help message for the secure vault tool usage.
     */
    private static void printHelpMessage() {
        LOGGER.info(
                "\n"
                + "Incorrect usage of the cipher tool."
                + "\n\n"
                + "Instructions: sh ciphertool.sh [<command> <parameter>]"
                + "\n\n"
                + "-- command      -configPath | -encryptText | -decryptText | -customLibPath"
                + "\n"
                + "-- parameter    input to the command"
                + "\n\n"
                + "Usages:"
                + "\n\n"
                + "* Load default secure vault config from [MESSAGE_BROKER_HOME]/conf/broker.yaml and encrypts the "
                + "secrets specified in the [MESSAGE_BROKER_HOME]/conf/security/secrets.properties file. "
                + "\n"
                + "     Eg: ciphertool.sh"
                + "\n\n"
                + "* Load secure vault config from given config path and encrypt secrets in the specified "
                + "secrets.properties file.\n"
                + "     Eg: ciphertool.sh -configPath /home/user/custom/config/secure-vault.yaml"
                + "\n\n"
                + "* Load libraries in the given path first and perform the same operation as above."
                + "\n"
                + "     Eg: ciphertool.sh -configPath /home/user/custom/config/secure-vault.yaml "
                + "-customLibPath /home/user/custom/libs"
                + "\n\n"
                + "* -encryptText : this option will first encrypt a given text and then prints the base64 encoded"
                + "\n"
                + "   string of the encoded cipher text in the console."
                + "\n"
                + "     Eg: ciphertool.sh -encryptText Ballerina@WSO2"
                + "\n\n"
                + "* -decryptText : this option accepts base64 encoded cipher text and prints the decoded plain text "
                + "in the console."
                + "\n"
                + "     Eg: ciphertool.sh -decryptText XxXxXx"
                + "\n"
        );
    }
}
