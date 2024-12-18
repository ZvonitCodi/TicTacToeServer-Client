package com.example.tictactoeclient;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GameController extends BaseController {

    @FXML
    private Label playerSymbolText;
    @FXML
    private Label serverSymbolText;
    @FXML
    private Label gameStatusLabel;
    @FXML
    private Button exitGameButton;
    @FXML
    private GridPane gameBoard;

    private boolean isPlayerTurn = true;
    private int lastMoveRow = -1;
    private int lastMoveCol = -1;

    Game game = null;

    @Override
    public void setClient(Client client) {
        super.setClient(client);
        if (client != null) {
            game = client.getCurrentGame();
            updateSymbols();
            gameStatusLabel.setText("В процессе игры");

            char[][] board = game.getBoardState();
            int xCount = 0, oCount = 0;
            boolean isBoardEmpty = true;
            for (int row = 0; row < game.getBoardSize(); row++) {
                for (int col = 0; col < game.getBoardSize(); col++) {
                    if (board[row][col] == 'X') xCount++;
                    if (board[row][col] == 'O') oCount++;
                    if (board[row][col] != '\0') isBoardEmpty = false;
                }
            }

            if (isBoardEmpty) {
                isPlayerTurn = game.getPlayerSymbol() == 'X';

                if (!isPlayerTurn) {
                    String serverResponse = client.sendServerMoveRequest();
                    processServerMoveResponse(serverResponse);
                }
            } else {
                if ((game.getPlayerSymbol() == 'X' && xCount <= oCount) ||
                        (game.getPlayerSymbol() == 'O' && oCount < xCount)) {
                    isPlayerTurn = true;
                } else {
                    isPlayerTurn = false;
                }

                if (!isPlayerTurn) {
                    String serverResponse = client.sendServerMoveRequest();
                    processServerMoveResponse(serverResponse);
                }
            }

            updateGameBoard();
        }
    }

    private void updateSymbols() {
        if (client != null) {
            String playerSymbol = String.valueOf(game.getPlayerSymbol());
            String serverSymbol = String.valueOf(game.getServerSymbol());
            if (playerSymbol != null) {
                if (playerSymbol.equals("X")) {
                    playerSymbolText.setText(playerSymbol);
                    playerSymbolText.setStyle("-fx-text-fill: blue;");
                    serverSymbolText.setText(serverSymbol);
                    serverSymbolText.setStyle("-fx-text-fill: red;");
                }
                else{
                    playerSymbolText.setText(playerSymbol);
                    playerSymbolText.setStyle("-fx-text-fill: red;");
                    serverSymbolText.setText(serverSymbol);
                    serverSymbolText.setStyle("-fx-text-fill: blue;");
                }
            }
        }
    }

    private void updateGameBoard() {
        if (client != null && game != null) {
            char[][] board = game.getBoardState();
            gameBoard.getChildren().clear();

            for (int row = 0; row < game.getBoardSize(); row++) {
                for (int col = 0; col < game.getBoardSize(); col++) {
                    Button cell = new Button(Character.toString(board[row][col] == '\0' ? ' ' : board[row][col]));

                    // Устанавливаем стиль для ячейки
                    if (board[row][col] == 'X') {
                        cell.setStyle("-fx-text-fill: blue; -fx-border-color: black; -fx-border-width: 2; -fx-background-color: #F9DBC3;");
                    } else if (board[row][col] == 'O') {
                        cell.setStyle("-fx-text-fill: red; -fx-border-color: black; -fx-border-width: 2; -fx-background-color: #F9DBC3;");
                    } else {
                        cell.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: #F9DBC3;");
                    }

                    // Выделение последнего хода
                    if (row == lastMoveRow && col == lastMoveCol) {
                        cell.setStyle(cell.getStyle() + " -fx-border-color: yellow; -fx-border-width: 4;");
                    }

                    if (board[row][col] != '\0') {
                        cell.setDisable(true);
                    } else {
                        if (isPlayerTurn) {
                            final int currentRow = row;
                            final int currentCol = col;
                            cell.setOnAction(event -> onPlayerMove(currentRow, currentCol));
                        }
                    }

                    cell.setFont(Font.font(24));
                    cell.setPrefSize(100, 100);
                    cell.setTextAlignment(TextAlignment.CENTER);
                    gameBoard.add(cell, col, row);
                }
            }
        }
    }


    private void onPlayerMove(int row, int col) {
        if (client != null && !game.isFinished()) {
            game.setBoardState(row, col, game.getPlayerSymbol());
            lastMoveRow = row; // Сохраняем последний ход
            lastMoveCol = col;
            updateGameBoard();
            isPlayerTurn = false;

            String status = client.sendCheckBoardStateRequest();
            handleGameEnd(status);

            if (!game.isFinished()) {
                String serverResponse = client.sendServerMoveRequest();
                processServerMoveResponse(serverResponse);
            }
        }
    }


    private void processServerMoveResponse(String response) {
        if (response == null || response.isEmpty()) {
            return;
        }

        if (response.contains(",")) {
            String[] coords = response.split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);

            game.setBoardState(row, col, game.getServerSymbol());
            lastMoveRow = row; // Сохраняем последний ход
            lastMoveCol = col;

            String status = client.sendCheckBoardStateRequest();
            handleGameEnd(status);

            if (!game.isFinished()) {
                isPlayerTurn = true;
                updateGameBoard();
            }
        }
    }


    private void handleGameEnd(String status) {
        String message = "";
        String winner = null;
        if ("playerwin".equals(status)) {
            message = "Вы выиграли!";
            gameStatusLabel.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            client.setWins(client.getWins() + 1);
            winner = "player";
        } else if ("serverwin".equals(status)) {
            message = "Вы проиграли!";
            gameStatusLabel.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            client.setLosses(client.getLosses() + 1);
            winner = "server";
        } else if ("draw".equals(status)) {
            message = "Ничья!";
            gameStatusLabel.setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
            client.setDraws(client.getDraws() + 1);
            winner = "draw";
        } else {
            return;
        }

        game.setFinished(true);
        client.sendUpdateGameRequest(winner);
        client.updateRecentGames(winner);
        showGameEndMessage(message);
    }

    private void showGameEndMessage(String message) {
        gameStatusLabel.setText(message);
        gameBoard.getChildren().clear();
        char[][] board = game.getBoardState();

        for (int row = 0; row < game.getBoardSize(); row++) {
            for (int col = 0; col < game.getBoardSize(); col++) {
                char symbol = board[row][col];
                Button cell = new Button(Character.toString(symbol == '\0' ? ' ' : symbol));
                cell.setDisable(true);
                cell.setFont(Font.font(24));
                cell.setPrefSize(100, 100);
                cell.setTextAlignment(TextAlignment.CENTER);
                cell.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: #F9DBC3;");

                // Устанавливаем цвет текста в зависимости от символа
                if (symbol == 'X') {
                    cell.setStyle(cell.getStyle() + "; -fx-text-fill: blue;");
                } else if (symbol == 'O') {
                    cell.setStyle(cell.getStyle() + "; -fx-text-fill: red;");
                }

                if (row == lastMoveRow && col == lastMoveCol) {
                    cell.setStyle(cell.getStyle() + "; -fx-border-color: yellow;");
                }

                gameBoard.add(cell, col, row);
            }
        }

        client.sendUpdateUserRequest();
        client.setCurrentGame(null);
    }


    @FXML
    private void onExitGame() {
        if (game != null && game.isFinished()) {
            closeGame();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Выход из игры");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Label message = new Label("Выберите действие:");
        ButtonType surrenderAndExit = new ButtonType("Сдаться и выйти", ButtonBar.ButtonData.YES);
        ButtonType saveAndExit = new ButtonType("Сохранить и выйти", ButtonBar.ButtonData.OK_DONE);
        ButtonType returnToGame = new ButtonType("Вернуться к игре", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().setContent(message);
        dialog.getDialogPane().getButtonTypes().addAll(surrenderAndExit, saveAndExit, returnToGame);

        dialog.showAndWait().ifPresent(response -> {
            if (response == surrenderAndExit) {
                game.setFinished(true);
                client.setLosses(client.getLosses() + 1);
                client.sendUpdateUserRequest();
                client.updateRecentGames("server");
                client.sendUpdateGameRequest("server");
                client.setCurrentGame(null);
                closeGame();
            } else if (response == saveAndExit) {
                game.setFinished(false);
                client.updateRecentGames("null");
                client.sendUpdateGameRequest(null);
                closeGame();
            } // Если выбрано "Вернуться к игре", просто закрываем диалог
        });
    }

    @FXML
    public void onCloseWindow() {
        if (game != null && game.isFinished()) {
            Stage currentStage = (Stage) exitGameButton.getScene().getWindow();
            currentStage.close();
            return;
        }

        // Действия как при "Сохранить и выйти"
        game.setFinished(false);
        client.sendUpdateGameRequest(null);

        // Закрытие окна
        Stage currentStage = (Stage) exitGameButton.getScene().getWindow();
        currentStage.close();
    }

    private void closeGame() {
        Stage currentStage = (Stage) exitGameButton.getScene().getWindow();
        closeWindow(currentStage);
        openWindow("/com/example/tictactoeclient/mainScreen.fxml", "Главное меню", client);
    }
}
