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

import io.ballerina.messaging.broker.amqp.codec.XaResult;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.data.types.LongString;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * AMQP frame for dtx.start
 * Parameter Summary:
 *      1. format (short) - Implementation specific format code
 *      2. global-id (LongString) - Global transaction identifier
 *      3. branch-id (LongString) - Branch qualifier
 *      4. join (bit) - Indicate whether this is joining an already associated xid
 *      5. resume (bit) - Indicate that the start applies to resuming a suspended transaction branch specified
 */
public class DtxStart extends MethodFrame {

    private static final short CLASS_ID = 100;
    private static final short METHOD_ID = 20;
    private final int format;
    private final LongString globalId;
    private final LongString branchId;
    private final boolean join;
    private final boolean resume;

    public DtxStart(int channel, int format, LongString globalId, LongString branchId, boolean join, boolean resume) {
        super(channel, CLASS_ID, METHOD_ID);
        this.format = format;
        this.globalId = globalId;
        this.branchId = branchId;
        this.join = join;
        this.resume = resume;
    }

    @Override
    protected long getMethodBodySize() {
        return 2L + globalId.getSize() + branchId.getSize() + 1L;
    }

    @Override
    protected void writeMethod(ByteBuf buf) {
        buf.writeShort(format);
        globalId.write(buf);
        branchId.write(buf);

        byte flags = 0x0;
        if (join) {
            flags |= 0x1;
        }
        if (resume) {
            flags |= 0x2;
        }
        buf.writeByte(flags);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        int channelId = getChannel();
        ctx.writeAndFlush(new DtxStartOk(channelId, XaResult.XA_OK.getValue()));

    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            int format = buf.readUnsignedShort();
            LongString globalId = LongString.parse(buf);
            LongString branchId = LongString.parse(buf);
            byte flags = buf.readByte();
            boolean join = (flags & 0x1) == 0x1;
            boolean resume = (flags & 0x2) == 0x2;
            return new DtxStart(channel, format, globalId, branchId, join, resume);
        };
    }
}
