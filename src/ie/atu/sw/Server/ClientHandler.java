package ie.atu.sw.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import ie.atu.sw.ConsoleDesign.ConsoleColor;
import ie.atu.sw.ConsoleDesign.ConsolePrint;

/**
 * The ChatClientHandler class manages communication with a single chat client.
 * It is responsible for receiving messages from the client, processing them,
 * and broadcasting them to all other connected clients. Each client connection
 * is handled by a separate instance of this class, running in its own thread.
 */

public class ClientHandler implements Runnable {
    private final Socket userSocket;
    private final String username;
    private final BufferedReader userInputReader;
    private final BufferedWriter userOutputWriter;
    // A ConcurrentHashMap to store active client handlers, allowing thread-safe
    // access and management of connected clients.
    private static final ConcurrentHashMap<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private static volatile boolean isServerClosing = false;

    /**
     * Constructs a ChatClientHandler instance for managing communication with a
     * client.
     * Initializes the client socket and sets up the necessary input and output
     * streams
     * for data exchange.
     *
     * @param socket The socket representing the connection to the client.
     * @throws IOException If an error occurs while initializing the input/output
     *                     streams.
     */

    public ClientHandler(Socket socket) throws IOException {
        this.userSocket = socket;
        this.userInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.userOutputWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.username = userInputReader.readLine();
        if (this.username == null || this.username.trim().isEmpty()) {
            throw new IOException("Invalid username received");
        }
        clientHandlers.put(this.username, this);
        sendSystemNotification(ConsoleColor.CYAN_BOLD + username + ConsoleColor.RESET + " has joined the chat.");
    }

    /**
     * The main execution method for the client handler thread.
     * Continuously listens for incoming messages from the client and broadcasts
     * them to all other connected clients. Handles client disconnection and
     * resource cleanup when the client disconnects or an error occurs.
     */

    @Override
    public void run() {
        String message;
        try {
            // Show the user's connection to the server console
            System.out.println(
                    ConsoleColor.CYAN_BOLD + username + ConsoleColor.RESET + " has connected to the server.");

            while ((message = userInputReader.readLine()) != null && !userSocket.isClosed()) {
                if (message.startsWith("/pm")) {
                    System.out.println(ConsoleColor.SUNRISE_BOLD + "Private Message Mode Activated: "
                            + ConsoleColor.RESET + message);
                    handlePrivateMessage(message);
                } else if (message.equalsIgnoreCase("/users")) {
                    sendActiveUsers();
                } else if (message.equalsIgnoreCase("/help")) {
                    sendHelp();
                } else {
                    sendToAllClients(ConsoleColor.CYAN_BOLD + username + ConsoleColor.RESET + ": " + message);
                }
            }
        } catch (SocketException e) {
            if (!isServerClosing) {
                ConsolePrint.printError("Unexpected client disconnection: " + username + e.getMessage());
            }
        } catch (IOException e) {
            ConsolePrint.printError("Error in client communication: " + e.getMessage());
        } finally {
            handleClientDisconnection();
        }
    }

    /**
     * Handles the disconnection of a client from the chat server.
     * Ensures that all resources associated with the client are released,
     * notifies other clients about the disconnection, and removes the client
     * from the list of active handlers.
     * 
     * The method performs the following steps:
     * - Releases the input/output streams and the socket associated with the
     * client.
     * - Sends a system notification to all connected clients indicating that the
     * user has left.
     * - Removes the client from the synchronized list of active client handlers.
     * 
     * Any errors encountered during the disconnection process are logged for
     * debugging purposes.
     */
    private void handleClientDisconnection() {
        try {
            releaseResources();
            sendSystemNotification(
                    ConsoleColor.CYAN_BOLD + username + ConsoleColor.RESET + " has left the chat.");
            synchronized (clientHandlers) {
                clientHandlers.remove(username);
            }

        } catch (Exception e) {
            ConsolePrint.printError("Error handling client disconnection: " + e.getMessage());
        }
    }

    /**
     * Handles the delivery of a private message from one client to another.
     * Extracts the recipient's username and the message content from the provided
     * string,
     * and delivers the message to the recipient if they are currently online.
     * Notifies the sender if the recipient is not found or if the message format is
     * invalid.
     *
     * @param message The private message command containing the recipient's
     *                username and the message content.
     * @throws IOException If an error occurs while sending the message.
     */
    private void handlePrivateMessage(String message) throws IOException {
        // Remove the '/pm' prefix and trim the messsage
        String content = message.substring(4).trim();
        int spaceIndex = content.indexOf(" ");
        if (spaceIndex > 0) {
            String recipient = content.substring(0, spaceIndex); // Grab the recipent's username
            String privateMessage = content.substring(spaceIndex + 1); // Grab the private message

            // Find the recipent's handler
            ClientHandler recipientHandler = clientHandlers.get(recipient);
            // Send the message to the recipent and notify the sender
            if (recipientHandler != null) {
                recipientHandler.deliverMessageToClient("[Private Message from " + ConsoleColor.CYAN_BOLD + username
                        + ConsoleColor.RESET + "] " + privateMessage);
                deliverMessageToClient(
                        "[Private Message to " + ConsoleColor.CYAN_BOLD + recipient + ConsoleColor.RESET + "] "
                                + privateMessage);
            } else {
                // Notify the sender if the recipient is not found
                deliverMessageToClient(
                        ConsoleColor.RED_BOLD + "[ERROR] " + ConsoleColor.RESET + "User " + ConsoleColor.CYAN_BOLD
                                + recipient + ConsoleColor.RESET + " not found.");
            }
        } else {
            // Notify the sender of invalid format
            deliverMessageToClient(
                    ConsoleColor.RED_BOLD + "Invalid private message format. (Use '/pm username message')"
                            + ConsoleColor.RESET);
        }
    }

    /**
     * Sends a list of all currently active users to the client.
     * Retrieves the usernames of all connected clients from the synchronized map
     * and formats them into a comma-separated string.
     */
    private void sendActiveUsers() {
        synchronized (clientHandlers) {
            String activeUsers = String.join(", ", clientHandlers.keySet());
            deliverMessageToClient(ConsoleColor.ORANGE_BOLD + "[CHAT]:" + ConsoleColor.RESET + " Active users: "
                    + ConsoleColor.CYAN_BOLD + activeUsers + ConsoleColor.RESET);
        }
    }

    /**
     * Sends a help message to the client, listing all available commands.
     * Provides detailed instructions for commands such as listing users, sending
     * private messages,
     * requesting help, and quitting the chat.
     */
    private void sendHelp() {
        String helpMessage = ConsoleColor.CYAN_BOLD_BRIGHT + "Available Commands:" + ConsoleColor.RESET + "\n" +
                ConsoleColor.ORANGE_BOLD + "/users" + ConsoleColor.RESET +
                " - List all connected users\n" +
                ConsoleColor.ORANGE_BOLD + "/pm username message" + ConsoleColor.RESET +
                " - Send a private message to a specific user\n" +
                ConsoleColor.ORANGE_BOLD + "/help" + ConsoleColor.RESET +
                " - Display this help message\n" +
                ConsoleColor.ORANGE_BOLD + "\\q" + ConsoleColor.RESET +
                " - Quit the chat\n";
        deliverMessageToClient(helpMessage);
    }

    /**
     * Alerts all connected clients about the impending server shutdown.
     * Sets the server's shutdown flag to true and sends a shutdown notification
     * message to all active client handlers. This message instructs clients to
     * disconnect from the server gracefully.
     */
    public static void alertServerShutdown() {
        isServerClosing = true;
        for (ClientHandler handler : clientHandlers.values()) {
            handler.deliverMessageToClient(
                    ConsoleColor.RED_BOLD + "Server is shutting down. Please disconnect." + ConsoleColor.RESET);
        }
    }

    /**
     * Disconnects all currently connected clients from the server.
     * Iterates through all active client handlers and initiates their
     * disconnection process to ensure that all client connections are
     * gracefully terminated.
     */
    public static void terminateAllConnections() {
        for (ClientHandler handler : clientHandlers.values()) {
            handler.terminateConnection();
        }
    }

    /**
     * Disconnects the client from the server.
     * Sends a disconnection message to the client, notifying them of the server
     * shutdown,
     * and then closes the client's socket to release resources.
     *
     * The method performs the following steps:
     * - Checks if the client's socket is open.
     * - Sends a message to the client informing them about the disconnection and
     * advises them to exit the chat.
     * - Closes the client socket to terminate the connection.
     * - Handles any potential I/O errors during the disconnection process and logs
     * them.
     */
    private void terminateConnection() {
        try {
            if (userSocket != null && !userSocket.isClosed()) {
                userOutputWriter
                        .write(ConsoleColor.RED_BOLD + "[ERROR] Server is shutting down, disconnecting... press "
                                + ConsoleColor.ORANGE_UNDERLINED + "\\q" + ConsoleColor.RESET + " to exit");
                userOutputWriter.newLine();
                userOutputWriter.flush();
                userSocket.close();
            }
        } catch (IOException e) {
            ConsolePrint.printError("Error closing client socket: " + e.getMessage());
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     * Sends the provided message to each active client, ensuring that the sender
     * receives a personalized message prefixed with "Me:" to indicate that they
     * sent it. Handles any errors that occur during the message delivery process.
     *
     * The method performs the following:
     * - Iterates through all connected client handlers.
     * - If the client is the sender, modifies the message to prefix "Me: ".
     * - Sends the message to each client through their respective output writer.
     * - If an error occurs while sending, logs the error and releases the client's
     * resources.
     *
     * @param message The message to broadcast to all connected clients.
     */
    private void sendToAllClients(String message) {
        for (ClientHandler clientHandler : clientHandlers.values()) {
            try {
                if (!clientHandler.userSocket.isClosed()) {
                    String formattedMessage;
                    if (this.username.equals(clientHandler.username)) {
                        // If the sender is the same as the receiver, prefix with "Me: "
                        formattedMessage = ConsoleColor.PURPLE_BOLD + "Me: " + ConsoleColor.RESET
                                + message.substring(message.indexOf(":") + 2);
                    } else {
                        // Otherwise, send the message as is
                        formattedMessage = message;
                    }
                    clientHandler.userOutputWriter.write(formattedMessage);
                    clientHandler.userOutputWriter.newLine();
                    clientHandler.userOutputWriter.flush();
                }
            } catch (IOException e) {
                ConsolePrint.printError("Error sending message: " + e.getMessage());
                clientHandler.releaseResources();
            }
        }
    }

    /**
     * Broadcasts a chat message to all connected clients, except for the user
     * who triggered the notification. This ensures that the chat message, such
     * as a user joining or leaving, is shared appropriately without redundancy.
     * 
     * The method performs the following:
     * - Checks if the server is not in the process of shutting down.
     * - Iterates through all active client handlers.
     * - Sends the system message to each client except the one associated with this
     * handler.
     * - Formats the message with a "[CHAT]" prefix and styles it using console
     * colors.
     *
     * @param chatMessage The chat message to be broadcast to all connected
     *                    clients.
     */
    private void sendSystemNotification(String chatMessage) {
        if (!isServerClosing) {
            for (ClientHandler clientHandler : clientHandlers.values()) {
                if (this != clientHandler) { // Avoid sending the message to the user who just joined
                    clientHandler
                            .deliverMessageToClient(
                                    ConsoleColor.ORANGE_BOLD + "[CHAT]: " + ConsoleColor.RESET + chatMessage);
                }
            }
        }
    }

    /**
     * Sends a message directly to the connected client.
     * This method writes the given message to the client's output stream and
     * flushes
     * it to ensure immediate delivery. If an IOException occurs during the process,
     * it handles the exception by logging the error and releasing all resources
     * associated with the client to prevent further issues.
     *
     * The method performs the following:
     * - Checks if the client's socket is still open.
     * - Writes the message to the output stream, appends a newline, and flushes the
     * stream.
     * - Logs any IOException that occurs and cleans up resources associated with
     * the client.
     *
     * @param message The message to be sent to the client.
     */
    public void deliverMessageToClient(String message) {
        try {
            if (!userSocket.isClosed()) {
                userOutputWriter.write(message);
                userOutputWriter.newLine();
                userOutputWriter.flush();
            }
        } catch (IOException e) {
            ConsolePrint.printError("Error sending message to: " + username + ": " + e.getMessage());
            releaseResources();
        }
    }

    /**
     * Releases all resources associated with this client handler to ensure a
     * clean disconnection. This method properly closes the input reader, output
     * writer, and client socket, handling any exceptions during the process.
     * 
     * Additionally, it removes the client from the synchronized list of active
     * client handlers and logs the disconnection, including the client’s username
     * and socket information. Afterward, it updates the list of active users.
     *
     * The method performs the following:
     * - Closes the input reader, output writer, and socket, if they are not null.
     * - Catches and logs any IOException that occurs during resource cleanup.
     * - Synchronizes access to the `clientHandlers` map to remove the client
     * safely.
     * - Logs the disconnection event and displays the updated list of active users.
     */
    private void releaseResources() {
        try {
            if (userInputReader != null)
                userInputReader.close();
            if (userOutputWriter != null)
                userOutputWriter.close();
            if (userSocket != null)
                userSocket.close();
        } catch (IOException e) {
            ConsolePrint.printError("Error closing resources for " + username + ": " + e.getMessage());
        }

        synchronized (clientHandlers) {
            if (clientHandlers.containsKey(username)) { // Avoid duplicate removal and logging
                clientHandlers.remove(username);
                ConsolePrint.printError(getCurrentTimestamp() + " " + username +
                        " (" + userSocket.getInetAddress() + ":" + userSocket.getPort() + ") has disconnected.");
                printActiveUsers();
            }
        }
    }

    /**
     * Generates a timestamp representing the current time.
     * The timestamp is formatted in the "HH:mm:ss" pattern (e.g., "[14:30:15]")
     * to provide a consistent and human-readable format for logging or displaying
     * events in the chat server.
     *
     * @return A string representation of the current time enclosed in square
     *         brackets.
     */
    private static String getCurrentTimestamp() {
        return "[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "]";
    }

    /**
     * Prints a list of all currently connected users to the server console.
     * If no users are connected, it displays a message indicating that there are
     * no active users. Otherwise, it iterates through the list of connected users
     * and displays their usernames in a formatted style.
     *
     * The method performs the following:
     * - Checks if the `clientHandlers` map is empty. If so, prints a message
     * indicating no active users.
     * - If there are active users, synchronizes access to the `clientHandlers` map
     * to ensure thread safety.
     * - Iterates through the map keys (usernames) and prints each username in a
     * formatted style.
     */
    private void printActiveUsers() {
        if (clientHandlers.isEmpty()) {
            System.out.println(ConsoleColor.ORANGE_BOLD + "No active users." + ConsoleColor.RESET);
            return;
        }
        System.out.println(ConsoleColor.CYAN_BOLD + "Currently connected users:" + ConsoleColor.RESET);
        synchronized (clientHandlers) {
            for (String username : clientHandlers.keySet()) {
                System.out.println(ConsoleColor.CYAN_BOLD + "- " + username + ConsoleColor.RESET);
            }
        }
    }

}