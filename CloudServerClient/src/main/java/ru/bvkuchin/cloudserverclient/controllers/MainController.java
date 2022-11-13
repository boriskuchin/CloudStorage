package ru.bvkuchin.cloudserverclient.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ru.bvkuchin.cloudserverclient.net.NettyClient;
import ru.bvkuchin.cloudserverclient.utils.Sender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;


public class MainController {
    public ListView listClient;
    public Label labelClient;
    public ListView listServer;
    public Path currentDirDir = Paths.get("");
    public Button authButton;
    public PasswordField passField;
    public TextField loginField;
    public Label labelLogin;
    public Label labelPass;
    public AnchorPane regAnchorePane;
    public Button signUpButton;
    public Label regAuthInfoLabel;
    public Label helloLabel;
    public Button sendCopyButton;
    public Button getCopyButton;
    public Button deleteServerButon;
    public Button renameServerButton;

    private ClientChangeNameController clientRenameController;
    private ServerChangeNameController serverRenameController;

    public Path getCurrentDirDir() {
        return currentDirDir;
    }

    @FXML
    void initialize() {

        NettyClient.getInstance().getClientInHandler().setMainController(this);

        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> NettyClient.getInstance().start(networkStarter)).start();
        try {
            networkStarter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        listServer.setDisable(true);
        deleteServerButon.setDisable(true);
        renameServerButton.setDisable(true);
        getCopyButton.setDisable(true);
        sendCopyButton.setDisable(true);


        fillClientListView(currentDirDir);

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
                            fillClientListView(currentDirDir);
                        } catch (Exception e) {
                            currentDirDir = currentDirDir.getParent();
                            fillClientListView(currentDirDir);
                            e.printStackTrace();
                        }
                    }
                }
            }
        );


    }

    public void fillClientListView(Path dir) {
        listClient.getItems().clear();
        Stream<Path> initialFiles = null;
        try {
            initialFiles = Files.list(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void deleteClientAction(ActionEvent actionEvent) {
        if (listClient.getSelectionModel().getSelectedItem() != null) {
            String fileName = listClient.getSelectionModel().getSelectedItem().toString();
            Path filePath = Paths.get(currentDirDir.toString(), fileName);
            if (Files.isRegularFile(filePath)) {
                try {
                    Files.delete(filePath);
                    listClient.getItems().clear();
                    fillClientListView(currentDirDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



    }

    public void renameClientAction(ActionEvent actionEvent) throws IOException {

        if ((listClient.getSelectionModel().getSelectedItem() != null)
                && Files.isRegularFile(Paths.get(String.valueOf(currentDirDir.toAbsolutePath()), File.separator, listClient.getSelectionModel().getSelectedItem().toString()))) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("client-rename-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("New Window");
            stage.setScene(scene);
            stage.show();

            clientRenameController = fxmlLoader.getController();
            String fileName = listClient.getSelectionModel().getSelectedItem().toString();
            Path src = Paths.get(String.valueOf(currentDirDir.toAbsolutePath()), File.separator, fileName);

            clientRenameController.setCurrentPath(src);
            clientRenameController.setStage(stage);
            clientRenameController.setMainController(this);

        }

    }

    public void sendCopy(ActionEvent actionEvent) {
        if (listClient.getSelectionModel().getSelectedItem() != null) {
            Path file = Paths.get(currentDirDir.toString(), listClient.getSelectionModel().getSelectedItem().toString());
            Sender.sendFile(NettyClient.getInstance().getCurrentChannel(), file, future -> {
                if (!future.isSuccess()) {
                    future.cause().printStackTrace();
//                Network.getInstance().stop();
                }
                if (future.isSuccess()) {
                    System.out.println("Файл успешно передан");
//                Network.getInstance().stop();
                }
            });
        }

    }



    public void fillServerList(String string) {
        String[] filesList = string.split("//<<<>>>//");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                listServer.getItems().clear();
                for (String file : filesList) {
                    listServer.getItems().add(file);
                }
            }
        });


    }

    public void deleteOnServer(ActionEvent actionEvent) {
        if (listServer.getSelectionModel().getSelectedItem() != null) {
            Sender.sendRequestDeleteFile(NettyClient.getInstance().getCurrentChannel(),
                    listServer.getSelectionModel().getSelectedItem().toString());
        }
    }


    public void renameServer(ActionEvent actionEvent) throws IOException {

        if (listServer.getSelectionModel().getSelectedItem() != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("server-rename-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("New Window");
            stage.setScene(scene);
            stage.show();

            serverRenameController = fxmlLoader.getController();
            String currentFileName = listServer.getSelectionModel().getSelectedItem().toString();

            serverRenameController.setCurrentName(currentFileName);
            serverRenameController.setStage(stage);
            serverRenameController.setMainController(this);

        }

    }

    public void getCopy(ActionEvent actionEvent) {
        if (listServer.getSelectionModel().getSelectedItem() != null) {
            Sender.sendFileRequest(NettyClient.getInstance().getCurrentChannel(), listServer.getSelectionModel().getSelectedItem().toString());
        }

    }

    public void singInButton(ActionEvent actionEvent) {
        disableAuthErrorLabel();
        Sender.sendAuthRequest(NettyClient.getInstance().getCurrentChannel(), loginField.getText(), passField.getText(), false);

    }

    public void signUp(ActionEvent actionEvent) {
        if (passField.getText().length() == 0) {
            getRegErrorLabel().setText("Пароль не может быть пустым");
        } else {
            disableAuthErrorLabel();
            Sender.sendAuthRequest(NettyClient.getInstance().getCurrentChannel(), loginField.getText(), passField.getText(), true );
        }

    }

    private void disableAuthErrorLabel() {
        regAuthInfoLabel.setVisible(false);
        regAuthInfoLabel.setDisable(true);
    }

    public void showWelcomePane(boolean loggedIn) {
        if (loggedIn) {
            helloLabel.setText("Добро пожаловать, " + loginField.getText());
        } else {
            helloLabel.setText("");
        }

        labelLogin.setDisable(loggedIn);
        labelLogin.setVisible(!loggedIn);

        labelPass.setVisible(!loggedIn);
        labelPass.setDisable(loggedIn);

        passField.setVisible(!loggedIn);
        passField.setDisable(loggedIn);

        loginField.setVisible(!loggedIn);
        loginField.setDisable(loggedIn);

        authButton.setVisible(!loggedIn);
        authButton.setDisable(loggedIn);

        signUpButton.setVisible(!loggedIn);
        signUpButton.setDisable(loggedIn);

        helloLabel.setVisible(loggedIn);
        helloLabel.setDisable(!loggedIn);

        listServer.setDisable(!loggedIn);

        deleteServerButon.setDisable(!loggedIn);
        renameServerButton.setDisable(!loggedIn);
        getCopyButton.setDisable(!loggedIn);
        sendCopyButton.setDisable(!loggedIn);

    }

    public Label getRegErrorLabel() {
        return regAuthInfoLabel;
    }

    public void quit(ActionEvent actionEvent) {
        System.exit(0);

    }

    public void logout(ActionEvent actionEvent) {
        listServer.getItems().clear();
        showWelcomePane(false);
    }
}