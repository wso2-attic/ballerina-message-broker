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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqpConnectionHandler;
import org.wso2.broker.amqp.codec.data.ShortString;

/**
 * AMQP frame for queue.declare-ok
 * Parameter Summary:
 *     1. queue (ShortString) - queue name
 *     2. message­count (long) - message count
 *     3. consumer­count (long) - number of consumers
 */
public class QueueDeclareOk extends MethodFrame {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueueDeclareOk.class);

    private final ShortString queue;
    private final long messageCount;
    private final long consumerCount;

    public QueueDeclareOk(int channel, ShortString queue, long messageCount, long consumerCount) {
        super(channel, (short) 50, (short) 11);
        this.queue = queue;
        this.messageCount = messageCount;
        this.consumerCount = consumerCount;
    }

    @Override
    protected long getMethodBodySize() {
        return queue.getSize() + 4L + 4L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        queue.write(buf);
        buf.writeInt((int) messageCount);
        buf.writeInt((int) consumerCount);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // Server does not handle basic.consumer-ok
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            ShortString queue = ShortString.parse(buf);
            long messageCount = buf.readUnsignedInt();
            long consumerCount = buf.readUnsignedInt();

            return new QueueDeclareOk(channel, queue, messageCount, consumerCount);
        };
    }
}
