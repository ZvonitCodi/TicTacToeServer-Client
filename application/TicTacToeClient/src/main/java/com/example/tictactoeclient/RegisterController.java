package com.example.tictactoeclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController extends BaseController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    public void onRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        initializeClient();
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Пароли не совпадают");
            return;
        }

        String response = client.sendRegisterRequest(username, password);

        if ("register_success".equals(response)) {
            showAlert(Alert.AlertType.INFORMATION, "Регистрация успешна", "Вы успешно зарегистрированы!");
            client.setCurrentUser(username);
            openMainWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка регистрации", response);
        }
    }

    private void openMainWindow() {
        client.sendStatsRequest(client.getCurrentUser());
        client.sendGamesRequest(client.getCurrentUser());
        openWindow("/com/example/tictactoeclient/MainScreen.fxml", "Главный экран", client);
        closeWindow((Stage) registerButton.getScene().getWindow());
    }
    @FXML
    protected void onCloseRegister(ActionEvent event) {
        openWindow("/com/example/tictactoeclient/login.fxml", "Главный экран", client);
        closeWindow((Stage) ((Node) event.getSource()).getScene().getWindow());
    }
}
