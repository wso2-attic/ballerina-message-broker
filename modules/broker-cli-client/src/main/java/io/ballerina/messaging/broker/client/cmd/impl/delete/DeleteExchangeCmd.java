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
package io.ballerina.messaging.broker.client.cmd.impl.delete;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.ballerina.messaging.broker.client.http.HttpClient;
import io.ballerina.messaging.broker.client.http.HttpRequest;
import io.ballerina.messaging.broker.client.http.HttpResponse;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.utils.BrokerClientException;
import io.ballerina.messaging.broker.client.utils.Utils;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * Command representing MB exchange deletion.
 */
@Parameters(commandDescription = "Delete MB exchange")
public class DeleteExchangeCmd extends DeleteCmd {

    @Parameter(description = "name of the exchange",
               required = true)
    private String exchangeName;

    @Parameter(names = { "--unused", "-u" },
               description = "delete only if the exchange is not in use")
    private boolean ifUnused = false;

    @Override
    public void execute() {
        if (help) {
            processHelpLogs();
            return;
        }

        Configuration configuration = Utils.readConfigurationFile();
        HttpClient httpClient = new HttpClient(configuration);
        String urlSuffix = "exchanges/";
        HttpRequest httpRequest = new HttpRequest(urlSuffix + exchangeName);

        if (ifUnused) {
            httpRequest.setQueryParameters("?ifUnused=true");
        }

        // do DELETE
        HttpResponse response = httpClient.sendHttpRequest(httpRequest, "DELETE");

        // handle response
        try {
            JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            OUT_STREAM.println(jsonParser.parse(response.getPayload()).toString());
        } catch (ParseException e) {
            BrokerClientException parseException = new BrokerClientException();
            parseException.addMessage("error while parsing broker response for exchange deletion" + e.getMessage());
            throw parseException;
        }

    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Delete an exchange in MB\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  mb delete exchange [exchange-name] [flag]*\n");
        out.append("Example:\n");
        out.append("* Delete exchange named 'myExchange' in MB, only if unused.\n");
        out.append("  mb delete exchange myExchange -u\n");
    }
}
