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

/**
 * Print backend responses in csv format.
 */
public class CsvFormatter implements ResponseFormatter {

    private static final String WRAPPED_STRING_FORMATTER = "\"%s\"";
    /**
     * Name of this formatter class. This will be used when displaying help logs.
     */
    private static final String FORMATTER_NAME = "csv";

    @Override
    public void printExchanges(Exchange[] exchanges) {
        if (exchanges.length == 0) {
            return;
        }
        String printTemplate = "%s,%s,%s,%s%n";
        OUT_STREAM.printf(printTemplate, Exchange.NAME_TAG, Exchange.TYPE_TAG,
                          Exchange.DURABLE_TAG, Exchange.OWNER_TAG);
        for (Exchange exchange : exchanges) {
            OUT_STREAM.printf(printTemplate.replaceFirst("%s", WRAPPED_STRING_FORMATTER), exchange.getName(),
                    exchange.getType(), String.valueOf(exchange.isDurable()), exchange.getOwner());
        }
    }

    @Override
    public void printExchange(Exchange exchange) {
        printExchanges(new Exchange[] { exchange });
    }

    @Override
    public void printQueues(Queue[] queues) {
        if (queues.length == 0) {
            return;
        }
        String printTemplate = "%s,%s,%s,%s,%s,%s,%s%n";
        OUT_STREAM.printf(printTemplate, Queue.NAME_TAG, Queue.CONSUMER_COUNT_TAG, Queue.CAPACITY_TAG,
                          Queue.SIZE_TAG, Queue.DURABLE_TAG, Queue.AUTO_DELETE_TAG, Queue.OWNER_TAG);
        for (Queue queue : queues) {
            OUT_STREAM.printf(printTemplate.replaceFirst("%s", WRAPPED_STRING_FORMATTER), queue.getName(),
                    String.valueOf(queue.getConsumerCount()), String.valueOf(queue.getCapacity()),
                    String.valueOf(queue.getSize()), String.valueOf(queue.isDurable()),
                    String.valueOf(queue.isAutoDelete()), queue.getOwner());
        }
    }

    @Override
    public void printQueue(Queue queues) {
        printQueues(new Queue[] {queues});
    }

    @Override
    public void printExchangeBindings(Binding[] bindings) {
        if (bindings.length == 0) {
            return;
        }
        String printTemplate = "%s,%s%n";
        OUT_STREAM.printf(printTemplate, Binding.QUEUE_NAME, Binding.BINDING_PATTERN);
        for (Binding binding : bindings) {
            OUT_STREAM.printf(printTemplate.replace("%s", WRAPPED_STRING_FORMATTER), binding.getQueueName(),
                    binding.getBindingPattern());
        }
    }

    @Override
    public void printConsumers(Consumer[] consumers) {
        if (consumers.length == 0) {
            return;
        }
        String printTemplate = "%s,%s,%s%n";
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
