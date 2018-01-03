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
import org.wso2.broker.amqp.codec.AmqpChannel;
import org.wso2.broker.amqp.codec.frames.BasicDeliver;
import org.wso2.broker.amqp.codec.frames.ContentFrame;
import org.wso2.broker.amqp.codec.frames.HeaderFrame;
import org.wso2.broker.common.data.types.ShortString;
import org.wso2.broker.core.ContentChunk;
import org.wso2.broker.core.Message;
import org.wso2.broker.core.Metadata;

/**
 * AMQP delivery message which consists of the basic.deliver, ContentHeader and ContentBody frames
 */
public class AmqpDeliverMessage {

    private final ShortString consumerTag;
    private final AmqpChannel channel;
    private final Message message;
    private final String queueName;

    public AmqpDeliverMessage(Message message, ShortString consumerTag, AmqpChannel channel, String queueName) {
        this.message = message;
        this.consumerTag = consumerTag;
        this.channel = channel;
        this.queueName = queueName;
    }

    public void write(ChannelHandlerContext ctx) {
        if (!channel.isFlowEnabled()) {
            channel.hold(this);
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
            headerFrame.setRawMetadata(metadata.getRawMetadata());
            ctx.write(basicDeliverFrame);
            ctx.write(headerFrame);
            for (ContentChunk chunk : message.getContentChunks()) {
                ContentFrame contentFrame = new ContentFrame(channel.getChannelId(),
                                                             chunk.getBytes().capacity(),
                                                             chunk.getBytes());
                ctx.write(contentFrame);
            }
        }
    }

    /**
     * Getter for channel
     */
    public AmqpChannel getChannel() {
        return channel;
    }
}
