package org.wso2.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.wso2.broker.amqp.codec.handlers.AmqpConnectionHandler;

/**
 * AMQP frame for tx.rollback-ok
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
