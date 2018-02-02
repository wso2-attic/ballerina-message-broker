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

package org.wso2.broker.core.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.common.ResourceNotFoundException;
import org.wso2.broker.common.ValidationException;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.QueueHandler;
import org.wso2.broker.core.rest.model.QueueCreateRequest;
import org.wso2.broker.core.rest.model.QueueCreateResponse;
import org.wso2.broker.core.rest.model.QueueMetadata;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

/**
 * Delegate class that handles Queues REST API requests.
 */
public class QueuesApiDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueuesApiDelegate.class);

    public static final String QUEUES_API_PATH = "/queues";

    private final Broker broker;

    public QueuesApiDelegate(Broker broker) {
        this.broker = broker;
    }

    public Response createQueue(QueueCreateRequest requestBody) {
        try {
            if (broker.createQueue(requestBody.getName(), false,
                                   requestBody.isDurable(), requestBody.isAutoDelete())) {
                QueueCreateResponse message = new QueueCreateResponse().message("Queue created.");
                return Response.created(new URI(BrokerAdminService.API_BASE_PATH + QUEUES_API_PATH
                                                        + "/" + requestBody.getName()))
                               .entity(message).build();
            } else {
                throw new BadRequestException("Queue already exists.");
            }
        } catch (BrokerException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (URISyntaxException e) {
            LOGGER.error("Error occurred while generating location URI ", e);
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    public Response deleteQueue(String queueName, Boolean ifUnused, Boolean ifEmpty) {
        if (Objects.isNull(ifUnused)) {
            ifUnused = true;
        }

        if (Objects.isNull(ifEmpty)) {
            ifEmpty = true;
        }

        try {
            broker.deleteQueue(queueName, ifUnused, ifEmpty);
            return Response.ok().build();
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (BrokerException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        } catch (ResourceNotFoundException e) {
            throw new NotFoundException("Queue " + queueName + " doesn't exist.", e);
        }
    }

    public Response getQueue(String queueName) {
        QueueHandler queueHandler = broker.getQueue(queueName);

        if (Objects.isNull(queueHandler)) {
            throw new NotFoundException("Queue " + queueName + " not found");
        }

        QueueMetadata queueMetadata = toQueueMetadata(queueHandler);
        return Response.ok().entity(queueMetadata).build();
    }

    public Response getAllQueues(Boolean durable) {
        boolean filterByDurability = Objects.nonNull(durable);
        Collection<QueueHandler> queueHandlers = broker.getAllQueues();
        List<QueueMetadata> queueArray = new ArrayList<>(queueHandlers.size());
        for (QueueHandler handler : queueHandlers) {
            // Add if filter is not set or durability equals to filer value.
            if (!filterByDurability || durable == handler.getQueue().isDurable()) {
                queueArray.add(toQueueMetadata(handler));
            }
        }
        return Response.ok().entity(queueArray).build();
    }

    private QueueMetadata toQueueMetadata(QueueHandler queueHandler) {
        return new QueueMetadata()
                .name(queueHandler.getQueue().getName())
                .durable(queueHandler.getQueue().isDurable())
                .autoDelete(queueHandler.getQueue().isAutoDelete())
                .capacity(queueHandler.getQueue().capacity())
                .consumerCount(queueHandler.consumerCount())
                .size(queueHandler.size());
    }
}
