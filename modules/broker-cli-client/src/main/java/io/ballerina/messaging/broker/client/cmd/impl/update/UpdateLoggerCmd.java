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
package io.ballerina.messaging.broker.client.cmd.impl.update;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import io.ballerina.messaging.broker.client.cmd.AbstractCmd;
import io.ballerina.messaging.broker.client.http.HttpClient;
import io.ballerina.messaging.broker.client.http.HttpRequest;
import io.ballerina.messaging.broker.client.http.HttpResponse;
import io.ballerina.messaging.broker.client.output.ResponseFormatter;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.resources.Logger;
import io.ballerina.messaging.broker.client.resources.Message;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.client.utils.Utils;

import java.net.HttpURLConnection;

import static io.ballerina.messaging.broker.client.utils.Constants.BROKER_ERROR_MSG;
import static io.ballerina.messaging.broker.client.utils.Constants.HTTP_PUT;

/**
 * Command representing the updating of log level of a logger.
 */
@Parameters(commandDescription = "Update the log level of a logger")
public class UpdateLoggerCmd extends AbstractCmd {

    @Parameter(description = "name of the logger")
    private String loggerName;

    @Parameter(names = {"--level", "-l"},
            description = "new level (OFF , TRACE , DEBUG , INFO , WARN , ERROR , FATAL)")
    private String logLevel;

    public UpdateLoggerCmd(String rootCommand) {
        super(rootCommand);
    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  ").append(rootCommand).append(" update logger [logger-name] --level [log-level] \n");
    }

    @Override
    public void execute() {

        if (help) {
            processHelpLogs();
            return;
        }

        if (loggerName == null || logLevel == null) {
            throw Utils.createUsageException("logger name and log level are expected after 'update logger'",
                                             rootCommand);
        }

        Logger logger = new Logger(loggerName, logLevel);
        Configuration configuration = Utils.getConfiguration(password);
        HttpClient httpClient = new HttpClient(configuration);

        // updating log level
        HttpRequest httpRequest = new HttpRequest(Constants.LOGGERS_URL_PARAM, (new Gson()).toJson(logger));
        HttpResponse response = httpClient.sendHttpRequest(httpRequest, HTTP_PUT);

        // handle response
        if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
            Message message = buildResponseMessage(response, "Log level changed successfully");
            ResponseFormatter.printMessage(message);
        } else {
            ResponseFormatter.handleErrorResponse(buildResponseMessage(response, BROKER_ERROR_MSG));
        }
    }

}
