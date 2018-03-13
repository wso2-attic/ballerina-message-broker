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

import io.ballerina.messaging.broker.auth.authorization.Authorizer;
import io.ballerina.messaging.broker.auth.authorization.authorizer.rdbms.resource.AuthResource;
import io.ballerina.messaging.broker.auth.authorization.enums.ResourceType;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthNotFoundException;
import io.ballerina.messaging.broker.auth.exception.BrokerAuthServerException;
import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.BrokerFactory;
import io.ballerina.messaging.broker.core.QueueHandler;
import io.ballerina.messaging.broker.core.rest.model.ActionUserGroupsMapping;
import io.ballerina.messaging.broker.core.rest.model.MessageDeleteResponse;
import io.ballerina.messaging.broker.core.rest.model.QueueCreateRequest;
import io.ballerina.messaging.broker.core.rest.model.QueueCreateResponse;
import io.ballerina.messaging.broker.core.rest.model.QueueMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.security.auth.Subject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

/**
 * Delegate class that handles Queues REST API requests.
 */
public class QueuesApiDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueuesApiDelegate.class);

    public static final String QUEUES_API_PATH = "/queues";

    private final BrokerFactory brokerFactory;

    private final Authorizer authorizer;

    public QueuesApiDelegate(BrokerFactory brokerFactory, Authorizer authorizer) {
        this.brokerFactory = brokerFactory;
        this.authorizer = authorizer;
    }

    public Response createQueue(QueueCreateRequest requestBody, Subject subject) {
        try {
            if (brokerFactory.getBroker(subject).createQueue(requestBody.getName(), false,
                                   requestBody.isDurable(), requestBody.isAutoDelete())) {
                QueueCreateResponse message = new QueueCreateResponse().message("Queue created.");
                return Response.created(new URI(BrokerAdminService.API_BASE_PATH + QUEUES_API_PATH
                                                        + "/" + requestBody.getName()))
                               .entity(message).build();
            } else {
                throw new BadRequestException("Queue already exists.");
            }
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (BrokerException | URISyntaxException e) {
            LOGGER.error("Error occurred while generating location URI ", e);
            throw new InternalServerErrorException(e.getMessage(), e);
        } catch (BrokerAuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        }
    }

    public Response deleteQueue(String queueName, Boolean ifUnused, Boolean ifEmpty, Subject subject) {
        if (Objects.isNull(ifUnused)) {
            ifUnused = true;
        }

        if (Objects.isNull(ifEmpty)) {
            ifEmpty = true;
        }

        try {
            int numberOfMessagesDeleted = brokerFactory.getBroker(subject).deleteQueue(queueName, ifUnused, ifEmpty);
            return Response.ok()
                           .entity(new MessageDeleteResponse().numberOfMessagesDeleted(numberOfMessagesDeleted))
                           .build();
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (BrokerException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        } catch (ResourceNotFoundException e) {
            throw new NotFoundException("Queue " + queueName + " doesn't exist.", e);
        } catch (BrokerAuthException | BrokerAuthNotFoundException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        }
    }

    public Response getQueue(String queueName, Subject subject) {
        QueueHandler queueHandler;
        try {
            queueHandler = brokerFactory.getBroker(subject).getQueue(queueName);
        } catch (BrokerAuthException | BrokerAuthNotFoundException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        }

        if (Objects.isNull(queueHandler)) {
            throw new NotFoundException("Queue " + queueName + " not found");
        }

        QueueMetadata queueMetadata = toQueueMetadata(queueHandler);
        return Response.ok().entity(queueMetadata).build();
    }

    public Response getAllQueues(Boolean durable, Subject subject) {
        boolean filterByDurability = Objects.nonNull(durable);
        Collection<QueueHandler> queueHandlers;
        try {
            queueHandlers = brokerFactory.getBroker(subject).getAllQueues();
        } catch (BrokerAuthException e) {
            throw new NotAuthorizedException(e.getMessage(), e);
        }
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
        QueueMetadata queueMetadata = new QueueMetadata();
        queueMetadata.name(queueHandler.getQueue().getName())
                .durable(queueHandler.getQueue().isDurable())
                .autoDelete(queueHandler.getQueue().isAutoDelete())
                .capacity(queueHandler.getQueue().capacity())
                .consumerCount(queueHandler.consumerCount())
                .size(queueHandler.size());
        AuthResource authResource = null;
        try {
            authResource = authorizer.getAuthResource(ResourceType.QUEUE.toString(),
                                                      queueHandler.getQueue().getName());
        } catch (BrokerAuthServerException | BrokerAuthNotFoundException e) {
            // TODO handle error correctly
            LOGGER.error("Error while querying auth resource", e);
        }
        if (Objects.nonNull(authResource)) {
            queueMetadata.owner(authResource.getOwner())
                    .permissions(toActionUserGroupsMapping(authResource.getActionsUserGroupsMap()));
        }
        return queueMetadata;
    }

    private ArrayList<ActionUserGroupsMapping> toActionUserGroupsMapping(
            Map<String, Set<String>> actionsUserGroupsMap) {

        ArrayList<ActionUserGroupsMapping> actionUserGroupsMappings = new ArrayList<>(actionsUserGroupsMap.size());
        actionsUserGroupsMap.forEach((action, userGroups) -> {
            ActionUserGroupsMapping actionUserGroupsMapping = new ActionUserGroupsMapping();
            actionUserGroupsMapping.setAction(action);
            actionUserGroupsMapping.setUserGroups(new ArrayList<>(userGroups));
        });
        return actionUserGroupsMappings;
    }

    public Response purgeQueue(String queueName, Subject subject) {
        try {
            int numberOfMessagesDeleted = brokerFactory.getBroker(subject).purgeQueue(queueName);
            return Response.ok()
                           .entity(new MessageDeleteResponse().numberOfMessagesDeleted(numberOfMessagesDeleted))
                           .build();
        } catch (ValidationException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (ResourceNotFoundException e) {
            throw new NotFoundException("Queue " + queueName + " doesn't exist.", e);
        }
    }
}
