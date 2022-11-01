package ru.bvkuchin.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.bvkuchin.server.controllers.ServerController;

import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;

public class ServerInHandler extends ChannelInboundHandlerAdapter {

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
                if (readed == 25){
                    byte[]  fileListMessage = new ServerController().getFilesList().getBytes(StandardCharsets.UTF_8);
                    System.out.println(new ServerController().getFilesList());
                    ctx.channel().writeAndFlush(new byte[]{25});
                    System.out.println(fileListMessage.length);
                    ctx.channel().writeAndFlush(toByteArray(fileListMessage.length));
                    ctx.channel().writeAndFlush(fileListMessage);


                }
            }
        }
    }

    private byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value};
    }


}

