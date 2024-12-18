import java.sql.*;
import java.io.*;
import java.net.*;

public class Server {
    private static final int PORT = 12345;
    public static final String DB_URL = "jdbc:mysql://localhost:3306/tictactoedb";
    public static final String DB_USER = "root"; // Укажите ваш пользователь базы данных
    public static final String DB_PASSWORD = "12345"; // Укажите ваш пароль базы данных

    public static void main(String[] args) {
        try {
            // Проверка подключения к базе данных
            if (checkDatabaseConnection()) {
                System.out.println("Server connected to the database successfully!");
            } else {
                System.out.println("Failed to connect to the database.");
                return;  // Если не удалось подключиться, выходим из программы
            }

            // Запуск сервера
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server started on port " + PORT);

                while (true) {
                    try {
                        // Принимаем подключение от клиента
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Client connected");

                        // Создаем новый поток для обработки клиента
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        new Thread(clientHandler).start();  // Запускаем новый поток
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для проверки подключения к базе данных
    private static boolean checkDatabaseConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Пытаемся выполнить простой запрос, чтобы убедиться в подключении
            if (conn != null) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        }
        return false;
    }
}