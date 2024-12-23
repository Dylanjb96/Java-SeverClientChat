package ie.atu.sw.Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;

import ie.atu.sw.ConsoleDesign.ConsoleColor;
import ie.atu.sw.ConsoleDesign.ConsoleLoadingMeter;
import ie.atu.sw.ConsoleDesign.ConsolePrint;

/**
 * The ChatClient connects to the chat server, allowing user to communicate with
 * others
 * and send private messages.
 */
public class ChatClient {
    private static String serverIP = "127.0.0.1"; // defualt server ip address
    private static int serverConnectionPort = 65534; // default server port
    private static final int RECONNECT_DELAY_MS = 5000; // Delay between reconnection attempts

    /**
     * The main method to start the chat client.
     * It loads configuration, establishes a connection, and handles user input and
     * server messages.
     *
     * @param args Command line arguments which may contain server address and port.
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean connected = false;

            // Main loop to manage retries
            while (!connected) {
                serverConnectionPort = 65534; // Default port

                // Prompt user for server IP address and port
                System.out.println(ConsoleColor.CYAN_BOLD + "Welcome to the Chat App" + ConsoleColor.RESET);
                System.out.println("[Hit " + ConsoleColor.ORANGE_UNDERLINED + "ENTER" + ConsoleColor.RESET
                        + " for default server IP Address: " + ConsoleColor.GREEN_BOLD + serverIP + ConsoleColor.RESET
                        + "]");
                System.out.print(ConsoleColor.GREEN_BOLD + "Enter server IP address: " + ConsoleColor.RESET);
                String inputAddress = scanner.nextLine().trim();
                if (!inputAddress.isEmpty()) {
                    serverIP = inputAddress;
                }

                System.out.println(
                        "[Hit " + ConsoleColor.ORANGE_UNDERLINED + "ENTER" + ConsoleColor.RESET + " for default port: "
                                + ConsoleColor.GREEN_BOLD + serverConnectionPort + ConsoleColor.RESET + "]");
                System.out.print(ConsoleColor.GREEN_BOLD + "Enter server port: " + ConsoleColor.RESET);
                String inputPort = scanner.nextLine().trim();
                if (!inputPort.isEmpty()) {
                    try {
                        int enteredPort = Integer.parseInt(inputPort);
                        if (enteredPort < 1 || enteredPort > 65535) { // Validate port range
                            throw new NumberFormatException("Port out of range.");
                        }
                        serverConnectionPort = enteredPort; // Update serverPort if valid
                    } catch (NumberFormatException e) {
                        ConsolePrint.printWarning("Invalid port number. Defaulting to port 65534.");
                    }
                }

                // Attempt to connect to the server
                System.out.println(ConsoleColor.ORANGE + "Connecting to the chat server..." + ConsoleColor.RESET);
                try {
                    for (int i = 0; i <= 100; i++) {
                        ConsoleLoadingMeter.printProgress(i, 100);
                        Thread.sleep(20); // Simulate loading process
                    }
                    try (Socket socket = publishConnection(serverIP, serverConnectionPort);
                            BufferedReader serverMessageReader = new BufferedReader(
                                    new InputStreamReader(socket.getInputStream()));
                            BufferedWriter clientMessageWriter = new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream()));
                            Scanner userScanner = new Scanner(System.in)) {

                        // Successfully connected
                        System.out.println(ConsoleColor.GREEN_BOLD + "Connected to the Server" + ConsoleColor.RESET);
                        connected = true;

                        // Start a thread to listen for server messages
                        Thread listenerThread = new Thread(() -> readServerMessages(serverMessageReader));
                        listenerThread.start();

                        // Handle user input and send messages to the server
                        processClientInput(userScanner, clientMessageWriter);

                        // Wait for the listener thread to finish
                        listenerThread.join();

                    } catch (IOException e) {
                        ConsolePrint.printError("Unable to connect to the server. " + e.getMessage());
                    }
                } catch (InterruptedException e) {
                    ConsolePrint.printError("Connection attempt interrupted.");
                    Thread.currentThread().interrupt(); // Restore interrupted status
                }
            }

            ConsolePrint.printInfo("Exiting the Chat App. Goodbye!");
        } // Scanner is closed here automatically
    }

    /**
     * Establishes a connection to the chat server, retries if necessary.
     *
     * @return A connected socket to the server.
     * @throws IOException If unable to connect after several attempts.
     */
    private static Socket publishConnection(String serverAddress, int serverPort) throws IOException {
        return new Socket(serverAddress, serverPort);
    }

    private static String getPresentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * Handles user input, sending messages to the server.
     * It reads user input from the console and sends it to the server.
     *
     * @param scanner             A Scanner object for reading user input.
     * @param clientMessageWriter A BufferedWriter to send messages to the server.
     * @throws IOException If an I/O error occurs.
     */
    private static void processClientInput(Scanner scanner, BufferedWriter clientMessageWriter) throws IOException {
        System.out
                .println("[Hit " + ConsoleColor.ORANGE_UNDERLINED + "ENTER" + ConsoleColor.RESET
                        + " for random username]");
        System.out.print(ConsoleColor.GREEN_BOLD + "Enter your username: " + ConsoleColor.RESET);
        String username = scanner.nextLine();

        // Generate a random username if the user clicks ENTER
        if (username.isEmpty()) {
            username = generateRandomUsername();
        }

        clientMessageWriter.write(username);
        clientMessageWriter.newLine();
        clientMessageWriter.flush();

        System.out.println(ConsoleColor.CYAN_BOLD + "Welcome to the Chat, " + username + "!" + ConsoleColor.RESET);
        System.out.println(ConsoleColor.GREEN_BOLD + "Type " + ConsoleColor.ORANGE_UNDERLINED + "'/pm username message'"
                + ConsoleColor.RESET + " to send a private message."
                + ConsoleColor.RESET);
        System.out.println(ConsoleColor.GREEN_BOLD + "Type a message and hit Enter to send." + ConsoleColor.RESET
                + " Type " + ConsoleColor.ORANGE_UNDERLINED + "\\q" + ConsoleColor.RESET + " to quit."
                + ConsoleColor.RESET);
        System.out.println(ConsoleColor.GREEN_BOLD + "Type " + ConsoleColor.RESET + ConsoleColor.ORANGE_BOLD
                + "/help " + ConsoleColor.RESET + ConsoleColor.GREEN_BOLD + "to see Available Commands"
                + ConsoleColor.RESET);

        String userInput;
        while (!(userInput = scanner.nextLine()).equals("\\q")) {
            // Don't prefix with time for private messages
            if (userInput.startsWith("/")) {
                clientMessageWriter.write(userInput.trim());
            } else {
                String message = "[" + getPresentTime() + "] " + userInput;
                clientMessageWriter.write(message);
            }
            clientMessageWriter.newLine();
            clientMessageWriter.flush();
        }
        ConsolePrint.printWarning("You have left the chat.");
        closeResourceQuietly(clientMessageWriter);
    }

    // Helper method to generate a random username
    private static String generateRandomUsername() {
        String[] adjectives = { "Happy", "Cool", "Bright", "Calm", "Fast", "Rude", "Unlucky", "Lucky" };
        String[] nouns = { "Shark", "Tiger", "Wolf", "Hawk", "Bunny" };
        Random random = new Random();

        String adjective = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];
        int number = random.nextInt(1000); // Append a random number to make the username unique

        return adjective + noun + number;
    }

    /**
     * Listens for messages from the server and displays them to the user.
     * It continuously reads messages from the server and prints them to the
     * console.
     *
     * @param serverInput A BufferedReader to read messages from the server.
     */
    private static void readServerMessages(BufferedReader serverMessageReader) {
        try {
            String message;
            while ((message = serverMessageReader.readLine()) != null) {
                if (message.contains(ConsoleColor.RED_BOLD + "Server is full" + ConsoleColor.RESET)) {
                    System.out.println(ConsoleColor.RED + message + ConsoleColor.RESET);
                    System.exit(0); // Exit if the server is full
                } else {
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            ConsolePrint.printError("Disconnected from server.");
            attemptToReconnectServer();
        }
    }

    /**
     * Attempts to reconnect to the server after a disconnection.
     * Provides the user with an option to retry and handles multiple attempts.
     */
    public static void attemptToReconnectServer() {
        Scanner scanner = new Scanner(System.in);
        System.out
                .println(ConsoleColor.YELLOW_BOLD + "You have been disconnected from the server." + ConsoleColor.RESET);
        System.out.println(ConsoleColor.CYAN_BOLD + "Would you like to try reconnecting to the server? (yes/no): "
                + ConsoleColor.RESET);
        String response = scanner.nextLine().trim();

        if (response.equalsIgnoreCase("yes")) {
            boolean connected = false;
            int retryAttempts = 0;
            final int MAXIMUM_RETRIES = 5; // Five attempts to reconnect

            while (!connected && retryAttempts < MAXIMUM_RETRIES) {
                retryAttempts++;
                System.out.println(
                        ConsoleColor.CYAN + "Attempting to reconnect to the server... (Attempt " + retryAttempts
                                + ")" + ConsoleColor.RESET);
                try {
                    // Attempt to establish connection
                    Socket socket = publishConnection(serverIP, serverConnectionPort);
                    BufferedReader serverMessageReader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    BufferedWriter clientMessageWriter = new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream()));

                    // Notify user of successful reconnection
                    ConsolePrint.printInfo("Reconnected to the server successfully!");

                    // Restart listening and user input
                    Thread listenerThread = new Thread(() -> readServerMessages(serverMessageReader));
                    listenerThread.start();

                    processClientInput(scanner, clientMessageWriter);
                    listenerThread.join(); // Wait for the listener thread to finish
                    connected = true;
                } catch (IOException | InterruptedException e) {
                    System.out.println(ConsoleColor.RED + "Reconnection attempt failed: " + e.getMessage()
                            + ConsoleColor.RESET);
                    if (retryAttempts < MAXIMUM_RETRIES) {
                        System.out.println(ConsoleColor.YELLOW_BOLD + "Retrying in " + RECONNECT_DELAY_MS / 1000
                                + " seconds..." + ConsoleColor.RESET);
                    } else {
                        ConsolePrint.printError("Maximum retries has reached. Unable to reconnect.");
                    }
                    try {
                        Thread.sleep(RECONNECT_DELAY_MS);
                    } catch (InterruptedException interruptedException) {
                        System.out.println(ConsoleColor.RED + "Reconnection distrupted." + ConsoleColor.RESET);
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } else {
            ConsolePrint.printInfo("Exiting the chat. Bye!");
            System.exit(0);
        }
    }

    /**
     * Safely closes a resource, handling any IOException that might occur.
     * This method is used to close resources like streams and sockets.
     *
     * @param resource The resource to close.
     */
    private static void closeResourceQuietly(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                ConsolePrint.printError("Failed to close resource properly: " + e.getMessage());
            }
        }
    }
}
