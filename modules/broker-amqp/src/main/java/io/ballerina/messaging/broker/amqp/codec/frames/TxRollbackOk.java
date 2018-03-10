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
 * AMQP frame for tx.rollback-ok.
 */
public class TxRollbackOk extends MethodFrame {

    private static final short CLASS_ID = 90;
    private static final short METHOD_ID = 31;

    public TxRollbackOk(int channel) {
        super(channel, CLASS_ID, METHOD_ID);
    }

    @Override
    protected long getMethodBodySize() {
        return 0;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // Server does not handle tx rollback ok
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> new TxRollbackOk(channel);
    }
}
