package ru.bvkuchin.cloudserverclient.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.bvkuchin.cloudserverclient.net.NettyClient;
import ru.bvkuchin.cloudserverclient.utils.Sender;

public class ServerChangeNameController {
    public TextField newNameTextField;
    private MainController mainController;
    private Stage stage;
    private String currentFileName;

    public void renameButton(ActionEvent actionEvent) {
        if (!newNameTextField.getText().equals("")) {
            Sender.sendRenameRequest(NettyClient.getInstance().getCurrentChannel(), currentFileName, newNameTextField.getText());
            this.stage.close();
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCurrentName(String currentFileName) {
        this.currentFileName = currentFileName;
    }
}
