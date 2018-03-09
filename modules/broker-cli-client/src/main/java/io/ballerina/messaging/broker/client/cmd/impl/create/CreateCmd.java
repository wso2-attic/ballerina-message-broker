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
package io.ballerina.messaging.broker.client.cmd.impl.create;

import com.beust.jcommander.Parameters;
import io.ballerina.messaging.broker.client.cmd.AbstractCmd;
import io.ballerina.messaging.broker.client.http.HttpClient;
import io.ballerina.messaging.broker.client.http.HttpRequest;
import io.ballerina.messaging.broker.client.http.HttpResponse;
import io.ballerina.messaging.broker.client.output.ResponseFormatter;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.resources.Message;
import io.ballerina.messaging.broker.client.utils.Utils;

import java.net.HttpURLConnection;

import static io.ballerina.messaging.broker.client.utils.Constants.BROKER_ERROR_MSG;
import static io.ballerina.messaging.broker.client.utils.Constants.HTTP_POST;

/**
 * Command representing the resource creation.
 */
@Parameters(commandDescription = "Create a resource in the Broker with parameters")
public class CreateCmd extends AbstractCmd {

    private String defaultSuccessMessage;

    public CreateCmd(String rootCommand) {
        super(rootCommand);
    }

    public CreateCmd(String rootCommand, String defaultSuccessMessage) {
        super(rootCommand);
        this.defaultSuccessMessage = defaultSuccessMessage;
    }

    @Override
    public void execute() {
        if (!help) {
            throw Utils.createUsageException("a command is expected after 'create'", rootCommand);
        }
        processHelpLogs();
    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  " + rootCommand + " create [command]\n");
    }

    /**
     * This will perform all the http request response processing related to resource creation.
     *
     * @param urlSuffix suffix that needs to be appended to the url.
     * @param payload message payload needs to be included.
     */
    void performResourceCreationOverHttp(String urlSuffix, String payload) {
        Configuration configuration = Utils.getConfiguration(password);
        HttpClient httpClient = new HttpClient(configuration);

        // do POST
        HttpRequest httpRequest = new HttpRequest(urlSuffix, payload);
        HttpResponse response = httpClient.sendHttpRequest(httpRequest, HTTP_POST);

        // handle response
        if (response.getStatusCode() == HttpURLConnection.HTTP_CREATED) {
            Message message = buildResponseMessage(response, defaultSuccessMessage);
            ResponseFormatter.printMessage(message);
        } else {
            ResponseFormatter.handleErrorResponse(buildResponseMessage(response, BROKER_ERROR_MSG));
        }
    }
}
