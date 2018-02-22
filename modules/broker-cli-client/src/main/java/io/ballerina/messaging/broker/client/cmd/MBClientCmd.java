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
package io.ballerina.messaging.broker.client.cmd;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.ballerina.messaging.broker.client.http.HttpResponse;
import io.ballerina.messaging.broker.client.resources.Message;

import java.util.Objects;

/**
 * Interface to represent API for any command to be declared.
 */
public interface MBClientCmd {

    /**
     * This method is commonly used by implementations of this interface. This method will extract response message,
     * from the HttpResponse and create a new {@link Message} instance with it. If response message payload is empty,
     * the new {@link Message} will contain the default message.
     *
     * @param response httpResponse received from the Brokers REST backend.
     * @param defaultMessage default message to be set, in case of absence of the http message payload.
     *
     * @return newly created {@link Message} instance.
     */
    default Message buildResponseMessage(HttpResponse response, String defaultMessage) {
        Gson gson = new Gson();
        Message message;

        // try and see if the response is a json
        try {
            message = gson.fromJson(response.getPayload(), Message.class);
        } catch (JsonSyntaxException ex) {
            message = new Message(response.getPayload());
        }

        // if the message content is null, set the default message
        if (Objects.isNull(message) || Objects.isNull(message.getMessage())) {
            message = new Message(defaultMessage);
        }
        return message;
    }

    /**
     * Execution logic of the command.
     */
    void execute();

    /**
     * Append usage description of this command to the passed string builder.
     *
     * @param out StringBuilder instance, which messages should be appended to.
     */
    void appendUsage(StringBuilder out);

    /**
     * Store the {@link JCommander} instance related to this command itself. This can be used to print help statements,
     * on its child commands.
     *
     * @param jCommander JCommander instance generated for this Command
     */
    void setSelfJCommander(JCommander jCommander);
}
