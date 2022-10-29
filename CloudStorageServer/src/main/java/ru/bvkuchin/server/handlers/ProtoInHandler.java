package ru.bvkuchin.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.bvkuchin.server.controllers.ServerController;

import java.io.BufferedOutputStream;

public class ProtoInHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        IDLE,
        NAME_LENGTH,
        NAME,
        FILE_NAME,
        FILE
    }

    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream outputStream;


    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected...");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected...");
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf inBuf = (ByteBuf) msg;
        ByteBuf outBuf = ByteBufAllocator.DEFAULT.directBuffer(1);



        while (inBuf.readableBytes() > 0) {
            byte readed = inBuf.readByte();
            if (currentState == State.IDLE) {
                // если ожидание конмады и команда за запрос состава папки = 25
                if (readed == 25) {
                    System.out.println(new ServerController().getFilesList());
                    outBuf.writeByte((byte) 25);
                    ctx.writeAndFlush(outBuf);
                    ctx.writeAndFlush(new ServerController().getFilesList());

                }
            }
        }
    }
}

