package ru.bvkuchin.cloudserverclient.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ClientChangeNameController {


    public TextField newNameTextField;
    private Path src;
    private Stage thisStage;
    private MainController mainController;

    public void renameButton(ActionEvent actionEvent) {
        Path dest = Paths.get(src.getParent().toString() + File.separator + newNameTextField.getText());
        try {
            Files.move(src, dest, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        thisStage.close();
        mainController.fillClientListView(src.getParent());
    }

    public String getNewFileName() {
        return newNameTextField.getText();
    }

    public void setCurrentPath(Path src) {
        this.src = src;
    }


    public void setStage(Stage stage) {
        this.thisStage = stage;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
