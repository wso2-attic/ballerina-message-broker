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
package io.ballerina.messaging.broker.client.cmd.impl.list;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import io.ballerina.messaging.broker.client.http.HttpClient;
import io.ballerina.messaging.broker.client.http.HttpRequest;
import io.ballerina.messaging.broker.client.http.HttpResponse;
import io.ballerina.messaging.broker.client.output.ResponseFormatter;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.resources.Queue;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.client.utils.Utils;

import java.net.HttpURLConnection;

import static io.ballerina.messaging.broker.client.utils.Constants.BROKER_ERROR_MSG;
import static io.ballerina.messaging.broker.client.utils.Constants.HTTP_GET;

/**
 * Command representing MB queue information retrieval.
 */
@Parameters(commandDescription = "List queue(s) in the Broker")
public class ListQueueCmd extends ListCmd {

    @Parameter(names = { "--all", "-a" },
               description = "return info on all queues of the broker")
    private boolean all;

    @Parameter(description = "name of the queue which info needs to be retrieved")
    private String queueName = "";

    public ListQueueCmd(String rootCommand) {
        super(rootCommand);
    }

    @Override
    public void execute() {
        if (help) {
            processHelpLogs();
            return;
        }

        Configuration configuration = Utils.getConfiguration(password);
        HttpClient httpClient = new HttpClient(configuration);

        if (all) {
            queueName = "";
        }

        // do GET
        HttpRequest httpRequest = new HttpRequest(Constants.QUEUES_URL_PARAM + queueName);
        HttpResponse response = httpClient.sendHttpRequest(httpRequest, HTTP_GET);

        if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
            Gson gson = new Gson();
            if (queueName.isEmpty()) {
                Queue[] queues = gson.fromJson(response.getPayload(), Queue[].class);
                responseFormatter.printQueues(queues);
            } else {
                Queue queue = gson.fromJson(response.getPayload(), Queue.class);
                responseFormatter.printQueue(queue);
            }
        } else {
            ResponseFormatter.handleErrorResponse(buildResponseMessage(response, BROKER_ERROR_MSG));
        }

    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  " + rootCommand + " list queue [queue-name]? [flag]*\n");
    }
}
