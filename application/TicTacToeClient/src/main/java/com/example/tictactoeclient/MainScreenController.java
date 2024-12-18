package com.example.tictactoeclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.Random;

public class MainScreenController extends BaseController {

    @FXML
    private Label nicknameLabel;
    @FXML
    private Label gamesPlayedLabel;
    @FXML
    private Label winsLabel;
    @FXML
    private Label lossesLabel;
    @FXML
    private ListView<String> recentGamesList;
    @FXML
    private Button logoutButton;
    @FXML
    private Button newGameButton;
    @FXML
    private Button continueGameButton;

    @Override
    public void setClient(Client client) {
        super.setClient(client); // Устанавливаем клиента в родительский класс
        if (client != null) {
            // Загружаем данные о пользователе
            nicknameLabel.setText(client.getCurrentUser());// Используем никнейм из клиента
            loadUserStatistics();
            loadRecentGames();
        }
    }

    @FXML
    private void onLogout() {
        try {
            // Закрытие текущего окна
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();

            // Закрытие клиента
            if (client != null) {
                client.close();
                client = null; // Обнуляем клиент
            }

            // Открытие окна авторизации
            openWindow("/com/example/tictactoeclient/login.fxml", "Авторизация", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUserStatistics() {
        if (client == null || client.getCurrentUser() == null) {
            System.out.println("Client or current user is null");
            return;
        }

        // Отправляем запрос на получение статистики пользователя
        gamesPlayedLabel.setText(client.getGamesPlayed() > 0 ? "    "+client.getGamesPlayed() : "    0");
        winsLabel.setText(client.getWins() > 0 ? "    "+client.getWins() : "    0");
        lossesLabel.setText(client.getLosses() > 0 ? "    "+client.getLosses() : "    0");


    }

    private void loadRecentGames() {
        if (client == null || client.getCurrentUser() == null) {
            return;
        }

      // Отправляем запрос на получение недавних игр пользователя
        recentGamesList.getItems().clear();

        if(!client.getRecentGames().isEmpty()){
            for (List<String> game : client.getRecentGames()) {
                String date = game.get(0);  // Получаем дату игры
                String winner = game.get(1);  // Получаем победителя игры
                String displayText = date + " - Winner: " + winner;  // Формируем строку для отображения
                recentGamesList.getItems().add(displayText);  // Добавляем строку в список
            }
        }
        if(client.getCurrentGame()==null){
            newGameButtons();
        }
        else{
            continueGameButtons();
        }

    }
    private void newGameButtons(){
        newGameButton.setDisable(false);
        continueGameButton.setDisable(true);
    }
    private void continueGameButtons(){
        newGameButton.setDisable(true);
        continueGameButton.setDisable(false);
    }

    @FXML
    private void onNewGame(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/tictactoeclient/fieldSizeSelection.fxml"));

            // Загружаем FXML и получаем root-узел
            Parent root = loader.load();

            // Получаем контроллер до передачи управления в openWindow
            FieldSizeSelectionController controller = loader.getController();

            // Открываем окно с использованием базового метода
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Выбор размера поля");
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.showAndWait();

            // Проверяем выбранный размер после закрытия окна
            int selectedSize = controller.getSelectedSize();
            if (selectedSize > 0) {
                Random rand = new Random();
                char playerSymbol = rand.nextInt(2) == 0 ? 'X' : 'O';
                int totalCells = selectedSize * selectedSize;
                // Формируем строку с запятыми и пробелами
                StringBuilder emptyBoard = new StringBuilder();

                for (int i = 0; i < totalCells-1; i++) {
                    emptyBoard.append(" ,");
                }
                emptyBoard.append(" ");
                client.setCurrentGame(new Game(playerSymbol,emptyBoard.toString(), selectedSize,false));
                client.addToRecentGames(null, String.valueOf(playerSymbol), emptyBoard.toString(), "false");
                client.sendAddGameRequest(null, String.valueOf(playerSymbol), emptyBoard.toString(), false);
                client.setGamesPlayed(client.getGamesPlayed() + 1);
                client.sendUpdateUserRequest();
                if(client.getRecentGames().size() > 10){
                    client.getRecentGames().removeLast();
                }

                openWindow("/com/example/tictactoeclient/game.fxml", "Игра", client);
                closeWindow((Stage) ((Node) event.getSource()).getScene().getWindow());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onContinueGame(ActionEvent event) {
        openWindow("/com/example/tictactoeclient/game.fxml", "Игра", client);

        closeWindow((Stage) ((Node) event.getSource()).getScene().getWindow());
    }
}
