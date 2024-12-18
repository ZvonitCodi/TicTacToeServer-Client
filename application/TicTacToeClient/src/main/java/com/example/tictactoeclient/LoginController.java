package com.example.tictactoeclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController extends BaseController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    public void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        initializeClient();
        String response = client.sendLoginRequest(username, password);

        if ("login_success".equals(response)) {
            showAlert(Alert.AlertType.INFORMATION, "Авторизация успешна", "Вы успешно вошли в систему!");
            client.setCurrentUser(username);
            openMainWindow();
        } else if ("incorrect_password".equals(response)) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Неверный пароль");
        } else if ("user_not_found".equals(response)) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Пользователь не найден");
        } else {
            showAlert(Alert.AlertType.ERROR, "Ошибка подключения", response);
        }
    }

    @FXML
    public void openRegisterWindow(ActionEvent event) {
        openWindow("/com/example/tictactoeclient/register.fxml", "Регистрация", client);
        closeWindow((Stage) ((Node) event.getSource()).getScene().getWindow());
    }

    private void openMainWindow() {
        client.sendStatsRequest(client.getCurrentUser());
        client.sendGamesRequest(client.getCurrentUser());
        openWindow("/com/example/tictactoeclient/MainScreen.fxml", "Главный экран", client);
        closeWindow((Stage) loginButton.getScene().getWindow());
    }

}
