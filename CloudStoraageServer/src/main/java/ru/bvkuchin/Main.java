package ru.bvkuchin;


import lombok.extern.slf4j.Slf4j;
import ru.bvkuchin.server.NettyServer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class Main {
    public static void main(String[] args) {
//        try {
//            new NettyServer().run();
//        } catch (Exception e) {
//            log.debug(e.getMessage());
//        }

        System.out.println(new File("serverStorage").exists());
        System.out.println(Paths.get(".").toAbsolutePath());

    }
}