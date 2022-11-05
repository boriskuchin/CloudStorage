package ru.bvkuchin.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.bvkuchin.server.controllers.ServerController;

import java.nio.charset.StandardCharsets;

public class ServerInHandler extends ChannelInboundHandlerAdapter {

    private State currentState = State.IDLE;
    private int deletionFileNameLength;
    private String renamingOldFileName;
    private String renamingNewFileName;
    private int renamingNewFileNameLength;
    private int renamingOldFileNameLength;


    public enum State {
        IDLE,
        SENDING_FILE_LIST,
        DELETION_GETTING_FILE_NAME_LENGTH, DELETION_GETTING_FILE_NAME,
        RENAMING_GETTING_OLD_NAME_FILE_LENGTH, RENAMING_GETTING_OLD_NAME, RENAMING_GETTING_NEW_NAME_FILE_LENGTH, RENAMING_GETTING_NEW_NAME,

    }


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

            if (currentState == State.IDLE) {
                byte readed = inBuf.readByte();

                if (readed == 25){
                    currentState = State.SENDING_FILE_LIST;
                }
                if (readed == 42) {
                    currentState = State.DELETION_GETTING_FILE_NAME_LENGTH;
                }
                if (readed == 45) {
                    currentState = State.RENAMING_GETTING_OLD_NAME_FILE_LENGTH;
                }

            }
            if (currentState == State.SENDING_FILE_LIST) {
                byte[]  fileListMessage = new String(ServerController.getFilesList()).getBytes(StandardCharsets.UTF_8);
                ctx.channel().writeAndFlush(new byte[]{25});
                ctx.channel().writeAndFlush(toByteArray(fileListMessage.length));
                ctx.channel().writeAndFlush(fileListMessage);
                currentState = State.IDLE;
            }

            if (currentState == State.DELETION_GETTING_FILE_NAME_LENGTH) {
                if (inBuf.readableBytes() >= 4) {
                    deletionFileNameLength = inBuf.readInt();
                    currentState = State.DELETION_GETTING_FILE_NAME;
                }
            }

            if (currentState == State.DELETION_GETTING_FILE_NAME) {
                if (inBuf.readableBytes() >= deletionFileNameLength) {
                    byte[] fileName = new byte[deletionFileNameLength];
                    inBuf.readBytes(fileName);
                    ServerController.deleteFile(new String(fileName, StandardCharsets.UTF_8), () ->{
                        ctx.channel().writeAndFlush(new byte[]{42}); //ответ, что удаление произошло
                    });
                    currentState = State.IDLE;
                }

            }

            if (currentState == State.RENAMING_GETTING_OLD_NAME_FILE_LENGTH) {
                if (inBuf.readableBytes() >= 4) {
                    renamingOldFileNameLength = inBuf.readInt();
                    currentState = State.RENAMING_GETTING_OLD_NAME;
                }
            }

            if (currentState == State.RENAMING_GETTING_OLD_NAME) {
                if (inBuf.readableBytes() >= renamingOldFileNameLength) {
                    byte[] oldFileName = new byte[renamingOldFileNameLength];
                    inBuf.readBytes(oldFileName);
                    renamingOldFileName = new String(oldFileName, StandardCharsets.UTF_8);
                    currentState = State.RENAMING_GETTING_NEW_NAME_FILE_LENGTH;
                }
            }

            if (currentState == State.RENAMING_GETTING_NEW_NAME_FILE_LENGTH) {
                if (inBuf.readableBytes() >= 4) {
                    renamingNewFileNameLength = inBuf.readInt();
                    currentState = State.RENAMING_GETTING_NEW_NAME;
                }
            }

            if (currentState == State.RENAMING_GETTING_NEW_NAME) {
                if (inBuf.readableBytes() >= renamingNewFileNameLength) {
                    byte[] newFileName = new byte[renamingNewFileNameLength];
                    inBuf.readBytes(newFileName);
                    renamingNewFileName = new String(newFileName, StandardCharsets.UTF_8);
                    ServerController.renameFile(renamingOldFileName, renamingNewFileName, () -> {
                        ctx.channel().writeAndFlush(new byte[]{45});
                    });

                    currentState = State.IDLE;
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

