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

import io.ballerina.messaging.broker.amqp.codec.AmqpChannel;
import io.ballerina.messaging.broker.amqp.codec.BlockingTask;
import io.ballerina.messaging.broker.amqp.codec.ChannelException;
import io.ballerina.messaging.broker.amqp.codec.ConnectionException;
import io.ballerina.messaging.broker.amqp.codec.XaResult;
import io.ballerina.messaging.broker.amqp.codec.handlers.AmqpConnectionHandler;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.data.types.LongString;
import io.ballerina.messaging.broker.common.data.types.ShortString;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.transaction.XidImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AMQP frame for dtx.commit
 * Parameter Summary:
 *      1. format (short) - Implementation specific format code
 *      2. global-id (LongString) - Global transaction identifier
 *      3. branch-id (LongString) - Branch qualifier
 *      4. one-phase (bit) - Indicate whether one-phase or two-phase commit is used
 */
public class DtxCommit extends MethodFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(DtxCommit.class);
    private static final short CLASS_ID = 100;
    private static final short METHOD_ID = 40;
    private final int format;
    private final LongString globalId;
    private final LongString branchId;
    private final boolean onePhase;

    private DtxCommit(int channel, int format, LongString globalId, LongString branchId, boolean onePhase) {
        super(channel, CLASS_ID, METHOD_ID);
        this.format = format;
        this.globalId = globalId;
        this.branchId = branchId;
        this.onePhase = onePhase;
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

        byte flag = 0x0;
        if (onePhase) {
            flag |= 0x1;
        }
        buf.writeByte(flag);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, AmqpConnectionHandler connectionHandler) {
        int channelId = getChannel();
        AmqpChannel channel = connectionHandler.getChannel(channelId);

        ctx.fireChannelRead((BlockingTask) () -> {
            XidImpl xid = new XidImpl(format, branchId.getBytes(), globalId.getBytes());
            try {
                channel.commit(xid, onePhase);
                ctx.writeAndFlush(new DtxCommitOk(channelId, XaResult.XA_OK.getValue()));
            } catch (ValidationException e) {
                LOGGER.debug("Validation error occurred while committing transaction", e);
                ctx.writeAndFlush(new ChannelClose(getChannel(),
                                                   ChannelException.PRECONDITION_FAILED,
                                                   ShortString.parseString(e.getMessage()),
                                                   CLASS_ID,
                                                   METHOD_ID));
            } catch (BrokerException e) {
                LOGGER.error("Error occurred while committing transaction for xid " + xid, e);
                ctx.writeAndFlush(new ConnectionClose(ConnectionException.INTERNAL_ERROR,
                                                      ShortString.parseString(e.getMessage()),
                                                      CLASS_ID,
                                                      METHOD_ID));
            }
        });
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> {
            int format = buf.readUnsignedShort();
            LongString globalId = LongString.parse(buf);
            LongString branchId = LongString.parse(buf);
            byte flag = buf.readByte();
            boolean onePhase = (flag & 0x1) == 0x1;
            return new DtxCommit(channel, format, globalId, branchId, onePhase);
        };
    }
}
