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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.messaging.broker.client.output;

import io.ballerina.messaging.broker.client.resources.Binding;
import io.ballerina.messaging.broker.client.resources.Consumer;
import io.ballerina.messaging.broker.client.resources.Exchange;
import io.ballerina.messaging.broker.client.resources.Permission;
import io.ballerina.messaging.broker.client.resources.Queue;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Print backend responses into tables. This is used for displaying results of 'list' commands.
 */
public class TableFormatter implements ResponseFormatter {

    private static final int TABLE_PADDING = 2;

    /**
     * Name of this formatter class. This will be used when displaying help logs.
     */
    private static final String FORMATTER_NAME = "table";

    @Override
    public void printExchanges(Exchange[] exchanges) {
        if (exchanges.length == 0) {
            return;
        }
        int maxExchangeNameLength = Arrays.stream(exchanges)
                                          .mapToInt(exchange -> exchange.getName().length())
                                          .max()
                                          .getAsInt();

        int maxColumnSize = Math.max(maxExchangeNameLength, Exchange.NAME_TAG.length());

        String printTemplate = "%-" + (maxColumnSize + TABLE_PADDING) + "s%-10s%-10s%-10s\n";

        OUT_STREAM.printf(printTemplate, Exchange.NAME_TAG, Exchange.TYPE_TAG,
                          Exchange.DURABLE_TAG, Exchange.OWNER_TAG);
        for (Exchange exchange : exchanges) {
            OUT_STREAM.printf(printTemplate, exchange.getName(), exchange.getType(),
                              String.valueOf(exchange.isDurable()), exchange.getOwner());
        }
    }

    @Override
    public void printExchange(Exchange exchange) {
        int maxFieldLength = Exchange.DURABLE_TAG.length() + 1;
        String printTemplate = "%-" + maxFieldLength + "s: %s\n";

        OUT_STREAM.printf(printTemplate, Exchange.NAME_TAG, exchange.getName());
        OUT_STREAM.printf(printTemplate, Exchange.TYPE_TAG, exchange.getType());
        OUT_STREAM.printf(printTemplate, Queue.DURABLE_TAG, exchange.isDurable());
        OUT_STREAM.printf(printTemplate, Queue.OWNER_TAG, exchange.getOwner());

        OUT_STREAM.println("\nPermissions");
        OUT_STREAM.println("===========");

        for (Permission permission : exchange.getPermissions()) {
            String permissionsList = permission.getUserGroups()
                                               .stream()
                                               .map(String::toString)
                                               .collect(Collectors.joining(","));
            OUT_STREAM.println(permission.getAction() + ": " + permissionsList);

        }
    }

    @Override
    public void printQueues(Queue[] queues) {
        if (queues.length == 0) {
            return;
        }
        int maxQueueNameLength = Arrays.stream(queues)
                                       .mapToInt(queue -> queue.getName().length())
                                       .max()
                                       .getAsInt();

        int maxColumnSize = Math.max(maxQueueNameLength, Queue.NAME_TAG.length());

        String printTemplate = "%-" + (maxColumnSize + TABLE_PADDING) + "s%-15s%-15s%-10s%-10s%-12s%-10s\n";

        OUT_STREAM.printf(printTemplate, Queue.NAME_TAG, Queue.CONSUMER_COUNT_TAG, Queue.CAPACITY_TAG,
                          Queue.SIZE_TAG, Queue.DURABLE_TAG, Queue.AUTO_DELETE_TAG, Queue.OWNER_TAG);
        for (Queue queue : queues) {
            OUT_STREAM.printf(printTemplate, queue.getName(), String.valueOf(queue.getConsumerCount()),
                              String.valueOf(queue.getCapacity()), String.valueOf(queue.getSize()),
                              String.valueOf(queue.isDurable()), String.valueOf(queue.isAutoDelete()),
                              queue.getOwner());
        }
    }

    @Override
    public void printQueue(Queue queue) {
        int maxFieldLength = Queue.CONSUMER_COUNT_TAG.length() + 1;
        String printTemplate = "%-" + maxFieldLength + "s: %s\n";

        OUT_STREAM.printf(printTemplate, Queue.NAME_TAG, queue.getName());
        OUT_STREAM.printf(printTemplate, Queue.CONSUMER_COUNT_TAG, queue.getConsumerCount());
        OUT_STREAM.printf(printTemplate, Queue.CAPACITY_TAG, queue.getCapacity());
        OUT_STREAM.printf(printTemplate, Queue.SIZE_TAG, queue.getSize());
        OUT_STREAM.printf(printTemplate, Queue.DURABLE_TAG, queue.isDurable());
        OUT_STREAM.printf(printTemplate, Queue.AUTO_DELETE_TAG, queue.isAutoDelete());
        OUT_STREAM.printf(printTemplate, Queue.OWNER_TAG, queue.getOwner());

        OUT_STREAM.println("\nPermissions");
        OUT_STREAM.println("===========");

        for (Permission permission : queue.getPermissions()) {
            String permissionsList = permission.getUserGroups()
                                               .stream()
                                               .map(String::toString)
                                               .collect(Collectors.joining(","));
            OUT_STREAM.println(permission.getAction() + ": " + permissionsList);

        }
    }

    @Override
    public void printExchangeBindings(Binding[] bindings) {
        if (bindings.length == 0) {
            return;
        }
        int maxQueueNameLength = Arrays.stream(bindings)
                                       .mapToInt(binding -> binding.getQueueName().length())
                                       .max()
                                       .getAsInt();

        int maxColumnSize = Math.max(maxQueueNameLength, Binding.QUEUE_NAME.length());

        String printTemplate = "%-" + (maxColumnSize + TABLE_PADDING) + "s%s\n";

        OUT_STREAM.printf(printTemplate, Binding.QUEUE_NAME, Binding.BINDING_PATTERN);
        for (Binding binding : bindings) {
            OUT_STREAM.printf(printTemplate, binding.getQueueName(), binding.getBindingPattern());
        }
    }

    @Override
    public void printConsumers(Consumer[] consumers) {
        if (consumers.length == 0) {
            return;
        }
        int maxIdLength = Arrays.stream(consumers)
                                .mapToInt(consumer -> String.valueOf(consumer.getId()).length())
                                .max()
                                .getAsInt();

        int maxColumnSize = Math.max(maxIdLength, Consumer.CONSUMER_ID.length());

        String printTemplate = "%-" + (maxColumnSize + TABLE_PADDING) + "s%-12s%s\n";

        OUT_STREAM.printf(printTemplate, Consumer.CONSUMER_ID, Consumer.IS_EXCLUSIVE, Consumer.FLOW_ENABLED);
        for (Consumer consumer : consumers) {
            OUT_STREAM.printf(printTemplate, consumer.getId(), consumer.isExclusive(), consumer.isFlowEnabled());
        }
    }

    @Override
    public String toString() {
        return FORMATTER_NAME;
    }
}
