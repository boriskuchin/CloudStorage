package ru.bvkuchin.server.controllers;

import ru.bvkuchin.server.callbacks.Callback;
import ru.bvkuchin.server.models.User;
import ru.bvkuchin.server.services.AuthService;
import ru.bvkuchin.server.services.impl.SimpleAuthServiceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PrimitiveIterator;
import java.util.stream.Stream;

public class ServerController {

    private static AuthService authService = new SimpleAuthServiceImpl();
    private static Path rootDir = Paths.get("serverStorage");
    private Path currentDir;
    private String dir;

    public ServerController(String dir) {
        this.dir = dir;
        this.currentDir = Paths.get(rootDir.toString(), dir);
        if (!Files.exists(rootDir)) {
            try {
                Files.createDirectory(rootDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Path getCurrentDir() {
        return currentDir;
    }

    public static AuthService getAuthService() {
        return authService;
    }

    public String getFilesList() {

        StringBuilder sb = new StringBuilder();
        Stream<Path> fileList;
        try {
            fileList = Files.list(currentDir);
            fileList.map(path -> path.toAbsolutePath().getFileName().toString() + "//<<<>>>//")
                    .forEach(sb::append);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();

    }

    public void deleteFile(String fileName, Callback deletionCallbackck) {

        Path pathToDelete = Paths.get(currentDir.toAbsolutePath().toString(), fileName);
        try {
            Files.delete(pathToDelete);
        } catch (Exception e) {
            e.printStackTrace();
        }
        deletionCallbackck.callbackAction();

    }

    public void renameFile(String renamingOldFileName, String renamingNewFileName, Callback renamingCallback) {
        Path oldPathName = Paths.get(currentDir.toAbsolutePath().toString(), renamingOldFileName);
        Path newPathName = Paths.get(currentDir.toAbsolutePath().toString(), renamingNewFileName);
        try {
            Files.move(oldPathName, newPathName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        renamingCallback.callbackAction();

    }

    public static void createUserFolreds() {

        Path rootPath = Paths.get(rootDir.toString());
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            try {
                Files.createDirectory(rootDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (User u : authService.getUserList()) {
            Path path = Paths.get(rootDir.toString(), u.getLogin());
            if (!Files.exists(path) || !Files.isDirectory(path)) {
                try {
                    Files.createDirectory(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
