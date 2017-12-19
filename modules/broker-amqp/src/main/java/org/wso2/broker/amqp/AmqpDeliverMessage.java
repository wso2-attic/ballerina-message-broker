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

    private final String consumerTag;
    private final int channelId;
    private final long deliveryTag;
    private final Message message;

    public AmqpDeliverMessage(Message message, String consumerTag, int channelId, long deliveryTag) {
        this.message = message;
        this.consumerTag = consumerTag;
        this.channelId = channelId;
        this.deliveryTag = deliveryTag;
    }

    public void write(ChannelHandlerContext ctx) {
        Metadata metadata = message.getMetadata();
        BasicDeliver basicDeliverFrame = new BasicDeliver(
                channelId,
                ShortString.parseString(consumerTag),
                deliveryTag,
                message.isRedelivered(),
                ShortString.parseString(metadata.getExchangeName()),
                ShortString.parseString(metadata.getRoutingKey()));

        HeaderFrame headerFrame = new HeaderFrame(channelId, 60, metadata.getContentLength());
        headerFrame.setRawMetadata(metadata.getRawMetadata());
        ctx.write(basicDeliverFrame);
        ctx.write(headerFrame);
        for (ContentChunk chunk : message.getContentChunks()) {
            ContentFrame contentFrame = new ContentFrame(channelId, chunk.getBytes().capacity(), chunk.getBytes());
            ctx.write(contentFrame);
        }
    }

}
