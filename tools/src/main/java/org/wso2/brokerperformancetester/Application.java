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

package org.wso2.brokerperformancetester;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.wso2.brokerperformancetester.Utils.ToolConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Provide cli interface to create
 */
public class Application {

    private static final Logger log = Logger.getLogger(Application.class);

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption("p", "properties", true, "Properties file to create the test plan.");
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("p")) {
                log.info("Using cli argument -p=" + cmd.getOptionValue("p"));
                ToolConfiguration toolConfiguration = new ToolConfiguration(cmd.getOptionValue("p"));
                String jndiPath = toolConfiguration.getJndiPropertyPath();
                if (jndiPath == null) {
                    log.error("Jndi properties file should be provided to run the tool.");
                } else {
                    int loopCount = toolConfiguration.getLoopCount();
                    int threadCount = toolConfiguration.getThreadCount();
                    int rampTime = toolConfiguration.getRampTime();
                    String message = toolConfiguration.getMessage();
                    String jmeterCommand = toolConfiguration.getJmeterHome() + " -n -t JmsPublisher.jmx -DTHREAD_COUNT="
                            + threadCount + " -DRAMP_TIME=" + rampTime + " -DLOOP_COUNT=" + loopCount + " -DJNDI_URL="
                            + jndiPath + " -DMESSAGE=" + message.replaceAll("\n", "");
                    Process jmeterProcess = Runtime.getRuntime().exec(jmeterCommand);
                    BufferedReader processOutput = new BufferedReader(new InputStreamReader(jmeterProcess.getInputStream()));
                    String output;
                    while ((output = processOutput.readLine()) != null) {
                        log.info(output);
                    }
                }
            } else {
                log.error("Missing -p option");
            }
        } catch (ParseException parseException) {
            log.error("Failed to parse comand line properties " + parseException.getMessage());
        } catch (IOException ioEx) {
            log.error("Error occured while executing jmeter " + ioEx.getMessage());
        }
    }
}