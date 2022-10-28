package ru.bvkuchin.server.controllers;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerController {
    private Path currentDir = Paths.get(".");

    public void getFilesList() {
        System.out.println(currentDir.toAbsolutePath());

    }

}
