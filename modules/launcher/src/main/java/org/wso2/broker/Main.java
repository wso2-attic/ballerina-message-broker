/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.Server;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.configuration.BrokerConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Starting point of the broker.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {

        BrokerConfiguration configuration = loadConfiguration();
        Broker broker = new Broker(configuration);
        broker.startMessageDelivery();
        Server amqpServer = new Server(broker, configuration);
        amqpServer.run();
    }

    
    /**
     * Loads configurations during the broker start up.
     * method will try to <br/>
     *  (1) Load the configuration file specified in 'broker.file' (e.g. -Dbroker.file=<FilePath>). <br/>
     *  (2) If -Dbroker.file is not specified, the broker.yaml file exists in current directory and load it. <br/>
     *  (3) If configuration files are not provided, starts with in-built broker.yaml file. <br/>
     *  
     *  <b>Note: </b> if provided configuration file cannot be read broker will not start.
     * @return a configuration object.
     */
    private static BrokerConfiguration loadConfiguration() {
        
        BrokerConfiguration configuration = null;
        File brokerYamlFile = null;

        String brokerFilePath = System.getProperty(BrokerConfiguration.SYSTEM_PARAM_BROKER_CONFIG_FILE);
        if (brokerFilePath == null || brokerFilePath.trim().isEmpty()) {
            // use current path.
            brokerYamlFile = Paths.get("", BrokerConfiguration.BROKER_FILE_NAME).toAbsolutePath().toFile();

        } else {
            brokerYamlFile = Paths.get(brokerFilePath).toAbsolutePath().toFile();
        }

        InputStream yamlStream = null;

        try {
            if (brokerYamlFile.canRead()) {
                yamlStream = new FileInputStream(brokerYamlFile);
            } else {
                log.info("using in-built configuration file -" + BrokerConfiguration.BROKER_FILE_NAME);
                yamlStream = Main.class.getResourceAsStream("/" + BrokerConfiguration.BROKER_FILE_NAME);

                if (yamlStream == null) {
                    throw new FileNotFoundException(
                            "unable to find - " + BrokerConfiguration.BROKER_FILE_NAME + " in class path");
                }
            }

            Yaml yaml = new Yaml();
            configuration = yaml.loadAs(yamlStream, BrokerConfiguration.class);

        } catch (FileNotFoundException e) {
            String msg = "unable to find - " + BrokerConfiguration.BROKER_FILE_NAME + " broker will terminate now";
            log.warn(msg, e);
            throw new RuntimeException(msg, e);
        } finally {

            try {
                if (yamlStream != null) {
                    yamlStream.close();
                }
            } catch (IOException e) {
                log.error("error while closing file - " + BrokerConfiguration.BROKER_FILE_NAME, e);
            }
        }
        return configuration;
    }
}
