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
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.ballerina.messaging.broker.client.http.HttpClient;
import io.ballerina.messaging.broker.client.http.HttpRequest;
import io.ballerina.messaging.broker.client.http.HttpResponse;
import io.ballerina.messaging.broker.client.output.ResponseFormatter;
import io.ballerina.messaging.broker.client.resources.Binding;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.utils.BrokerClientException;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.client.utils.Utils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.messaging.broker.client.utils.Constants.BROKER_ERROR_MSG;
import static io.ballerina.messaging.broker.client.utils.Constants.HTTP_GET;

/**
 * Command representing MB binding information retrieval.
 */
@Parameters(commandDescription = "List Binding(s) in the Broker for a queue or an exchange")
public class ListBindingCmd extends ListCmd {

    @Parameter(names = { "--exchange", "-e" },
               description = "name of the exchange (only one from 'queue' name and 'exchange' name should be provided)")
    private String exchangeName = "";

    @Parameter(names = { "--queue", "-q" },
               description = "name of the queue (only one from 'queue' name and 'exchange' name should be provided)")
    private String queueName = "";

    public ListBindingCmd(String rootCommand) {
        super(rootCommand);
    }

    @Override
    public void execute() {
        if (help) {
            processHelpLogs();
            return;
        }

        if ((!queueName.isEmpty() && !exchangeName.isEmpty()) || (queueName.isEmpty() && exchangeName.isEmpty())) {
            BrokerClientException exception = new BrokerClientException();
            exception.addMessage("either one from 'queue' name and 'exchange' name should only be present");
            throw exception;
        }

        // todo: remove this check after the implementation
        if (!queueName.isEmpty()) {
            BrokerClientException exception = new BrokerClientException();
            exception.addMessage("listing bindings of a particular queue is not supported yet");
            throw exception;
        }

        Configuration configuration = Utils.getConfiguration(password);
        HttpClient httpClient = new HttpClient(configuration);
        String urlParamType = !exchangeName.isEmpty() ? Constants.EXCHANGES_URL_PARAM : Constants.QUEUES_URL_PARAM;
        String urlParamName = !exchangeName.isEmpty() ? exchangeName : queueName;

        // do GET
        HttpRequest httpRequest = new HttpRequest(urlParamType + urlParamName + Constants.BINDINGS_URL_PARAM);
        HttpResponse response = httpClient.sendHttpRequest(httpRequest, HTTP_GET);

        if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
            if (!exchangeName.isEmpty()) {
                Binding[] bindings = processExchangeResponse(response.getPayload(), exchangeName);
                responseFormatter.printExchangeBindings(bindings);
            }
        } else {
            ResponseFormatter.handleErrorResponse(buildResponseMessage(response, BROKER_ERROR_MSG));
        }

    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  " + rootCommand + " list binding (--exchange|--queue) [resource-name] [flag]*\n");
    }

    /**
     * Parse the response payload.
     *
     * @param payload response payload.
     * @param exchangeName name of the exchange given at the command.
     * @return array of binding objects.
     */
    private Binding[] processExchangeResponse(String payload, String exchangeName) {
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(payload).getAsJsonArray();
        List<Binding> bindings = new ArrayList<>();

        json.forEach(jsonElement -> {
                    String bindingPatten = jsonElement.getAsJsonObject().get(Binding.BINDING_PATTERN).getAsString();
                    JsonArray internalArray = jsonElement.getAsJsonObject().getAsJsonArray("bindings");
                    internalArray.forEach(resource -> {
                        String bindQueueName = resource.getAsJsonObject().get(Binding.QUEUE_NAME).getAsString();
                        bindings.add(new Binding(bindQueueName, bindingPatten, exchangeName, ""));
                    });
                }
        );
        return bindings.toArray(new Binding[bindings.size()]);
    }
}
