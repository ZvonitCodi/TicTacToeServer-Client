package com.example.tictactoeclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application {
    private static Client client = new Client();

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/tictactoeclient/login.fxml"));
            AnchorPane root = loader.load();
            LoginController loginController = loader.getController();
            loginController.setClient(client); // Передаем общий клиент
            // Создаем сцену
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Крестики-Нолики");
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
