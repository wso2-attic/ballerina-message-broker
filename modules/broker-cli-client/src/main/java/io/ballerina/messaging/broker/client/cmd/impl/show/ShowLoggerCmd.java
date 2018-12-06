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

package io.ballerina.messaging.broker.client.cmd.impl.show;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import io.ballerina.messaging.broker.client.http.HttpClient;
import io.ballerina.messaging.broker.client.http.HttpRequest;
import io.ballerina.messaging.broker.client.http.HttpResponse;
import io.ballerina.messaging.broker.client.output.ResponseFormatter;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.resources.Logger;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.client.utils.Utils;

import java.net.HttpURLConnection;

import static io.ballerina.messaging.broker.client.utils.Constants.BROKER_ERROR_MSG;
import static io.ballerina.messaging.broker.client.utils.Constants.HTTP_GET;

/**
 * Command representing the logger information retrieval.
 */
@Parameters(commandDescription = "Show logger(s) in the Broker")
public class ShowLoggerCmd extends ShowCmd {

    @Parameter(description = "regular expression for filtering loggers by name")
    private String loggerName = "";

    public ShowLoggerCmd(String rootCommand) {
        super(rootCommand);
    }

    @Override
    public void execute() {

        if (help) {
            processHelpLogs();
        } else {
            Configuration configuration = Utils.getConfiguration(password);
            HttpClient httpClient = new HttpClient(configuration);

            // getting logger information
            HttpRequest httpRequest;
            if (loggerName.isEmpty()) {
                httpRequest = new HttpRequest(Constants.LOGGERS_URL_PARAM);
            } else {
                httpRequest = new HttpRequest(Constants.LOGGERS_URL_PARAM + loggerName);
            }
            HttpResponse response = httpClient.sendHttpRequest(httpRequest, HTTP_GET);

            // handle response
            if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                Gson gson = new Gson();
                Logger[] loggers = gson.fromJson(response.getPayload(), Logger[].class);
                responseFormatter.printLoggers(loggers);
            } else {
                ResponseFormatter.handleErrorResponse(buildResponseMessage(response, BROKER_ERROR_MSG));
            }
        }

    }


    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  ").append(rootCommand).append(" show logger [logger-name]\n");
    }

}
