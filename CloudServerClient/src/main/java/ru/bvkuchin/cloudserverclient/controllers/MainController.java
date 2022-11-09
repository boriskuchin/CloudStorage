package ru.bvkuchin.cloudserverclient.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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

        Sender.sendRequestDirectoryContent(NettyClient.getInstance().getCurrentChannel());
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
//            Sender.sendRequestDirectoryContent(NettyClient.getInstance().getCurrentChannel());
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
        Sender.sendFileRequest(NettyClient.getInstance().getCurrentChannel(), listServer.getSelectionModel().getSelectedItem().toString());

    }
}