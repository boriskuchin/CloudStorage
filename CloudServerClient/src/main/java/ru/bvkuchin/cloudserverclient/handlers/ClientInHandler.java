package ru.bvkuchin.cloudserverclient.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.bvkuchin.cloudserverclient.controllers.MainController;

import java.io.BufferedOutputStream;
import java.util.Arrays;

public class ClientInHandler extends  ChannelInboundHandlerAdapter {

    public enum State {
        IDLE,
        NAME_LENGTH,
        NAME,
        FILE_NAME,
        FILE,
        GETTING_FILES_LIST, FILE_LIST_SIZE_RECEIVING
    }

    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream outputStream;
    private int filesListLenght;
    private long receivedFileList;

    private MainController mainController;


    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        StringBuilder sb = new StringBuilder();
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readedByte = buf.readByte();
                if ((readedByte == 25)) {
                    currentState = State.FILE_LIST_SIZE_RECEIVING;
                }
            }

            if (currentState == State.FILE_LIST_SIZE_RECEIVING) {
                if (buf.readableBytes() >= 4) {
                    filesListLenght = buf.readInt();
                    currentState = State.GETTING_FILES_LIST;
                }
            }

            if (currentState == State.GETTING_FILES_LIST) {
                receivedFileList = 0L;
                while (buf.readableBytes() > 0) {
                    receivedFileList++;
                    char readed = (char) buf.readByte();
                    sb.append(readed);
                    if (receivedFileList == filesListLenght) {
                        currentState = State.IDLE;
                        break;
                    }
                }
            }
        }

        mainController.fillServerList(sb.toString().trim().replaceFirst("(\\r|\\n)+", ""));

    }
}
