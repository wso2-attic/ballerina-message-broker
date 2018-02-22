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

package io.ballerina.messaging.broker.amqp.codec.frames;

import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP frame for queue.unbind-ok.
 * Parameter Summary:
 *     No parameters
 */
public class QueueUnbindOk extends MethodFrame {

    private static final short CLASS_ID = 50;

    private static final short METHOD_ID = 51;

    public QueueUnbindOk(int channel) {
        super(channel, CLASS_ID, METHOD_ID);
    }

    @Override
    protected long getMethodBodySize() {
        return 0L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // Server does not handl queue.unbind-ok frames
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> new QueueUnbindOk(channel);
    }


}
