import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ConsoleClient {

    private static final String SERVER_ADDRESS = "localhost";  // Адрес сервера
    private static final int SERVER_PORT = 12345;             // Порт сервера

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the server.");

            while (true) {
                System.out.println("\nChoose an action:");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");
                String choice = scanner.nextLine();

                if ("1".equals(choice)) {
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();

                    // Отправляем запрос на регистрацию
                    writer.println("register:" + username + ":" + password);
                    String response = reader.readLine(); // Читаем ответ от сервера
                    System.out.println("Server response: " + response);

                } else if ("2".equals(choice)) {
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();

                    // Отправляем запрос на вход
                    writer.println("login:" + username + ":" + password);
                    String response = reader.readLine(); // Читаем ответ от сервера
                    System.out.println("Server response: " + response);

                } else if ("3".equals(choice)) {
                    System.out.println("Exiting...");
                    break;

                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            }

        } catch (IOException e) {
            System.out.println("Error connecting to the server: " + e.getMessage());
        }
    }
}

