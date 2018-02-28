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
import io.ballerina.messaging.broker.client.resources.Binding;
import io.ballerina.messaging.broker.client.utils.Constants;

import java.util.Objects;

/**
 * Command representing MB binding creation.
 */
@Parameters(commandDescription = "Create a binding in the Broker with parameters")
public class CreateBindingCmd extends CreateCmd {

    @Parameter(description = "binding key (queue name is used if not present)")
    private String bindingPattern = null;

    @Parameter(names = { "--exchange", "-e" },
               description = "name of the exchange",
               required = true)
    private String exchangeName;

    @Parameter(names = { "--queue", "-q" },
               description = "name of the queue",
               required = true)
    private String queueName;

    @Parameter(names = { "--filter", "-f" },
               description = "filter expression")
    private String filterExpression = "";

    public CreateBindingCmd(String rootCommand) {
        super(rootCommand, "Binding created successfully");
    }

    @Override
    public void execute() {
        if (help) {
            processHelpLogs();
            return;
        }

        if (Objects.isNull(bindingPattern)) {
            bindingPattern = queueName;
        }

        Binding binding = new Binding(queueName, bindingPattern, exchangeName, filterExpression);

        performResourceCreationOverHttp(
                Constants.QUEUES_URL_PARAM + binding.getQueueName() + Constants.BINDINGS_URL_PARAM,
                binding.getAsJsonString());

    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  " + rootCommand
                + " create binding [binding-pattern]? -e [exchange-name] -q [queue-name] [other-flags]*\n");
    }
}
