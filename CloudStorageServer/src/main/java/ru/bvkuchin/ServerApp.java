package ru.bvkuchin;


import lombok.extern.slf4j.Slf4j;
import ru.bvkuchin.server.NettyServer;

@Slf4j
public class ServerApp {
    public static void main(String[] args) {
        try {
            new NettyServer().run();
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
    }
}