package ru.bvkuchin.cloudserverclient.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

import ru.bvkuchin.cloudserverclient.handlers.ClientInHandler;
import ru.bvkuchin.cloudserverclient.handlers.ClientOutHandler;

public class NettyClient {

    private static NettyClient ourInstance = new NettyClient();
    private Channel currentChannel;

    private NettyClient() {}

    public static NettyClient getInstance() {
        return ourInstance;
    }

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    private ClientInHandler clientInHandler = new ClientInHandler();

    public ClientInHandler getClientInHandler() {
        return clientInHandler;
    }

    public void start(CountDownLatch countDownLatch) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("localhost", 1111))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
//                                    new ClientOutHandler(),
                                    clientInHandler);
                            currentChannel = socketChannel;
                        }

                    });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            countDownLatch.countDown();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void stop() {
        currentChannel.close();
    }
}
