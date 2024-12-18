public class GameLogic {

    public static String makeServerMove(String boardState, int boardSize, String playerSymbol, String serverSymbol) {
        char[] board = boardState.replace(",", "").toCharArray();
        int bestMove = findBestMove(board, serverSymbol.charAt(0), playerSymbol.charAt(0), boardSize);
        if (bestMove != -1) {
            return (bestMove / boardSize) + "," + (bestMove % boardSize);
        }
        return "draw";
    }

    public static String checkState(String boardState, int boardSize, String playerSymbol, String serverSymbol) {
        char[] board = boardState.replace(",", "").toCharArray();

        if (checkWinner(board, boardSize, serverSymbol.charAt(0))) {
            return "serverwin";
        }

        if (checkWinner(board, boardSize, playerSymbol.charAt(0))) {
            return "playerwin";
        }

        if (isDraw(board)) {
            return "draw";
        }
        return "continue";
    }

    private static boolean checkWinner(char[] board, int boardSize, char symbol) {
        int[][] winPositions = generateWinPositions(boardSize);

        for (int[] pos : winPositions) {
            boolean win = true;
            for (int idx : pos) {
                if (board[idx] != symbol) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }
        return false;
    }

    private static int[][] generateWinPositions(int boardSize) {
        if (boardSize == 3) {
            return new int[][] {
                    // Горизонтали из 3 символов
                    {0, 1, 2},
                    {3, 4, 5},
                    {6, 7, 8},
                    // Вертикали из 3 символов
                    {0, 3, 6},
                    {1, 4, 7},
                    {2, 5, 8},
                    // Главные диагонали из 3 символов
                    {0, 4, 8},
                    // Побочные диагонали из 3 символов
                    {2, 4, 6}
            };
        } else if (boardSize == 4) {
            return new int[][] {
                    // Горизонтали из 4 символов
                    {0, 1, 2, 3},
                    {4, 5, 6, 7},
                    {8, 9, 10, 11},
                    {12, 13, 14, 15},
                    // Вертикали из 4 символов
                    {0, 4, 8, 12},
                    {1, 5, 9, 13},
                    {2, 6, 10, 14},
                    {3, 7, 11, 15},
                    // Главные диагонали из 4 символов
                    {0, 5, 10, 15},
                    // Побочные диагонали из 4 символов
                    {3, 6, 9, 12}
            };
        } else if (boardSize == 5) {
            return new int[][] {
                    // Горизонтали из 4 и 5 символов
                    {0, 1, 2, 3}, {1, 2, 3, 4},
                    {5, 6, 7, 8}, {6, 7, 8, 9},
                    {10, 11, 12, 13}, {11, 12, 13, 14},
                    {15, 16, 17, 18}, {16, 17, 18, 19},
                    {20, 21, 22, 23}, {21, 22, 23, 24},

                    {0, 1, 2, 3, 4},
                    {5, 6, 7, 8, 9},
                    {10, 11, 12, 13, 14},
                    {15, 16, 17, 18, 19},
                    {20, 21, 22, 23, 24},

                    // Вертикали, 4 и 5 символов

                    {0, 5, 10, 15}, {5, 10, 15, 20},
                    {1, 6, 11, 16}, {6, 11, 16, 21},
                    {2, 7, 12, 17}, {7, 12, 17, 22},
                    {3, 8, 13, 18}, {8, 13, 18, 23},
                    {4, 9, 14, 19}, {9, 14, 19, 24},

                    {0, 5, 10, 15, 20},
                    {1, 6, 11, 16, 21},
                    {2, 7, 12, 17, 22},
                    {3, 8, 13, 18, 23},
                    {4, 9, 14, 19, 24},

                    // Главные диагонали, 4 и 5 символов
                    {0, 6, 12, 18}, {6, 12, 18, 24},
                    {1, 7, 13, 19}, {5, 11, 17, 23},

                    {0, 6, 12, 18, 24},
                    {1, 7, 13, 19, 25},

                    // Побочные диагонали, 4 и 5 символов
                    {3, 7, 11, 15}, {2, 6, 10, 14},
                    {4, 8, 12, 16}, {8, 12, 16, 20},

                    {4, 8, 12, 16, 20},
                    {3, 7, 11, 15, 19},
                    {2, 6, 10, 14, 18}
            };
        } else {
            throw new IllegalArgumentException("Unsupported board size: " + boardSize);
        }
    }


    private static boolean isDraw(char[] board) {
        for (char cell : board) {
            if (cell == ' ') {
                return false; // Есть свободная клетка
            }
        }
        return true;
    }

    private static int findBestMove(char[] board, char serverSymbol, char playerSymbol, int boardSize) {
        int bestScore = Integer.MIN_VALUE;
        int move = -1;

        for (int i = 0; i < board.length; i++) {
            if (board[i] == ' ') {
                board[i] = serverSymbol;
                int score = minimax(board, false, serverSymbol, playerSymbol, boardSize, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 5);
                board[i] = ' ';

                if (score > bestScore) {
                    bestScore = score;
                    move = i;
                }
            }
        }

        return move;
    }

    private static int minimax(char[] board, boolean isMaximizing, char serverSymbol, char playerSymbol, int boardSize, int alpha, int beta, int depth, int maxDepth) {
        if (checkWinner(board, boardSize, serverSymbol)) {
            return 10 - depth;
        }
        if (checkWinner(board, boardSize, playerSymbol)) {
            return depth - 10;
        }
        if (isDraw(board) || depth == maxDepth) {
            return 0;
        }

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < board.length; i++) {
                if (board[i] == ' ') {
                    board[i] = serverSymbol;
                    int score = minimax(board, false, serverSymbol, playerSymbol, boardSize, alpha, beta, depth + 1, maxDepth);
                    board[i] = ' ';
                    bestScore = Math.max(score, bestScore);
                    alpha = Math.max(alpha, bestScore);
                    if (beta <= alpha) {
                        break; // Отсечение
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < board.length; i++) {
                if (board[i] == ' ') {
                    board[i] = playerSymbol;
                    int score = minimax(board, true, serverSymbol, playerSymbol, boardSize, alpha, beta, depth + 1, maxDepth);
                    board[i] = ' ';
                    bestScore = Math.min(score, bestScore);
                    beta = Math.min(beta, bestScore);
                    if (beta <= alpha) {
                        break; // Отсечение
                    }
                }
            }
            return bestScore;
        }
    }
}