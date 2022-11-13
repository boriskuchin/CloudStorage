package ru.bvkuchin.cloudserverclient.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Sender {

    public static void sendRequestDirectoryContent(Channel channel) {
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 25);
        channel.writeAndFlush(buf);

    }


    public static void sendRequestDeleteFile(Channel channel, String fileName) {
        channel.writeAndFlush(new byte[]{42});

        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 42);
        channel.writeAndFlush(buf);

        byte[] filenameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
    }


    public static void sendRenameRequest(Channel channel, String currentFileName, String newFileName) {
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 45);
        channel.writeAndFlush(buf);

        byte[] currentFileNameBytes = currentFileName.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(currentFileNameBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(currentFileNameBytes.length);
        buf.writeBytes(currentFileNameBytes);
        channel.writeAndFlush(buf);

        byte[] newFileNameBytes = newFileName.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(newFileNameBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(newFileNameBytes.length);
        buf.writeBytes(newFileNameBytes);
        channel.writeAndFlush(buf);

    }

    public static void sendFile(Channel channel, Path path, ChannelFutureListener finishListener) {
        try {
            FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
            ByteBuf buf = null;
            buf = ByteBufAllocator.DEFAULT.directBuffer(1);
            buf.writeByte((byte) 99);
            channel.writeAndFlush(buf);

            byte[] fileNameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
            buf = ByteBufAllocator.DEFAULT.directBuffer(4);
            buf.writeInt(fileNameBytes.length);
            channel.writeAndFlush(buf);

            buf = ByteBufAllocator.DEFAULT.directBuffer(fileNameBytes.length);
            buf.writeBytes(fileNameBytes);
            channel.writeAndFlush(buf);

            buf = ByteBufAllocator.DEFAULT.directBuffer(8);
            buf.writeLong(Files.size(path));
            channel.writeAndFlush(buf);

            ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
            if (finishListener != null) {
                transferOperationFuture.addListener(finishListener);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendFileRequest(Channel channel, String fileName) {
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(100);
        buf.writeByte((byte) 1);
        channel.writeAndFlush(buf);

        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(fileNameBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(fileNameBytes.length);
        buf.writeBytes(fileNameBytes);
        channel.writeAndFlush(buf);
    }

    public static void sendAuthRequest(Channel channel, String login, String password, boolean isNewUser) {
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        if (!isNewUser) {
            buf.writeByte((byte) 21);
        } else {
            buf.writeByte((byte) 10);
        }
        channel.writeAndFlush(buf);

        byte[] loginBytes = login.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(loginBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(loginBytes.length);
        buf.writeBytes(loginBytes);
        channel.writeAndFlush(buf);

        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(passwordBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(passwordBytes.length);
        buf.writeBytes(passwordBytes);
        channel.writeAndFlush(buf);

    }


}
