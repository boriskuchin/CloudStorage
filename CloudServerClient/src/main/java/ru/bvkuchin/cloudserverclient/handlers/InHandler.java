package ru.bvkuchin.cloudserverclient.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;

public class InHandler extends  ChannelInboundHandlerAdapter {

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


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        StringBuilder sb = new StringBuilder();
        System.out.print("Осталось " + buf.readableBytes());
        System.out.println(" State " + currentState);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                System.out.println("Readed " + readed);
                if (readed == 25) {
                    System.out.println(readed);
                    sb.append((char) buf.readByte());
                }

                // если ожидание конмады и команда за запрос состава папки = 25


            }
        }

        System.out.println(sb);

    }
}
