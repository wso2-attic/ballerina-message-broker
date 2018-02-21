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
import io.ballerina.messaging.broker.common.data.types.LongString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP frame for dtx.recover-ok
 * Parameter Summary:
 *      1. in-doubt (LongString) - Array containing the xids to be recovered
 */
public class DtxRecoverOk extends MethodFrame {

    private static final short CLASS_ID = 100;
    private static final short METHOD_ID = 81;
    private final LongString inDoubt;

    public DtxRecoverOk(int channel, LongString inDoubt) {
        super(channel, CLASS_ID, METHOD_ID);
        this.inDoubt = inDoubt;
    }

    @Override
    protected long getMethodBodySize() {
        return inDoubt.getSize();
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        inDoubt.write(buf);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        // Server does not handle dtx recover ok
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            LongString inDoubt = LongString.parse(buf);
            return new DtxRecoverOk(channel, inDoubt);
        };
    }
}
