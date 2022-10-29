package ru.bvkuchin.server.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ServerController {
    private Path currentDir = Paths.get("serverStorage");


    public String getFilesList() {
        StringBuilder sb = new StringBuilder();

        Stream<Path> fileList;
        try {

            fileList = Files.list(currentDir);
            fileList.map(path -> path.toString() + "//<-->//")
                    .forEach(sb::append);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();

    }

   }
