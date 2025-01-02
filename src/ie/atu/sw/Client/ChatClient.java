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
 * The ChatClient class represents a client that connects to the chat server.
 * It enables users to participate in a chat session, communicate with other
 * connected users, and send private messages to specific users. The client
 * manages the connection to the server, handles incoming messages, and provides
 * an interface for sending messages.
 *
 * Key Features:
 * - Connects to a designated chat server.
 * - Receives and displays messages from the server, including system
 * notifications.
 * - Supports sending public and private messages.
 * - Handles disconnection and cleanup upon exit.
 */

public class ChatClient {
    private static String serverIP = "127.0.0.1"; // defualt server ip address
    private static int serverConnectionPort = 65534; // default server port
    private static final int RECONNECT_DELAY_MS = 5000; // Delay between reconnection attempts

    /**
     * The main method serves as the entry point for the ChatClient application.
     * It facilitates the client-side functionality of connecting to a chat server,
     * interacting with other users, and managing user input and server messages.
     *
     * Key Responsibilities:
     * - Loads configuration and sets up a connection to the chat server.
     * - Allows users to specify the server's IP address and port, with defaults
     * available.
     * - Establishes a connection to the server, retries if the connection fails,
     * and handles
     * errors gracefully.
     * - Starts a background thread to listen for and display messages from the
     * server.
     * - Processes user input for sending messages or commands to the server.
     * - Ensures proper cleanup of resources, including closing sockets,
     * input/output streams,
     * and the scanner.
     *
     * The method performs the following steps:
     * 1. Displays a welcome message and prompts the user for server details (IP
     * address and port).
     * 2. Attempts to connect to the server, with a progress indicator for user
     * feedback.
     * 3. If connected:
     * - Starts a thread to listen for server messages.
     * - Allows the user to input messages, which are sent to the server.
     * - Waits for the listener thread to finish before exiting.
     * 4. Handles disconnections and retries if the connection fails.
     * 5. Logs messages to the console for important events like errors, connection
     * status, and exit notifications.
     *
     * @param args Command-line arguments, which may include the server address and
     *             port.
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
     * Establishes a connection to the chat server using the specified server
     * address and port.
     * This method attempts to create a socket connection to the server. If the
     * connection
     * fails, the exception is propagated for handling by the caller.
     *
     * Key Responsibilities:
     * - Uses the provided server address and port to establish a socket connection.
     * - Returns the connected socket for further communication with the server.
     * - Throws an IOException if the connection cannot be established.
     *
     * @param serverAddress The IP address or hostname of the server.
     * @param serverPort    The port number on which the server is listening.
     * @return A connected Socket instance for communicating with the server.
     * @throws IOException If an error occurs during the connection attempt.
     */
    private static Socket publishConnection(String serverAddress, int serverPort) throws IOException {
        return new Socket(serverAddress, serverPort);
    }

    /**
     * Retrieves the current time formatted as a string.
     * The time is obtained from the system's local clock and formatted in the
     * "HH:mm:ss" pattern (e.g., "14:30:15") for consistent and human-readable
     * representation.
     *
     * @return A string representation of the current time in "HH:mm:ss" format.
     */
    private static String getPresentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * Handles user input and sends messages to the server.
     * This method reads user input from the console and transmits it to the server
     * using the provided BufferedWriter. It supports sending standard messages,
     * private messages, and commands. The user can quit the chat by typing '\q'.
     *
     * Key Responsibilities:
     * - Prompts the user to enter a username or generates a random username if none
     * is provided.
     * - Sends the username to the server for identification.
     * - Displays instructions for using the chat interface.
     * - Reads user input continuously, formats the input with a timestamp for
     * standard messages,
     * and sends it to the server.
     * - Handles private messages and commands differently, sending them directly
     * without timestamps.
     * - Allows the user to quit the chat by typing '\q' and closes resources upon
     * exit.
     *
     * @param scanner             A Scanner object for reading user input from the
     *                            console.
     * @param clientMessageWriter A BufferedWriter for sending messages to the
     *                            server.
     * @throws IOException If an I/O error occurs while writing to the server.
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

    /**
     * Helper method to generate a random username.
     * This method creates a unique and fun username by combining a randomly
     * selected adjective and noun from predefined arrays, followed by a
     * randomly generated number. The resulting username is intended to provide
     * variety and ensure uniqueness.
     *
     * Key Steps:
     * - Randomly selects an adjective from the predefined `adjectives` array.
     * - Randomly selects a noun from the predefined `nouns` array.
     * - Appends a random integer (0-999) to the adjective-noun combination to
     * ensure uniqueness.
     *
     * Example:
     * - Output: "HappyWolf527", "CoolTiger123"
     *
     * @return A randomly generated username as a String.
     */
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
     * This method runs in a loop, continuously reading messages from the server
     * using a BufferedReader and printing them to the console. It handles special
     * server messages, such as when the server is full, and attempts to reconnect
     * if disconnected.
     *
     * Key Responsibilities:
     * - Reads messages from the server until the connection is closed or an error
     * occurs.
     * - Prints each received message to the console.
     * - Checks for specific server messages (e.g., "Server is full") and performs
     * appropriate actions, such as exiting the application.
     * - Logs and handles disconnection scenarios by attempting to reconnect.
     *
     * @param serverMessageReader A BufferedReader for reading messages from the
     *                            server.
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
     * This method provides the user with the option to retry reconnecting to the
     * server
     * and handles multiple reconnection attempts with a delay between retries. If
     * the
     * user opts not to reconnect or the maximum number of retries is reached, the
     * application exits gracefully.
     *
     * Key Responsibilities:
     * - Informs the user about the disconnection and asks if they want to
     * reconnect.
     * - Handles up to a maximum number of retry attempts with a delay between
     * attempts.
     * - Establishes a new connection to the server if a retry is successful.
     * - Restarts the input and message listening processes upon successful
     * reconnection.
     * - Handles errors during reconnection attempts and logs them to the console.
     * - Exits the application if the user opts out of reconnecting or if retries
     * fail.
     *
     * The method performs the following steps:
     * 1. Prompts the user for a decision to reconnect.
     * 2. If the user opts to reconnect:
     * - Attempts to reconnect to the server, retrying up to `MAXIMUM_RETRIES`
     * times.
     * - Prints progress and logs any errors encountered during retries.
     * - If successful, re-establishes the input and listener processes.
     * 3. If the user opts not to reconnect or retries fail:
     * - Exits the application with a farewell message.
     *
     * @throws InterruptedException If the thread is interrupted during a sleep
     *                              delay.
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
     * Safely closes a resource, ensuring that any IOException encountered
     * during the closing process is caught and logged without interrupting
     * the program's flow. This method is commonly used to release resources
     * such as streams and sockets gracefully.
     *
     * Key Responsibilities:
     * - Checks if the provided resource is non-null before attempting to close it.
     * - Calls the `close` method on the resource.
     * - Logs an error message if an IOException occurs during the closing process.
     *
     * @param resource The resource to close. It must implement the `Closeable`
     *                 interface.
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
