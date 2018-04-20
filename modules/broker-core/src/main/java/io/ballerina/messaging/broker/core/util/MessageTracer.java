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

package io.ballerina.messaging.broker.core.util;

import io.ballerina.messaging.broker.core.Consumer;
import io.ballerina.messaging.broker.core.DetachableMessage;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Metadata;
import io.ballerina.messaging.broker.core.QueueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.transaction.xa.Xid;

/**
 * Trace messages flowing through the broker.
 */
public final class MessageTracer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageTracer.class);

    public static final String NO_ROUTES = "No routes for message. Message dropped!";
    public static final String UNKNOWN_EXCHANGE = "Unknown exchange. Message dropped!";
    public static final String PUBLISH_FAILURE = "Failed to enqueue. Message dropped!";
    public static final String PUBLISH_SUCCESSFUL = "Successfully enqueued message to queue.";
    public static final String RETRIEVE_FOR_REDELIVERY = "Dequeue message for re-delivery.";
    public static final String RETRIEVE_FOR_DELIVERY = "Dequeue message for delivery.";
    public static final String REQUEUE = "Requeue message.";
    public static final String ACKNOWLEDGE = "Acknowledge message.";
    public static final String DELIVER = "Deliver message to transport consumer.";
    public static final String PREPARE_ENQUEUE = "Prepare to enqueue message.";
    public static final String PREPARE_DEQUEUE = "Prepare dequeue message event.";
    public static final String COMMIT = "Transaction commit event.";
    public static final String ROLLBACK = "Transaction rollback event.";
    public static final String PREPARED = "Transaction prepared.";
    public static final String QUEUE_COMMIT = "Committed prepared events on queue.";
    public static final String QUEUE_ROLLBACK = "Rollbacked prepared events on queue.";

    private MessageTracer() {
    }

    public static void trace(Message message, String description) {
        if (LOGGER.isTraceEnabled() && Objects.nonNull(message)) {
            TraceBuilder traceBuilder = getTraceBuilder(message);
            LOGGER.trace(traceBuilder.buildTrace(description));
        }
    }

    public static void trace(Message message, QueueHandler queueHandler, String description) {
        if (LOGGER.isTraceEnabled() && Objects.nonNull(message) && Objects.nonNull(queueHandler)) {
            TraceBuilder traceBuilder = getTraceBuilder(message, queueHandler);
            LOGGER.trace(traceBuilder.buildTrace(description));
        }
    }

    public static void trace(Message message, Consumer consumer, String description,
                             List<TraceField> properties) {
        if (LOGGER.isTraceEnabled() && Objects.nonNull(message) && Objects.nonNull(consumer)) {
            Metadata metadata = message.getMetadata();
            String queueName = consumer.getQueueName();
            int id = consumer.getId();
            TraceBuilder traceBuilder = new TraceBuilder().internalId(message.getInternalId())
                                                          .queueName(queueName)
                                                          .consumerId(id)
                                                          .routingKey(metadata.getRoutingKey())
                                                          .fieldList(properties);
            LOGGER.trace(traceBuilder.buildTrace(description));
        }
    }

    public static void trace(Message message, String description, TraceField... traceFields) {
        if (LOGGER.isTraceEnabled() && Objects.nonNull(message)) {
            Metadata metadata = message.getMetadata();
            TraceBuilder traceBuilder = new TraceBuilder().internalId(message.getInternalId())
                                                          .routingKey(metadata.getRoutingKey())
                                                          .exchangeName(metadata.getExchangeName())
                                                          .fieldList(Arrays.asList(traceFields));
            LOGGER.trace(traceBuilder.buildTrace(description));
        }

    }

    public static void trace(Message message, String description, List<TraceField> traceFields) {
        if (LOGGER.isTraceEnabled() && Objects.nonNull(message)) {
            TraceBuilder traceBuilder = new TraceBuilder().internalId(message.getInternalId())
                                                          .routingKey(message.getMetadata().getRoutingKey());

            for (TraceField traceField : traceFields) {
                traceBuilder.field(traceField);
            }
            LOGGER.trace(traceBuilder.buildTrace(description));
        }
    }

    public static void trace(String description, TraceField... traceFields) {
        if (LOGGER.isTraceEnabled()) {
            TraceBuilder traceBuilder = new TraceBuilder();
            for (TraceField traceField : traceFields) {
                traceBuilder.field(traceField);
            }
            LOGGER.trace(traceBuilder.buildTrace(description));
        }
    }

    public static boolean isTraceEnabled() {
        return LOGGER.isTraceEnabled();
    }

    public static void trace(Message message, Xid xid, String description) {
        if (LOGGER.isDebugEnabled() && Objects.nonNull(message)) {
            TraceBuilder traceBuilder = getTraceBuilder(message);
            traceBuilder.xid(xid);
            LOGGER.trace(traceBuilder.buildTrace(description));
        }
    }

    private static TraceBuilder getTraceBuilder(Message message) {
        return new TraceBuilder().internalId(message.getInternalId())
                                 .routingKey(message.getMetadata().getRoutingKey())
                                 .exchangeName(message.getMetadata().getExchangeName());
    }

    public static void trace(Message message, Xid xid, QueueHandler queueHandler, String description) {
        if (LOGGER.isTraceEnabled() && Objects.nonNull(message) && Objects.nonNull(queueHandler)) {
            TraceBuilder traceBuilder = getTraceBuilder(message, queueHandler);
            traceBuilder.xid(xid);
            LOGGER.trace(traceBuilder.buildTrace(description));
        }
    }

    public static void trace(DetachableMessage detachableMessage, QueueHandler queueHandler, String description) {
        if (LOGGER.isTraceEnabled() && Objects.nonNull(queueHandler)) {
            TraceBuilder traceBuilder = getTraceBuilder(detachableMessage, queueHandler);
            LOGGER.trace(traceBuilder.buildTrace(description));
        }
    }

    public static void trace(DetachableMessage detachableMessage, Xid xid,
                             QueueHandler queueHandler, String description) {
        if (LOGGER.isTraceEnabled() && Objects.nonNull(queueHandler)) {
            TraceBuilder traceBuilder = getTraceBuilder(detachableMessage, queueHandler);
            traceBuilder.xid(xid);
            LOGGER.trace(traceBuilder.buildTrace(description));
        }
    }

    private static TraceBuilder getTraceBuilder(DetachableMessage detachableMessage, QueueHandler queueHandler) {
        TraceBuilder traceBuilder = new TraceBuilder();
        traceBuilder.internalId(detachableMessage.getInternalId())
                    .queueName(queueHandler.getUnmodifiableQueue().getName());
        return traceBuilder;
    }

    private static TraceBuilder getTraceBuilder(Message message, QueueHandler queueHandler) {
        Metadata metadata = message.getMetadata();
        String queueName = queueHandler.getUnmodifiableQueue().getName();
        TraceBuilder traceBuilder = new TraceBuilder().internalId(message.getInternalId());

        // Metadata can be null if we clear message when in-memory queue limit is exceeded
        if (Objects.nonNull(metadata)) {
            traceBuilder.routingKey(metadata.getRoutingKey())
                        .exchangeName(metadata.getExchangeName());
        }
        traceBuilder.redeliveryCount(message.getRedeliveryCount())
                    .isRedelivered(message.isRedelivered())
                    .queueName(queueName);
        return traceBuilder;
    }

    public static void trace(Xid xid, QueueHandler queueHandler, String description) {
        if (LOGGER.isTraceEnabled() && Objects.nonNull(queueHandler)) {
            TraceBuilder traceBuilder = new TraceBuilder().xid(xid)
                                                          .queueName(queueHandler.getUnmodifiableQueue().getName());
            LOGGER.trace(traceBuilder.buildTrace(description));
        }
    }

    public static void trace(Xid xid, String description) {
        if (LOGGER.isTraceEnabled()) {
            TraceBuilder traceBuilder = new TraceBuilder().xid(xid);
            LOGGER.trace(traceBuilder.buildTrace(description));
        }
    }
    /**
     * Internal trace message builder class.
     */
    private static class TraceBuilder {

        private static final String FIELD_SUFFIX = " } ";
        private static final String FIELD_PREFIX = " { ";
        private static final String FIELD_DELIMITER = " , ";

        /**
         * Char length of a average trace line.
         */
        private static final int INITIAL_CAPACITY = 200;

        private static final String INTERNAL_ID = "id: ";
        private static final String EXCHANGE_NAME = "exchangeName: ";
        private static final String ROUTING_KEY = "routingKey: ";
        private static final String QUEUE_NAME = "queueName: ";
        private static final String CONSUMER_ID = "consumerId: ";
        private static final String REDELIVERY_COUNT = "redeliveryCount: ";
        private static final String REDELIVERED = "isRedelivered: ";
        private static final String XID = "xid: ";

        private final StringBuilder fields;

        TraceBuilder() {
            this.fields = new StringBuilder(INITIAL_CAPACITY);
        }

        TraceBuilder internalId(long internalId) {
            fields.append(INTERNAL_ID).append(internalId).append(FIELD_DELIMITER);
            return this;
        }

        TraceBuilder exchangeName(String name) {
            fields.append(EXCHANGE_NAME).append(name).append(FIELD_DELIMITER);
            return this;
        }

        TraceBuilder xid(Xid xid) {
            fields.append(XID).append(xid).append(FIELD_DELIMITER);
            return this;
        }

        TraceBuilder routingKey(String routingKey) {
            fields.append(ROUTING_KEY).append(routingKey).append(FIELD_DELIMITER);
            return this;
        }

        TraceBuilder queueName(String name) {
            fields.append(QUEUE_NAME).append(name).append(FIELD_DELIMITER);
            return this;
        }

        TraceBuilder consumerId(int id) {
            fields.append(CONSUMER_ID).append(id).append(FIELD_DELIMITER);
            return this;
        }

        TraceBuilder fieldList(List<TraceField> traceFields) {
            traceFields.forEach(this::field);
            return this;
        }

        TraceBuilder field(TraceField traceField) {
            fields.append(traceField.getKey())
                  .append(TraceField.DELIMITER)
                  .append(traceField.getValue())
                  .append(FIELD_DELIMITER);
            return this;
        }

        TraceBuilder redeliveryCount(int redeliveryCount) {
            fields.append(REDELIVERY_COUNT).append(redeliveryCount).append(FIELD_DELIMITER);
            return this;
        }

        TraceBuilder isRedelivered(boolean redelivered) {
            fields.append(REDELIVERED).append(redelivered).append(FIELD_DELIMITER);
            return this;
        }

        String buildTrace(String description) {
            // remove the final FIELD_DELIMITER
            fields.delete(fields.length() - FIELD_DELIMITER.length(), fields.length());
            fields.insert(0, FIELD_PREFIX)
                  .append(FIELD_SUFFIX)
                  .append(description);

            return fields.toString();
        }
    }
}
