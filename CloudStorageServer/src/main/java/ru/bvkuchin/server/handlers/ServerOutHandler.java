package ru.bvkuchin.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.Arrays;

public class ServerOutHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        byte[] arr = (byte[]) msg;
        ByteBufAllocator byteBufAllocator = new PooledByteBufAllocator();
        ByteBuf buf = byteBufAllocator.buffer(arr.length);
        buf.writeBytes(arr);
        ctx.writeAndFlush(buf);

    }
}
