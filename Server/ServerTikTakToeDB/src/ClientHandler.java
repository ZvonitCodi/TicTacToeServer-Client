// File: ClientHandler.java
import java.io.*;
import java.net.*;
import java.sql.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request;
            while ((request = reader.readLine()) != null) { // Читаем запросы в цикле
                System.out.println("Received request: " + request);

                String[] requestParts = request.split(":");
                String command = requestParts[0];

                System.out.println("Command: " + command);
                if ("login".equals(command)) {
                    String username = requestParts[1];
                    String password = requestParts[2];
                    handleLogin(writer, username, password);
                } else if ("register".equals(command)) {
                    String username = requestParts[1];
                    String password = requestParts[2];
                    handleRegister(writer, username, password);
                } else if ("get_statistics".equals(command)) {
                    String username = requestParts[1];
                    sendUserStatistics(writer, username);
                } else if ("get_recent_games".equals(command)) {
                    String username = requestParts[1];
                    sendRecentGames(writer, username);
                } else if ("add_game".equals(command)) {
                    String gameDate = requestParts[1];
                    String winner = "null".equals(requestParts[2]) ? null : requestParts[2];
                    String playerSymbol = requestParts[3];
                    String boardState = requestParts[4];
                    boolean isFinished = Boolean.parseBoolean(requestParts[5]);
                    String username = requestParts[6];
                    addGame(writer, gameDate, winner, playerSymbol, boardState, isFinished, username);
                } else if ("get_server_move".equals(command)) {
                    String boardState = requestParts[1];
                    String boardSize = requestParts[2];
                    String playerSymbol = requestParts[3];
                    String serverSymbol = requestParts[4];
                    handleServerMove(writer, boardState, boardSize, playerSymbol, serverSymbol);
                } else if ("check_board_state".equals(command)) {
                    String boardState = requestParts[1];
                    String boardSize = requestParts[2];
                    String playerSymbol = requestParts[3];
                    String serverSymbol = requestParts[4];
                    writer.println(GameLogic.checkState(boardState, Integer.parseInt(boardSize), playerSymbol, serverSymbol));
                } else if("update_recent_game".equals(command)){
                    String username = requestParts[1];
                    String date = requestParts[2];
                    String winner = requestParts[3];
                    String playerSymbol = requestParts[4];
                    String boardState = requestParts[5];
                    String isFinished = requestParts[6];
                    updateGame(writer, username, date, winner, playerSymbol, boardState, isFinished);
                } else if("update_user_stats".equals(command)){
                    String username = requestParts[1];
                    String gamesPlayed = requestParts[2];
                    String wins = requestParts[3];
                    String losses = requestParts[4];
                    String draws = requestParts[5];
                    updateUserStats(writer, username, gamesPlayed, wins, losses, draws);
                }
                else
                    writer.println("Unknown command: " + command);
            }
            System.out.println("Finished processing request.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogin(PrintWriter writer, String username, String password) {
        try (Connection conn = DriverManager.getConnection(Server.DB_URL, Server.DB_USER, Server.DB_PASSWORD)) {
            String query = "SELECT password_hash, salt FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    // Извлечение хеша и соли из базы данных
                    String storedPasswordHash = rs.getString("password_hash");
                    String storedSalt = rs.getString("salt");

                    // Хеширование введённого пароля с использованием извлечённой соли
                    String hashedPassword = PasswordHasher.hashPassword(password, storedSalt);

                    if (hashedPassword.equals(storedPasswordHash)) {
                        writer.println("login_success");
                    } else {
                        writer.println("incorrect_password");
                    }
                } else {
                    writer.println("user_not_found");
                }
            }
        } catch (SQLException e) {
            writer.println("Database error during login: " + e.getMessage());
        }
    }

    private void handleRegister(PrintWriter writer, String username, String password) {
        try (Connection conn = DriverManager.getConnection(Server.DB_URL, Server.DB_USER, Server.DB_PASSWORD)) {
            // Генерация соли и хеширование пароля
            String salt = PasswordHasher.generateSalt();
            String hashedPassword = PasswordHasher.hashPassword(password, salt);

            String query = "INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, hashedPassword);
                stmt.setString(3, salt);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    writer.println("register_success");
                } else {
                    writer.println("register_failed");
                }
            }
        } catch (SQLException e) {
            writer.println("Ошибка в ходе регистрации: " + e.getMessage());
        }
    }
    private void handleServerMove(PrintWriter writer, String boardState, String boardSize, String playerSymbol, String serverSymbol) {
        String response = GameLogic.makeServerMove(boardState, Integer.parseInt(boardSize), playerSymbol, serverSymbol);
        System.out.println("Server move: " + response);
        writer.println(response);
    }
    private void sendUserStatistics(PrintWriter writer, String username) {
        try (Connection conn = DriverManager.getConnection(Server.DB_URL, Server.DB_USER, Server.DB_PASSWORD)) {
            String query = "SELECT games_played, wins, losses, draws FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int gamesPlayed = rs.getInt("games_played");
                    int wins = rs.getInt("wins");
                    int losses = rs.getInt("losses");
                    int draws = rs.getInt("draws");
                    writer.println(gamesPlayed + ":" + wins + ":" + losses + ":" + draws);
                }
            }
        } catch (SQLException e) {
            writer.println("Error: " + e.getMessage());
        }
    }

    private void sendRecentGames(PrintWriter writer, String username) {
        try (Connection conn = DriverManager.getConnection(Server.DB_URL, Server.DB_USER, Server.DB_PASSWORD)) {
            String query = "SELECT game_date, winner, player_symbol, board_state, is_finished FROM games WHERE username = ?  ORDER BY id DESC LIMIT 10";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                StringBuilder result = new StringBuilder();
                while (rs.next()) {
                    String date = rs.getString("game_date");
                    String winner = rs.getString("winner");
                    String playerSymbol = rs.getString("player_symbol");
                    String boardState = rs.getString("board_state");
                    boolean isFinished = rs.getBoolean("is_finished");
                    result.append(date).append("&").append(winner).append("&").append(playerSymbol).append("&").append(boardState).append("&").append(isFinished).append("|");
                }
                writer.println(result.toString());
            }
        } catch (SQLException e) {
            writer.println("Error: " + e.getMessage());
        }
    }

    private void addGame(PrintWriter writer, String gameDate, String winner, String playerSymbol, String boardState, boolean isFinished, String username) {
        try (Connection conn = DriverManager.getConnection(Server.DB_URL, Server.DB_USER, Server.DB_PASSWORD)) {
            String insertGameQuery = "INSERT INTO games (username, player_symbol, board_state, is_finished, winner, game_date) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement gameStmt = conn.prepareStatement(insertGameQuery)) {
                gameStmt.setString(1, username);
                gameStmt.setString(2, playerSymbol);
                gameStmt.setString(3, boardState);
                gameStmt.setBoolean(4, isFinished);
                gameStmt.setString(5, winner);
                gameStmt.setString(6, gameDate);
                int rowsInserted = gameStmt.executeUpdate();
                if (rowsInserted > 0) {
                    writer.println("add_game_success");
                } else {
                    writer.println("add_game_failed");
                }
            }
        } catch (SQLException e) {
            writer.println("Error adding game: " + e.getMessage());
        }
    }

    private void updateUserStats(PrintWriter writer, String username, String gamesPlayed, String wins, String losses, String draws) {
        String updateStatsQuery = "UPDATE users SET games_played = ?, wins = ?, losses = ?, draws = ? WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(Server.DB_URL, Server.DB_USER, Server.DB_PASSWORD);
             PreparedStatement statsStmt = conn.prepareStatement(updateStatsQuery)) {

            statsStmt.setString(1, gamesPlayed);
            statsStmt.setString(2, wins);
            statsStmt.setString(3, losses);
            statsStmt.setString(4, draws);
            statsStmt.setString(5, username);

            int rowsUpdated = statsStmt.executeUpdate();
            if (rowsUpdated > 0) {
                writer.println("update_user_stats_success");
            } else {
                writer.println("update_user_stats_failed");
            }
        } catch (SQLException e) {
            writer.println("Error updating user stats: " + e.getMessage());
        }
    }
    private void updateGame(PrintWriter writer, String username, String gameDate, String winner, String playerSymbol, String boardState, String isFinished) {
        String selectGameQuery = "SELECT id FROM games WHERE username = ? ORDER BY id DESC LIMIT 1";
        String updateGameQuery = "UPDATE games SET game_date = ?, winner = ?, player_symbol = ?, board_state = ?, is_finished = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(Server.DB_URL, Server.DB_USER, Server.DB_PASSWORD);
             PreparedStatement selectStmt = conn.prepareStatement(selectGameQuery);
             PreparedStatement updateStmt = conn.prepareStatement(updateGameQuery)) {

            // Найти последнюю игру
            selectStmt.setString(1, username);
            ResultSet resultSet = selectStmt.executeQuery();

            if (resultSet.next()) {
                int gameId = resultSet.getInt("id");
                System.out.println(gameId);
                // Обновить игру
                updateStmt.setString(1, gameDate);
                updateStmt.setString(2, winner);
                updateStmt.setString(3, playerSymbol);
                updateStmt.setString(4, boardState);
                updateStmt.setBoolean(5, Boolean.parseBoolean(isFinished));
                updateStmt.setInt(6, gameId);
                System.out.println("Select game query: " + selectGameQuery);
                System.out.println("Update game query: " + updateGameQuery);
                System.out.println("Params: " + gameDate + ", " + winner + ", " + playerSymbol + ", " + boardState + ", " + isFinished);

                int rowsUpdated = updateStmt.executeUpdate();
                System.out.println("Rows updated: " + rowsUpdated);

                if (rowsUpdated > 0) {
                    writer.println("update_game_success");
                    System.out.println("update_game_success");
                } else {
                    writer.println("update_game_failed");
                    System.out.println("update_game_failed");
                }
            } else {
                writer.println("update_game_not_found");
            }
        } catch (SQLException e) {
            writer.println("Error updating game: " + e.getMessage());
        }
    }

}
