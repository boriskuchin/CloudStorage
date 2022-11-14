package ru.bvkuchin.cloudserverclient.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import ru.bvkuchin.cloudserverclient.controllers.MainController;
import ru.bvkuchin.cloudserverclient.net.NettyClient;
import ru.bvkuchin.cloudserverclient.utils.Sender;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientInHandler extends  ChannelInboundHandlerAdapter {

    private int fileFileNameLength;
    private String receivingFileName;
    private BufferedOutputStream out;
    private long fileFileLength;

    public enum State {
        IDLE,
        NAME_LENGTH,
        NAME,
        FILE_NAME,
        FILE,
        GETTING_FILES_LIST, FILE_GETTING_FILE_NAME_LENGHT, FILE_LIST_SIZE_RECEIVING, FILE_GETTING_FILE_NAME, FILE_GETTING_FILE_SIZE, FILE_GETTING_FILE;
    }

    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream outputStream;
    private int filesListLenght;
    private int receivedFileList;

    private MainController mainController;




    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg ) throws Exception {
        ByteBuf inBuf = (ByteBuf) msg;
        StringBuilder sb = new StringBuilder();
        while (inBuf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readedByte = inBuf.readByte();
                if ((readedByte == 25)) {
                    currentState = State.FILE_LIST_SIZE_RECEIVING;
                }
                if ((readedByte == 1)) {
                    currentState = State.FILE_GETTING_FILE_NAME_LENGHT;
                }

                if ((readedByte == 42 || readedByte == 45 || readedByte == 99)) {
                    Sender.sendRequestDirectoryContent(ctx.channel());
                }

                if (readedByte == 22) {
                    Platform.runLater(() -> mainController.showWelcomePane(true));
                    Sender.sendRequestDirectoryContent(NettyClient.getInstance().getCurrentChannel());
                }
                if (readedByte == 23) {
                    Platform.runLater(() -> {
                        mainController.getRegErrorLabel().setVisible(true);
                        mainController.getRegErrorLabel().setDisable(false);
                        mainController.getRegErrorLabel().setText("Некорректное имя пользователя или пароль");
                    } );
                }
                if (readedByte == 24) {
                    Platform.runLater(() -> {
                        mainController.getRegErrorLabel().setVisible(true);
                        mainController.getRegErrorLabel().setDisable(false);
                        mainController.getRegErrorLabel().setText("Пользователя не существует");
                    } );
                }
                if (readedByte == 11) {
                    Platform.runLater(() -> {
                        mainController.getRegErrorLabel().setVisible(true);
                        mainController.getRegErrorLabel().setDisable(false);
                        mainController.getRegErrorLabel().setText("Пользователь добавлен");
                    } );
                }

                if (readedByte == 12) {
                    Platform.runLater(() -> {
                        mainController.getRegErrorLabel().setVisible(true);
                        mainController.getRegErrorLabel().setDisable(false);
                        mainController.getRegErrorLabel().setText("Пользователь с таким логином уже существует");
                    } );
                }


            }

            if (currentState == State.FILE_LIST_SIZE_RECEIVING) {
                if (inBuf.readableBytes() >= 4) {
                    filesListLenght = inBuf.readInt();
                    currentState = State.GETTING_FILES_LIST;
                }
            }

            if (currentState == State.GETTING_FILES_LIST) {
                if (inBuf.readableBytes() >= filesListLenght) {
                    byte[] fileList = new byte[filesListLenght];
                    inBuf.readBytes(fileList);
                    mainController.fillServerList(new String(fileList, StandardCharsets.UTF_8).trim());
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
                    Path outPath = Paths.get(mainController.getCurrentDirDir().toString(), new String(receivingFileName));
                    out = new BufferedOutputStream( new FileOutputStream(outPath.toFile(),true));
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
                        Platform.runLater(() -> {
                            mainController.fillClientListView(mainController.getCurrentDirDir());
                        });
                        break;
                    }
                }
            }
        }
    }
}
