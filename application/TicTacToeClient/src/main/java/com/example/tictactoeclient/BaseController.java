package com.example.tictactoeclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public abstract class BaseController {
    protected Client client;
    public void initializeClient() {

        if (client == null) {
            client = new Client();
        }
    }
    public void setClient(Client client) {
        this.client = client;
    }


    protected void openWindow(String fxmlPath, String title, Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            BaseController controller = loader.getController();
            if (controller != null) {
                controller.setClient(client);
            }

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void closeWindow(Stage stage) {
        if (stage != null) {
            stage.close();
        }
    }

    protected void minimizeWindow(Stage stage) {
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    protected void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    @FXML
    protected void onMinimize(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        minimizeWindow(stage);
    }

    @FXML
    protected void onClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        closeWindow(stage);
    }

}

