package ru.bvkuchin.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.bvkuchin.server.controllers.ServerController;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class ServerInHandler extends ChannelInboundHandlerAdapter {

    private State currentState = State.IDLE;
    private int deletionFileNameLength;
    private String renamingOldFileName;
    private String renamingNewFileName;
    private int renamingNewFileNameLength;
    private int renamingOldFileNameLength;
    private int fileFileNameLength;
    private String receivingFileName;
    private long fileFileLength;
    private BufferedOutputStream out;
    private long receivedFileLength;


    public enum State {
        IDLE,
        SENDING_FILE_LIST,
        DELETION_GETTING_FILE_NAME_LENGTH, DELETION_GETTING_FILE_NAME,
        RENAMING_GETTING_OLD_NAME_FILE_LENGTH, RENAMING_GETTING_OLD_NAME, RENAMING_GETTING_NEW_NAME_FILE_LENGTH, RENAMING_GETTING_NEW_NAME, FILE_GETTING_FILE_NAME_LENGHT, FILE_GETTING_FILE_NAME, FILE_GETTING_FILE_SIZE, FILE_GETTING_FILE,

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
            System.out.println(currentState);
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
                if (readed == 99) {
                    currentState = State.FILE_GETTING_FILE_NAME_LENGHT;
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

            if (currentState == State.FILE_GETTING_FILE_NAME_LENGHT) {
                if (inBuf.readableBytes() >= 4) {
                    fileFileNameLength = inBuf.readInt();
                    currentState = State.FILE_GETTING_FILE_NAME;
                }
            }

            if (currentState == State.FILE_GETTING_FILE_NAME) {
                if (inBuf.readableBytes() >= fileFileNameLength) {
                    byte[] fileName = new byte[fileFileNameLength];
                    inBuf.readBytes(fileName);
                    receivingFileName = new String(fileName, StandardCharsets.UTF_8);
                    out = new BufferedOutputStream( new FileOutputStream(Paths.get(ServerController.getCurrentDir() + File.separator + new String(receivingFileName)).toFile(),true));
                    currentState = State.FILE_GETTING_FILE_SIZE;
                }
            }
            if (currentState == State.FILE_GETTING_FILE_SIZE) {
                if (inBuf.readableBytes() >= 8) {
                    fileFileLength = inBuf.readLong();
                    receivedFileLength = 0L;
                    currentState = State.FILE_GETTING_FILE;
                }
            }

            if (currentState == State.FILE_GETTING_FILE) {


                while (inBuf.readableBytes() > 0) {


                    out.write(inBuf.readByte());
                    receivedFileLength++;
                    System.out.println("fileFileLength " + fileFileLength + "; receivedFileLength" + receivedFileLength);

                    if (fileFileLength == receivedFileLength) {
                        currentState = State.IDLE;
                        out.close();
                        ctx.channel().writeAndFlush(new byte[]{99});
                        break;
                    }
                    System.out.println(inBuf.readableBytes());
                    System.out.println(currentState);
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

