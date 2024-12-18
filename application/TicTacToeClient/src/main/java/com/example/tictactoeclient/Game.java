package com.example.tictactoeclient;

import java.util.List;
import java.util.Random;

public class Game {

    private char playerSymbol;
    private char serverSymbol;
    private char[][] boardState;
    private boolean isFinished;
    private int boardSize;

    public Game(char playerSymbol, String boardState,int boardSize, boolean isFinished) {
        this.playerSymbol = playerSymbol;
        System.out.println(playerSymbol);
        this.serverSymbol = playerSymbol=='X' ? 'O' : 'X';
        System.out.println(this.serverSymbol);
        this.boardSize = boardSize;
        this.boardState = new char[boardSize][boardSize];
        // Пустая доска
        this.isFinished = isFinished;
        convertBoardState(boardState);
    }
    private void convertBoardState(String boardState) {
        String[] tokens = boardState.split(","); // Разделяем строку по запятой
        if (tokens.length != boardSize*boardSize) {
            throw new IllegalArgumentException("Неправильная строка состояния доски");
        }


        for (int i = 0; i < boardSize*boardSize; i++) {
            int row = i / boardSize;
            int col = i % boardSize;
            this.boardState[row][col] = tokens[i].trim().isEmpty() ? '\0' : tokens[i].trim().charAt(0);
        }
    }
    public String boardStateToString(char[][] board) {
        StringBuilder boardString = new StringBuilder();

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                char cell = board[row][col] == '\0' ? ' ' : board[row][col];
                boardString.append(cell);

                if (row != boardSize-1 || col != boardSize-1) {
                    boardString.append(",");
                }
            }
        }

        return boardString.toString();
    }

    public char getPlayerSymbol() {
        return playerSymbol;
    }

    public char getServerSymbol() {
        return serverSymbol;
    }

    public int getBoardSize(){
        return boardSize;
    }

    public char[][] getBoardState() {
        return boardState;
    }

    public void setBoardState(int i,int j,char symbol) {
        this.boardState[i][j]=symbol;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

}
