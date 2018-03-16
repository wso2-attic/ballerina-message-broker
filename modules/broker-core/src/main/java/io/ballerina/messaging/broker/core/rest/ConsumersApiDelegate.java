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

package io.ballerina.messaging.broker.core.rest;

import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.core.BrokerAuthException;
import io.ballerina.messaging.broker.core.BrokerAuthNotFoundException;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.BrokerFactory;
import io.ballerina.messaging.broker.core.Consumer;
import io.ballerina.messaging.broker.core.QueueHandler;
import io.ballerina.messaging.broker.core.rest.model.ConsumerMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.security.auth.Subject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

/**
 * Delegate class to handle {@link Consumer} related REST requests.
 */
public class ConsumersApiDelegate {

    private final BrokerFactory brokerFactory;

    public ConsumersApiDelegate(BrokerFactory brokerFactory) {
        this.brokerFactory = brokerFactory;
    }

    public Response getConsumer(String queueName, Integer consumerId, Subject subject) {
        QueueHandler queue;
        try {
            queue = brokerFactory.getBroker(subject).getQueue(queueName);
        } catch (BrokerAuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        } catch (BrokerAuthNotFoundException | ResourceNotFoundException e) {
            throw new NotFoundException("Unknown queue name " + queueName);
        } catch (BrokerException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }

        Consumer matchingConsumer = null;
        for (Consumer consumer : queue.getConsumers()) {
            if (consumer.getId() == consumerId) {
                matchingConsumer = consumer;
                break;
            }
        }
        if (Objects.nonNull(matchingConsumer)) {
            return Response.ok().entity(toConsumerMetadata(matchingConsumer)).build();
        } else {
            throw new NotFoundException("Consumer with id " + consumerId + " for queue " + queueName + " not found.");
        }
    }

    public Response getAllConsumers(String queueName, Subject subject) {
        QueueHandler queueHandler;
        try {
            queueHandler = brokerFactory.getBroker(subject).getQueue(queueName);
        } catch (BrokerAuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        } catch (BrokerAuthNotFoundException | ResourceNotFoundException e) {
            throw new NotFoundException("Queue " + queueName + " doesn't exist.", e);
        } catch (BrokerException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
        if (Objects.isNull(queueHandler)) {
            throw new NotFoundException("Unknown queue Name " + queueName);
        }

        Collection<Consumer> consumers = queueHandler.getConsumers();
        List<ConsumerMetadata> consumerMetadataList = new ArrayList<>(consumers.size());
        for (Consumer consumer : consumers) {
            consumerMetadataList.add(toConsumerMetadata(consumer));
        }
        return Response.ok().entity(consumerMetadataList).build();
    }

    private ConsumerMetadata toConsumerMetadata(Consumer consumer) {
        return new ConsumerMetadata()
                .id(consumer.getId())
                .isExclusive(consumer.isExclusive())
                .flowEnabled(consumer.isReady());
    }

    public Response deleteConsumer(String queueName, Integer consumerId, Subject subject) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
}
