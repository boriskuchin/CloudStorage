package ru.bvkuchin.cloudserverclient.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;


public class MainController {
    public ListView listClient;
    public Label labelClient;
    Path currentDirDir = Paths.get("");



    @FXML
    void initialize() {
        try {
            fillClientListView(currentDirDir);

        } catch (IOException e) {
            e.printStackTrace();

        }

        listClient.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
                    if (listClient.getSelectionModel().getSelectedItem().toString().equals("..")) {
                        try {
                            if (currentDirDir.toAbsolutePath().getParent() != null) {
                                listClient.getItems().clear();
                                currentDirDir = currentDirDir.toAbsolutePath().getParent();
                                fillClientListView(currentDirDir);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (listClient.getSelectionModel().getSelectedItem().toString().endsWith(File.separator)) {

                        try {
                            String path = labelClient.getText() + File.separator + listClient.getSelectionModel().getSelectedItem();
                            path = path.substring(0, path.length() - 1);
                            currentDirDir = Paths.get(path);
                            listClient.getItems().clear();
                            fillClientListView(currentDirDir);
                        } catch (Exception e) {
                            currentDirDir = currentDirDir.getParent();
                            listClient.getItems().clear();
                            try {
                                fillClientListView(currentDirDir);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    }
                }
            }
        );


    }

    private void fillClientListView(Path dir) throws IOException {
        Stream<Path> initialFiles = Files.list(dir);
        listClient.getItems().add("..");
        initialFiles
                .map(path -> {
                    if (path.toFile().isDirectory()) {
                        return path.getFileName().toString() + File.separator;
                    } else {
                        return path.getFileName().toString();
                    }
                })
                .forEach(s-> listClient.getItems().add(s));
        labelClient.setText(dir.toFile().getAbsolutePath());
    }

}