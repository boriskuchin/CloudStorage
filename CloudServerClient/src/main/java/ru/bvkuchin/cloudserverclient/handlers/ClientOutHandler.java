package ru.bvkuchin.cloudserverclient.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.Arrays;

public class ClientOutHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {


        byte[] arr = (byte[]) msg;
        System.out.println("Отправляется :" + Arrays.toString(arr));

        ByteBufAllocator byteBufAllocator = new PooledByteBufAllocator();
        ByteBuf buf = byteBufAllocator.buffer(arr.length);
        buf.writeBytes(arr);
        ctx.writeAndFlush(buf);


    }
}
