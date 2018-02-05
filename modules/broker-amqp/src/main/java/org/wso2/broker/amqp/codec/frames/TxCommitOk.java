package org.wso2.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.wso2.broker.amqp.codec.handlers.AmqpConnectionHandler;

/**
 * AMQP frame for tx.commit-ok
 */
public class TxCommitOk extends MethodFrame {

    private static final short CLASS_ID = 90;
    private static final short METHOD_ID = 21;

    public TxCommitOk(int channel) {
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
        // Server does not handle tx commit ok
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> new TxCommitOk(channel);
    }
}
