package ru.bvkuchin.cloudserverclient.senders;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.buffer.ByteBuf;

public class CmdSender {

    public void sendCmd(Channel channel) {
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 25);
        channel.writeAndFlush(buf);

    }
}
