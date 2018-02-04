/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.broker.amqp;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqpChannel;
import org.wso2.broker.amqp.codec.frames.BasicDeliver;
import org.wso2.broker.amqp.codec.frames.ContentFrame;
import org.wso2.broker.amqp.codec.frames.HeaderFrame;
import org.wso2.broker.common.data.types.ShortString;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.BrokerException;
import org.wso2.broker.core.ContentChunk;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.Metadata;
import org.wso2.broker.core.util.MessageTracer;
import org.wso2.broker.core.util.TraceField;

/**
 * AMQP delivery message which consists of the basic.deliver, ContentHeader and ContentBody frames.
 */
public class AmqpDeliverMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(AmqpDeliverMessage.class);


    private static final String SEND_MESSAGE = "Delivering message to client from AMQP transport.";
    private static final String SENT_ON_HOLD = "Message delivery on hold. Flow disabled.";
    private static final String QUEUE_NAME_FIELD = "queueName";
    private static final String CONSUMER_ADDRESS_FIELD = "consumerAddress";

    private final ShortString consumerTag;
    private final AmqpChannel channel;
    private final Message message;
    private final String queueName;
    private final Broker broker;

    public AmqpDeliverMessage(Message message,
                              ShortString consumerTag,
                              AmqpChannel channel,
                              String queueName,
                              Broker broker) {
        this.message = message;
        this.consumerTag = consumerTag;
        this.channel = channel;
        this.queueName = queueName;
        this.broker = broker;
    }

    public void write(ChannelHandlerContext ctx) {
        if (channel.isClosed()) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Requeueing message since subscriber is already closed. {}", message);
                }
                broker.requeue(queueName, message);
            } catch (BrokerException e) {
                LOGGER.error("Error while requeueing message {} for queue {}", message, queueName, e);
            }
        } else if (!channel.isFlowEnabled()) {
            channel.hold(this);
            if (MessageTracer.isTraceEnabled()) {
                MessageTracer.trace(message, SENT_ON_HOLD,
                                    new TraceField(AmqpConsumer.CONSUMER_TAG_FIELD_NAME, consumerTag),
                                    new TraceField(AmqpChannel.CHANNEL_ID_FIELD_NAME, channel.getChannelId()),
                                    new TraceField(QUEUE_NAME_FIELD, queueName),
                                    new TraceField(CONSUMER_ADDRESS_FIELD, ctx.channel().remoteAddress())
                                   );
            }
        } else {
            long deliveryTag = channel.getNextDeliveryTag();
            channel.recordMessageDelivery(deliveryTag, new AckData(message.shallowCopy(), queueName, consumerTag));

            Metadata metadata = message.getMetadata();
            BasicDeliver basicDeliverFrame = new BasicDeliver(
                    channel.getChannelId(),
                    consumerTag,
                    deliveryTag,
                    message.isRedelivered(),
                    ShortString.parseString(metadata.getExchangeName()),
                    ShortString.parseString(metadata.getRoutingKey()));

            HeaderFrame headerFrame = new HeaderFrame(channel.getChannelId(), 60, metadata.getContentLength());
            headerFrame.setProperties(metadata.getProperties());
            headerFrame.setHeaders(metadata.getHeaders());
            ctx.write(basicDeliverFrame);
            ctx.write(headerFrame);
            for (ContentChunk chunk : message.getContentChunks()) {
                ContentFrame contentFrame = new ContentFrame(channel.getChannelId(),
                                                             chunk.getBytes().capacity(),
                                                             chunk.getBytes());
                ctx.write(contentFrame);
            }

            if (MessageTracer.isTraceEnabled()) {
                MessageTracer.trace(message, SEND_MESSAGE,
                                    new TraceField(AmqpChannel.DELIVERY_TAG_FIELD_NAME, deliveryTag),
                                    new TraceField(AmqpChannel.CHANNEL_ID_FIELD_NAME, channel.getChannelId()),
                                    new TraceField(QUEUE_NAME_FIELD, queueName),
                                    new TraceField(AmqpConsumer.CONSUMER_TAG_FIELD_NAME, consumerTag),
                                    new TraceField(CONSUMER_ADDRESS_FIELD, ctx.channel().remoteAddress())
                                    );
            }

        }
    }
}
