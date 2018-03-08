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
package io.ballerina.messaging.broker.client.utils;

import io.ballerina.messaging.broker.client.resources.Configuration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static io.ballerina.messaging.broker.client.utils.Constants.SYSTEM_PARAM_CLI_CLIENT_CONFIG_FILE;

/**
 * Container class for common static methods of Broker CLI Client.
 */
public class Utils {

    /**
     * Create {@link BrokerClientException} instance including the error message.
     *
     * @param errorMsg    error message.
     * @param rootCommand root command used in the script.
     * @return new {@link BrokerClientException} instance with error message.
     */
    public static BrokerClientException createUsageException(String errorMsg, String rootCommand) {
        BrokerClientException brokerClientException = new BrokerClientException();
        brokerClientException.addMessage(rootCommand + ": " + errorMsg);
        brokerClientException.addMessage("Run '" + rootCommand + " --help' for usage.");
        return brokerClientException;
    }

    private static Configuration readConfigurationFile() {
        Yaml yaml = new Yaml();

        try (InputStream in = new FileInputStream(getConfigFilePath())) {
            Configuration configuration = yaml.loadAs(in, Configuration.class);
            // validate the configuration
            if (!Configuration.validateConfiguration(configuration)) {
                BrokerClientException exception = new BrokerClientException();
                exception.addMessage("Error in the CLI client configuration");
                exception.addMessage("Please re-initialize using 'init' command");
                throw exception;
            }
            return configuration;
        } catch (IOException e) {
            BrokerClientException brokerClientException = new BrokerClientException();
            brokerClientException.addMessage("error when reading the configuration file. " + e.getMessage());
            throw brokerClientException;
        }
    }

    /**
     * Read CLI Client configuration file and binds its information into a {@link Configuration} instance.
     * If the password needs to be overridden it should be passed into the method, otherwise pass null.
     *
     * @param password updated password that should be set into the Configuration instance. Pass null to use existing.
     * @return generated {@link Configuration} instance.
     */
    public static Configuration getConfiguration(String password) {
        Configuration configuration = readConfigurationFile();
        // order-ride the password
        if (Objects.nonNull(password)) {
            configuration.setPassword(password);
        }

        if (Objects.isNull(configuration.getPassword())) {
            BrokerClientException exception = new BrokerClientException();
            exception.addMessage("User password is not provided.");
            exception.addMessage(
                    "Setup the password at global level using 'init' command or provide password when executing each "
                            + "command using (--password|-p) flag");
            throw exception;
        }
        return configuration;
    }

    /**
     * Create the CLI configuration information file.
     *
     * @param configuration instance containing the Configuration information.
     */
    public static void createConfigurationFile(Configuration configuration) {
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        // dump to the file
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(getConfigFilePath()),
                StandardCharsets.UTF_8)) {
            yaml.dump(configuration, writer);
        } catch (IOException e) {
            BrokerClientException brokerClientException = new BrokerClientException();
            brokerClientException.addMessage("error when creating the configuration file. " + e.getMessage());
            throw brokerClientException;
        }
    }

    private static String getConfigFilePath() {
        String path = System.getProperty(SYSTEM_PARAM_CLI_CLIENT_CONFIG_FILE);
        if (path != null && !path.trim().isEmpty()) {
            return path;
        }
        return Constants.DEFAULT_CONFIG_FILE_PATH;
    }
}
