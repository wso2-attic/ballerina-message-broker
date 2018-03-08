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
import io.ballerina.messaging.broker.client.output.TableFormatter;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.resources.Consumer;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.client.utils.Utils;

import java.net.HttpURLConnection;

import static io.ballerina.messaging.broker.client.utils.Constants.BROKER_ERROR_MSG;
import static io.ballerina.messaging.broker.client.utils.Constants.HTTP_GET;

/**
 * Command representing MB consumer information retrieval.
 */
@Parameters(commandDescription = "List consumers in a Broker queue")
public class ListConsumerCmd extends ListCmd {

    @Parameter(names = { "--all", "-a" },
               description = "return info on all consumer of the queue")
    private boolean all;

    @Parameter(names = { "--consumer", "-c" },
               description = "id of the consumer which info needs to be retrieved")
    private String consumerId = "";

    @Parameter(description = "name of the queue",
               required = true)
    private String queueName = "";

    public ListConsumerCmd(String rootCommand) {
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
            consumerId = "";
        }

        // do GET
        HttpRequest httpRequest = new HttpRequest(
                Constants.QUEUES_URL_PARAM + queueName + Constants.CONSUMERS_URL_PARAM + consumerId);
        HttpResponse response = httpClient.sendHttpRequest(httpRequest, HTTP_GET);

        // handle data processing
        ResponseFormatter responseFormatter = new TableFormatter();

        if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
            Gson gson = new Gson();
            Consumer[] consumers;
            if (consumerId.isEmpty()) {
                consumers = gson.fromJson(response.getPayload(), Consumer[].class);
            } else {
                consumers = new Consumer[] { gson.fromJson(response.getPayload(), Consumer.class) };
            }
            responseFormatter.printConsumers(consumers);
        } else {
            ResponseFormatter.handleErrorResponse(buildResponseMessage(response, BROKER_ERROR_MSG));
        }

    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  " + rootCommand + " list consumer [queue-name] [flag]*\n");
    }
}
