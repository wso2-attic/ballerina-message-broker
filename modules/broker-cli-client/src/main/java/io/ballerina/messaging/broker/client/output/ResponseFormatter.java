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
import io.ballerina.messaging.broker.client.resources.Message;
import io.ballerina.messaging.broker.client.resources.Queue;
import io.ballerina.messaging.broker.client.utils.BrokerClientException;

import java.io.PrintStream;

/**
 * Interface needs to be implemented by any output formatter. This has one method for each resource (response) type.
 */
public interface ResponseFormatter {

    PrintStream OUT_STREAM = System.out;

    /**
     * Handle error messages, create {@link BrokerClientException} and throw it.
     *
     * @param message Message containing the error.
     */
    static void handleErrorResponse(Message message) {
        BrokerClientException brokerException = new BrokerClientException();
        brokerException.addMessage(message.getMessage());
        throw brokerException;
    }

    /**
     * Print Message type responses. Broker REST service will send message type responses as an ack for several types
     * of requests.
     *
     * @param message Response message received from the REST service
     */
    static void printMessage(Message message) {
        OUT_STREAM.println(message.getMessage());
    }

    /**
     * Print an array of exchanges into a desired output format.
     *
     * @param exchanges array of exchanges
     */
    void printExchanges(Exchange[] exchanges);

    /**
     * Print an exchange into a desired output format.
     *
     * @param exchange array of exchanges
     */
    void printExchange(Exchange exchange);

    /**
     * Print an array of queues into a desired output format.
     *
     * @param queues array of exchanges
     */
    void printQueues(Queue[] queues);

    /**
     * Print a queue into a desired output format.
     *
     * @param queues array of exchanges
     */
    void printQueue(Queue queues);

    /**
     * Print an array of Bindings under a exchange into a desired output format.
     *
     * @param bindings array of bindings.
     */
    void printExchangeBindings(Binding[] bindings);

    /**
     * Print an array of Queue consumers into a desired output format.
     *
     * @param consumers array of consumers.
     */
    void printConsumers(Consumer[] consumers);
}
