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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.ballerina.messaging.broker.client.cmd.AbstractCmd;
import io.ballerina.messaging.broker.client.http.HttpClient;
import io.ballerina.messaging.broker.client.http.HttpRequest;
import io.ballerina.messaging.broker.client.http.HttpResponse;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.resources.Exchange;
import io.ballerina.messaging.broker.client.utils.BrokerClientException;
import io.ballerina.messaging.broker.client.utils.Utils;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * Command representing MB exchange creation.
 */
@Parameters(commandDescription = "Create MB exchange")
public class CreateExchangeCmd extends CreateCmd {

    @Parameter(description = "name of the exchange")
    private String exchangeName;

    @Parameter(names = { "--type", "-t" },
               description = "type of the exchange")
    private String type = "direct";

    @Parameter(names = { "--durable", "-d" },
               description = "durability of the exchange")
    private boolean durable = false;

    @Override
    public void execute() {
        if (help) {
            processHelpLogs();
            return;
        }

        Configuration configuration = Utils.readConfigurationFile();
        HttpClient httpClient = new HttpClient(configuration);
        String urlSuffix = "exchanges/";

        Exchange exchange = new Exchange(exchangeName, type, durable);

        // do POST
        HttpResponse response = httpClient
                .sendHttpRequest(new HttpRequest(urlSuffix, exchange.getAsJsonString()), "POST");

        // handle response
        try {
            JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);
            AbstractCmd.OUT_STREAM.println(jsonParser.parse(response.getPayload()).toString());
        } catch (ParseException e) {
            BrokerClientException parseException = new BrokerClientException();
            parseException.addMessage("error while parsing broker response for exchange creation" + e.getMessage());
            throw parseException;
        }
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Create an exchange in MB with parameters\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  mb create exchange [exchange-name] [options]*\n");
        out.append("Example:\n");
        out.append("* Create a durable direct Exchange in MB.\n");
        out.append("  mb create exchange myExchange -t direct -d\n");
    }
}
