package ru.bvkuchin.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import ru.bvkuchin.server.components.ServerController;
import ru.bvkuchin.server.services.AuthService;
import ru.bvkuchin.server.services.impl.SimpleAuthServiceImpl;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private int sendFileFileNameLength;
    private String sendFileFileName;
    private int authLoginLenght;
    private String authLogin;
    private int authPassLenght;
    private String authPass;
    private AuthService authService = ServerController.getAuthService();
    private ServerController controller;

    public enum State {
        IDLE,
        SENDING_FILE_LIST,
        DELETION_GETTING_FILE_NAME_LENGTH, DELETION_GETTING_FILE_NAME,
        RENAMING_GETTING_OLD_NAME_FILE_LENGTH,
        RENAMING_GETTING_OLD_NAME,
        RENAMING_GETTING_NEW_NAME_FILE_LENGTH,
        RENAMING_GETTING_NEW_NAME,
        FILE_GETTING_FILE_NAME_LENGHT,
        FILE_GETTING_FILE_NAME,
        FILE_GETTING_FILE_SIZE,
        FILE_GETTING_FILE,
        SENDFILE_GET_FILE_NAME_LENGHT,
        SENDFILE_GET_GETTING_FILE_NAME,
        SENDFILE_GETTING_FILE_SIZE,
        SENDFILE_SEND,
        SENDFILE_SEND_FILENAME_SIZE,
        AUTH_LOGIN_LENGHT, AUTH_GETTING_LOGIN, AUTH_PASS_LENGHT, AUTH_GETTING_PASS, AUTH_CHECK_AUTH, ADDNEWUSER_LOGIN_LENGHT, ADDNEWUSER_GETTING_LOGIN, ADDNEWUSER_PASS_LENGHT, ADDNEWUSER_GETTING_PASS, ADDNEWUSER_ADD_USER,

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
        ByteBuf outBuf = null;

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
                if (readed == 99) {
                    currentState = State.FILE_GETTING_FILE_NAME_LENGHT;
                }
                if (readed == 1) {
                    currentState = State.SENDFILE_GET_FILE_NAME_LENGHT;
                }
                if (readed == 21) {
                    currentState = State.AUTH_LOGIN_LENGHT;
                }
                if (readed == 10) {
                    currentState = State.ADDNEWUSER_LOGIN_LENGHT;
                }

            }

            if (currentState == State.SENDING_FILE_LIST) {
                byte[]  fileListMessage = new String(controller.getFilesList()).getBytes(StandardCharsets.UTF_8);

                outBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                outBuf.writeByte((byte) 25);
                ctx.channel().writeAndFlush(outBuf);

                outBuf = ByteBufAllocator.DEFAULT.directBuffer(4);
                outBuf.writeInt(fileListMessage.length);
                ctx.channel().writeAndFlush(outBuf);

                ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(fileListMessage));
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
                    controller.deleteFile(new String(fileName, StandardCharsets.UTF_8), () ->{
                        ctx.channel().writeAndFlush(ByteBufAllocator.DEFAULT.directBuffer(1).writeByte((byte) 42));

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
                    controller.renameFile(renamingOldFileName, renamingNewFileName, () -> {
                        ctx.channel().writeAndFlush(ByteBufAllocator.DEFAULT.directBuffer(1).writeByte((byte) 45));
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
                    Path outFilePath = Paths.get(controller.getCurrentDir().toString(), new String(receivingFileName));
                    if (Files.exists(outFilePath)) {
                        Files.delete(outFilePath);
                    }
                    out = new BufferedOutputStream( new FileOutputStream(outFilePath.toFile(),true));
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
                    if (fileFileLength == receivedFileLength) {
                        currentState = State.IDLE;
                        out.close();
                        outBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                        outBuf.writeByte((byte) 99);
                        ctx.channel().writeAndFlush(outBuf);
                        break;
                    }
                }
            }

            if (currentState == State.SENDFILE_GET_FILE_NAME_LENGHT) {
                if (inBuf.readableBytes() >= 4) {
                    sendFileFileNameLength = inBuf.readInt();
                    currentState = State.SENDFILE_GET_GETTING_FILE_NAME;
                }
            }

            if (currentState == State.SENDFILE_GET_GETTING_FILE_NAME) {
                if (inBuf.readableBytes() >= sendFileFileNameLength) {
                    byte[] fileName = new byte[sendFileFileNameLength];
                    inBuf.readBytes(fileName);
                    sendFileFileName = new String(fileName, StandardCharsets.UTF_8);
                    currentState = State.SENDFILE_SEND;
                }
            }

            if (currentState == State.SENDFILE_SEND) {
                Path path = Paths.get(controller.getCurrentDir().toString(), sendFileFileName);
                FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));

                outBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                outBuf.writeByte((byte) 1);
                ctx.channel().writeAndFlush(outBuf);

                byte[] fileNameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
                outBuf = ByteBufAllocator.DEFAULT.directBuffer(4);
                outBuf.writeInt(fileNameBytes.length);
                ctx.channel().writeAndFlush(outBuf);

                outBuf = ByteBufAllocator.DEFAULT.directBuffer(fileNameBytes.length);
                outBuf.writeBytes(fileNameBytes);
                ctx.channel().writeAndFlush(outBuf);

                outBuf = ByteBufAllocator.DEFAULT.directBuffer(8);
                outBuf.writeLong(Files.size(path));
                ctx.channel().writeAndFlush(outBuf);

                ChannelFuture transferOperationFuture = ctx.channel().writeAndFlush(region);

                currentState = State.IDLE;

            }

            if (currentState == State.AUTH_LOGIN_LENGHT) {
                if (inBuf.readableBytes() >= 4) {
                    authLoginLenght = inBuf.readInt();
                    currentState = State.AUTH_GETTING_LOGIN;
                }
            }

            if (currentState == State.AUTH_GETTING_LOGIN) {
                if (inBuf.readableBytes() >= authLoginLenght) {
                    byte[] authLoginBytes= new byte[authLoginLenght];
                    inBuf.readBytes(authLoginBytes);
                    authLogin = new String(authLoginBytes, StandardCharsets.UTF_8);
                    currentState = State.AUTH_PASS_LENGHT;
                }
            }

            if (currentState == State.AUTH_PASS_LENGHT) {
                if (inBuf.readableBytes() >= 4) {
                    authPassLenght = inBuf.readInt();
                    currentState = State.AUTH_GETTING_PASS;
                }
            }

            if (currentState == State.AUTH_GETTING_PASS) {
                if (inBuf.readableBytes() >= authPassLenght) {
                    byte[] authPassBytes= new byte[authPassLenght];
                    inBuf.readBytes(authPassBytes);
                    authPass = new String(authPassBytes, StandardCharsets.UTF_8);
                    currentState = State.AUTH_CHECK_AUTH;
                }
            }

            if (currentState == State.AUTH_CHECK_AUTH) {

                byte authResult;
                if (authService.checkUserExist(authLogin)) {
                    if (authService.checkCredentials(authLogin, authPass)) {
                        authResult = (byte) 22;
                        controller = new ServerController(authLogin);
                    } else {
                        authResult = (byte) 23;
                    }
//                    authResult = authService.checkCredentials(authLogin, authPass) ? (byte) 22 : (byte) 23;
                } else {
                    authResult = (byte) 24;
                }

                outBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                outBuf.writeByte(authResult);
                ctx.channel().writeAndFlush(outBuf);
                currentState = State.IDLE;

            }


            if (currentState == State.ADDNEWUSER_LOGIN_LENGHT) {
                if (inBuf.readableBytes() >= 4) {
                    authLoginLenght = inBuf.readInt();
                    currentState = State.ADDNEWUSER_GETTING_LOGIN;
                }
            }

            if (currentState == State.ADDNEWUSER_GETTING_LOGIN) {
                if (inBuf.readableBytes() >= authLoginLenght) {
                    byte[] authLoginBytes= new byte[authLoginLenght];
                    inBuf.readBytes(authLoginBytes);
                    authLogin = new String(authLoginBytes, StandardCharsets.UTF_8);
                    currentState = State.ADDNEWUSER_PASS_LENGHT;
                }
            }

            if (currentState == State.ADDNEWUSER_PASS_LENGHT) {
                if (inBuf.readableBytes() >= 4) {
                    authPassLenght = inBuf.readInt();
                    currentState = State.ADDNEWUSER_GETTING_PASS;
                }
            }

            if (currentState == State.ADDNEWUSER_GETTING_PASS) {
                if (inBuf.readableBytes() >= authPassLenght) {
                    byte[] authPassBytes= new byte[authPassLenght];
                    inBuf.readBytes(authPassBytes);
                    authPass = new String(authPassBytes, StandardCharsets.UTF_8);
                    currentState = State.ADDNEWUSER_ADD_USER;
                }
            }

            if (currentState == State.ADDNEWUSER_ADD_USER) {
                AuthService authService = ServerController.getAuthService();
                byte authResult;
                if (!authService.checkUserExist(authLogin)) {
                    authService.addUser(authLogin, authPass);
                    ServerController.createUserFolreds();
                    authResult = (byte) 11;
                } else {
                    authResult = (byte) 12;
                }
                outBuf = ByteBufAllocator.DEFAULT.directBuffer(1);
                outBuf.writeByte(authResult);
                ctx.channel().writeAndFlush(outBuf);
                currentState = State.IDLE;

            }

        }

    }



}

