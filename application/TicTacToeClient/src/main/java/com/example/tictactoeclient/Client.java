package com.example.tictactoeclient;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import java.util.Random;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";  // Адрес сервера
    private static final int SERVER_PORT = 12345;             // Порт сервера
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    // Поля для хранения данных о текущем пользователе
    private String currentUser;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private int draws;
    private List<List<String>> recentGames;
    private Game currentGame = null;

    public Client() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendLoginRequest(String username, String password) {
        sendRequest("login:" + username + ":" + password);
        return readResponse();
    }

    public String sendRegisterRequest(String username, String password) {
        sendRequest("register:" + username + ":" + password);
        return readResponse();
    }

    private void sendRequest(String request) {
        writer.println(request);
    }

    public String sendStatsRequest(String username){
        sendRequest("get_statistics:" + username);
        String response = readResponse();
        parseStatistics(response);  // Парсим статистику
        return response;
    }

    public String sendGamesRequest(String username){
        sendRequest("get_recent_games:" + username);
        String response = readResponse();
        parseRecentGames(response);  // Парсим список игр
        return response;
    }
    public String sendAddGameRequest(String winner, String playerSymbol, String boardState, boolean isFinished){
        LocalDate today = LocalDate.now();
        String formattedDate = today.toString();
        sendRequest("add_game:" + formattedDate + ":" + winner + ":" + playerSymbol + ":" + boardState + ":" + isFinished + ":" + currentUser);
        return readResponse();
    }
    public String sendServerMoveRequest(){
        sendRequest("get_server_move:"
                + currentGame.boardStateToString(currentGame.getBoardState())
                + ":" + currentGame.getBoardSize()
                + ":" + currentGame.getPlayerSymbol()
                + ":" + currentGame.getServerSymbol());
        return readResponse();
    }

    public String sendCheckBoardStateRequest(){
        sendRequest("check_board_state:"
                + currentGame.boardStateToString(currentGame.getBoardState())
                + ":" + currentGame.getBoardSize()
                + ":" + currentGame.getPlayerSymbol()
                + ":" + currentGame.getServerSymbol());
        return readResponse();
    }
    private String readResponse() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading response from server";
        }
    }

    public String sendUpdateUserRequest() {
        sendRequest("update_user_stats:"
                + this.currentUser
                + ":" + this.gamesPlayed
                + ":" + this.wins
                + ":" + this.losses
                + ":" + this.draws);
        return readResponse();
    }

    public String sendUpdateGameRequest(String winner){
        LocalDate today = LocalDate.now();
        String formattedDate = today.toString();
        sendRequest("update_recent_game:"
                + this.currentUser
                + ":" + formattedDate
                + ":" + winner
                + ":" + this.currentGame.getPlayerSymbol()
                + ":" + this.currentGame.boardStateToString(this.currentGame.getBoardState())
                + ":" + this.currentGame.isFinished());
        return readResponse();
    }

    // Методы для парсинга полученных данных и обновления полей клиента
    private void parseStatistics(String response) {
        if (response != null && !response.isEmpty()) {
            String[] stats = response.split(":");
            this.gamesPlayed = Integer.parseInt(stats[0]);
            this.wins = Integer.parseInt(stats[1]);;
            this.losses = Integer.parseInt(stats[2]);;
            this.draws = Integer.parseInt(stats[3]);;
        }
    }

    private void parseRecentGames(String response) {
        this.recentGames = new ArrayList<>();

        if (response != null && !response.isEmpty()) {
            String[] games = response.split("\\|");  // Разделяем на игры по символу "|"

            for (String game : games) {
                if (!game.isEmpty()) {
                    String[] gameDetails = game.split("&");
                    List<String> gameData = new ArrayList<>();
                    for (String detail : gameDetails) {
                        gameData.add(detail);
                    }
                    recentGames.add(gameData);
                }
            }

            if (!recentGames.isEmpty()) {
                List<String> firstGame = recentGames.get(0);
                boolean isFinished = Boolean.parseBoolean(firstGame.get(4));

                if (!isFinished) {
                    char playerSymbol = firstGame.get(2).charAt(0);
                    int boardSize = getBoardSize(firstGame.get(3));
                    setCurrentGame(new Game(playerSymbol, firstGame.get(3), boardSize,false));
                }
            }
        }
    }
    public int getBoardSize(String board) {
        String cleanedBoard = board.replace(",", "");
        int length = cleanedBoard.length();
        return (int) Math.sqrt(length);
    }


    public void addToRecentGames(String winner,String playerSymbol,String boardState, String isFinished){
        LocalDate today = LocalDate.now();
        String formattedDate = today.toString();
        recentGames.addFirst(Arrays.asList(formattedDate,winner,playerSymbol,boardState,isFinished));
    }

    public void close() {
        try {
            if (socket != null) socket.close();
            if (reader != null) reader.close();
            if (writer != null) writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void updateRecentGames(String winner) {
        this.recentGames.getFirst().set(0, LocalDate.now().toString());
        this.recentGames.getFirst().set(1, winner);
        this.recentGames.getFirst().set(2, String.valueOf(this.currentGame.getPlayerSymbol()));
        this.recentGames.getFirst().set(3, this.currentGame.boardStateToString(this.currentGame.getBoardState()));
        this.recentGames.getFirst().set(4, String.valueOf(this.currentGame.isFinished()));
    }



    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentGame(Game game) {
        this.currentGame = game;
    }

    public Game getCurrentGame() {
        return currentGame;
    }
    public int getGamesPlayed() {
        return this.gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draws;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public List<List<String>> getRecentGames() {
        return recentGames;
    }
}
