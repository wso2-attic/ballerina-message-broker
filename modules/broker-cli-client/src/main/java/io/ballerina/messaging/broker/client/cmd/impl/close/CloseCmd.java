/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.client.cmd.impl.close;

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
import static io.ballerina.messaging.broker.client.utils.Constants.HTTP_DELETE;

/**
 * Command representing close.
 */
@Parameters(commandDescription = "Close a resource in the Broker with parameters")
public class CloseCmd extends AbstractCmd {

    public CloseCmd(String rootCommand) {
        super(rootCommand);
    }

    @Override
    public void execute() {
        if (!help) {
            throw Utils.createUsageException("a command is expected after 'close'", rootCommand);
        }
        processHelpLogs();
    }

    @Override
    public void appendUsage(StringBuilder out) {
        appendUsage(out, "  " + rootCommand + " close [command] [flag]*\n");
    }

    void appendUsage(StringBuilder out, String usage) {
        out.append("Usage:\n").append("  ").append(rootCommand).append(" ").append(usage);
    }

    private void performCloseOverHttp(String urlSuffix, String defaultResponse) {
        Configuration configuration = Utils.getConfiguration(password);
        HttpClient httpClient = new HttpClient(configuration);
        HttpRequest httpRequest = new HttpRequest(urlSuffix);

        // do DELETE
        HttpResponse response = httpClient.sendHttpRequest(httpRequest, HTTP_DELETE);

        // handle response
        if (response.getStatusCode() == HttpURLConnection.HTTP_ACCEPTED) {
            Message message = buildResponseMessage(response, defaultResponse);
            ResponseFormatter.printMessage(message);
        } else {
            ResponseFormatter.handleErrorResponse(buildResponseMessage(response, BROKER_ERROR_MSG));
        }
    }

    void executeClose(String urlSuffix, String defaultResponse) {
        if (!help) {
            performCloseOverHttp(urlSuffix, defaultResponse);
        } else {
            processHelpLogs();
        }
    }


}
