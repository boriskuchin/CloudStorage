package ru.bvkuchin.server.controllers;

import ru.bvkuchin.server.callbacks.Callback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ServerController {
    private static Path currentDir = Paths.get("serverStorage");

    public static Path getCurrentDir() {
        return currentDir;
    }

    public static String getFilesList() {
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

    public static void deleteFile(String fileName, Callback deletionCallbackck) {

        Path pathToDelete = Paths.get(currentDir.toAbsolutePath().toString(), fileName);
        try {
            Files.delete(pathToDelete);
        } catch (Exception e) {
            e.printStackTrace();
        }
        deletionCallbackck.callbackAction();

    }

    public static void renameFile(String renamingOldFileName, String renamingNewFileName, Callback renamingCallback) {
        Path oldPathName = Paths.get(currentDir.toAbsolutePath().toString(), renamingOldFileName);
        Path newPathName = Paths.get(currentDir.toAbsolutePath().toString(), renamingNewFileName);
        try {
            Files.move(oldPathName, newPathName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        renamingCallback.callbackAction();

    }
}
