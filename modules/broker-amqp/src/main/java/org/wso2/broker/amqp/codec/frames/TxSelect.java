package org.wso2.broker.amqp.codec.frames;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.codec.AmqpChannel;
import org.wso2.broker.amqp.codec.handlers.AmqpConnectionHandler;

/**
 * AMQP frame for tx.select
 */
public class TxSelect extends MethodFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(TxSelect.class);
    private static final short CLASS_ID = 90;
    private static final short METHOD_ID = 10;

    public TxSelect(int channel) {
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
        int channelId = getChannel();
        AmqpChannel channel = connectionHandler.getChannel(channelId);
        channel.setLocalTransactional();
        ctx.writeAndFlush(new TxSelectOk(channelId));
    }

    public static AmqMethodBodyFactory getFactory() {
        return (buf, channel, size) -> new TxSelect(channel);
    }
}
