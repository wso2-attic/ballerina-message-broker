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
package io.ballerina.messaging.broker.client.cmd.impl.delete;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.messaging.broker.client.http.HttpClient;
import io.ballerina.messaging.broker.client.http.HttpRequest;
import io.ballerina.messaging.broker.client.http.HttpResponse;
import io.ballerina.messaging.broker.client.output.ResponseFormatter;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.resources.Message;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.client.utils.Utils;
import java.net.HttpURLConnection;
import java.util.Objects;

/**
 * Command representing message deletion of a queue(purge) in MB.
 */
@Parameters(commandDescription = "Delete messages in a queue in the Broker")
public class DeleteMessagesCmd extends DeleteCmd {

    @Parameter(names = {Constants.QUEUE_FLAG, "-q"},
            description = "name of the queue",
            required = true)
    private String queueName;

    public DeleteMessagesCmd(String rootCommand) {
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
        HttpRequest httpRequest = new HttpRequest(Constants.QUEUES_URL_PARAM + queueName
                                                  + Constants.MESSAGES_URL_PARAM);

        // do DELETE
        HttpResponse response = httpClient.sendHttpRequest(httpRequest, Constants.HTTP_DELETE);

        // handle response
        if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
            Message message = buildResponseMessage(response, "Messages deleted successfully.");
            ResponseFormatter.printMessage(message);
        } else {
            ResponseFormatter.handleErrorResponse(buildResponseMessage(response, Constants.BROKER_ERROR_MSG));
        }

    }

    @Override
    public Message buildResponseMessage(HttpResponse response, String defaultMessage) {
        Gson gson = new Gson();
        Message message;

        JsonElement numberOfMessagesElement = gson.fromJson(response.getPayload(), JsonObject.class).get
                ("numberOfMessagesDeleted");
        if (Objects.nonNull(numberOfMessagesElement)) {
            int numberOfMessagesDeleted = numberOfMessagesElement.getAsInt();
            message = new Message(defaultMessage + " Number of messages deleted=" + numberOfMessagesDeleted);
        } else {
            message = super.buildResponseMessage(response, defaultMessage);
        }
        return message;
    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n").append("  ").append(rootCommand).append(" delete messages [flag]*\n");
    }
}
