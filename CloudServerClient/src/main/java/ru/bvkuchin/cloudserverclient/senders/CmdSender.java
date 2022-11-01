package ru.bvkuchin.cloudserverclient.senders;

import io.netty.channel.Channel;

public class CmdSender {

    public static void updateFilesInDirectory(Channel channel) {
        channel.writeAndFlush(new byte[]{25});

    }
}
