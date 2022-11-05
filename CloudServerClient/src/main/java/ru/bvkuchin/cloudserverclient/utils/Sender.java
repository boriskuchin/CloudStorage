package ru.bvkuchin.cloudserverclient.utils;

import io.netty.channel.Channel;

import java.nio.charset.StandardCharsets;

public class Sender {

    public static void sendRequestDirectoryContent(Channel channel) {
        channel.writeAndFlush(new byte[]{25});

    }


    public static void sendRequestDeleteFile(Channel channel, String fileName) {
        channel.writeAndFlush(new byte[]{42});
        channel.writeAndFlush(toByteArray(fileName.getBytes(StandardCharsets.UTF_8).length));
        channel.writeAndFlush(fileName.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] toByteArray(int value) {
        return new byte[] {
                (byte)(value >> 24),
                (byte)(value >> 16),
                (byte)(value >> 8),
                (byte)value};
    }


    public static void sendRenameRequest(Channel channel, String currentFileName, String newFileName) {
        channel.writeAndFlush(new byte[]{45});
        channel.writeAndFlush(toByteArray(currentFileName.getBytes(StandardCharsets.UTF_8).length));
        channel.writeAndFlush(currentFileName.getBytes(StandardCharsets.UTF_8));
        channel.writeAndFlush(toByteArray(newFileName.getBytes(StandardCharsets.UTF_8).length));
        channel.writeAndFlush(newFileName.getBytes(StandardCharsets.UTF_8));


    }
}
