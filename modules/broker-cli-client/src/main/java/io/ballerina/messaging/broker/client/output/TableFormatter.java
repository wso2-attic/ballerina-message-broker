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
import io.ballerina.messaging.broker.client.resources.Queue;

import java.util.Arrays;

/**
 * Print backend responses into tables. This is used for displaying results of 'list' commands.
 */
public class TableFormatter implements ResponseFormatter {

    public static final int TABLE_PADDING = 2;

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

        int maxColumnSize = Math.max(maxExchangeNameLength, Exchange.NAME.length());

        String printTemplate = "%-" + String.valueOf(maxColumnSize + TABLE_PADDING) + "s%-10s%-10s\n";

        OUT_STREAM.printf(printTemplate, Exchange.NAME, Exchange.TYPE, Exchange.DURABLE);
        for (Exchange exchange : exchanges) {
            OUT_STREAM.printf(printTemplate, exchange.getName(), exchange.getType(),
                    String.valueOf(exchange.isDurable()));
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

        int maxColumnSize = Math.max(maxQueueNameLength, Queue.NAME.length());

        String printTemplate = "%-" + String.valueOf(maxColumnSize + TABLE_PADDING) + "s%-15s%-15s%-10s%-10s%-10s\n";

        OUT_STREAM.printf(printTemplate, Queue.NAME, Queue.CONSUMER_COUNT, Queue.CAPACITY, Queue.SIZE, Queue.DURABLE,
                Queue.AUTO_DELETE);
        for (Queue queue : queues) {
            OUT_STREAM.printf(printTemplate, queue.getName(), String.valueOf(queue.getConsumerCount()),
                    String.valueOf(queue.getCapacity()), String.valueOf(queue.getSize()),
                    String.valueOf(queue.isDurable()), String.valueOf(queue.isAutoDelete()));
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

        String printTemplate = "%-" + String.valueOf(maxColumnSize + TABLE_PADDING) + "s%s\n";

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

        String printTemplate = "%-" + String.valueOf(maxColumnSize + TABLE_PADDING) + "s%-12s%s\n";

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
