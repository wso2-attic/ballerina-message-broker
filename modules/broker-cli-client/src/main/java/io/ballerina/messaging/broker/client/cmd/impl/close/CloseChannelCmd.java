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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.ballerina.messaging.broker.client.utils.Constants;

/**
 * Command representing forceful channel close.
 */
@Parameters(commandDescription = "Close a channel in the Broker with parameters")
public class CloseChannelCmd extends CloseCmd {

    @Parameter(names = {"--connection"},
               description = "Identifier of the connection for which the channel belongs to", required = true)
    private String connectionId = "";

    @Parameter(description = "Identifier of the channel to be closed", required = true)
    private String channelId = "";

    @Parameter(names = {Constants.IF_USED_FLAG},
            description = "If set to true, the connection will be closed from the broker without "
                          + "communicating with the amqp client")
    private boolean ifUsed = false;

    public CloseChannelCmd(String rootCommand) {
        super(rootCommand);
    }

    @Override
    public void execute() {
        executeClose(Constants.CONNECTIONS_URL_PARAM + connectionId + "/" + Constants.CHANNELS_URL_PARAM
                     + channelId + Constants.QUERY_PARAM_BEGINNING + Constants.USED_QUERY_PARAM + ifUsed,
                     "Channel close request submitted successfully");
    }

    @Override
    public void appendUsage(StringBuilder out) {
        appendUsage(out, "close channel [channel-id] --connection [connection-id]");
    }
}
