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
 * The ChatClientHandler class manages the communication with an individual chat
 * client.
 * It handles incoming messages from the client and broadcasts them to other
 * clients.
 */
public class ClientHandler implements Runnable {
    private final Socket userSocket;
    private final String username;
    private final BufferedReader userInputReader;
    private final BufferedWriter userOutputWriter;
    // ConcurrentHashMap to hold active client handlers
    private static final ConcurrentHashMap<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private static volatile boolean isServerClosing = false;

    /**
     * Constructor for ChatClientHandler.
     * Initialises the client socket and sets up input/output streams.
     *
     * @param socket The socket connected to the client.
     * @throws IOException If an I/O error occurs while setting up streams.
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
     * Main run method for the thread.
     * Continuously listens for client messages and broadcasts them to other
     * clients.
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

    private void sendActiveUsers() {
        synchronized (clientHandlers) {
            String activeUsers = String.join(", ", clientHandlers.keySet());
            deliverMessageToClient(ConsoleColor.ORANGE_BOLD + "[CHAT]:" + ConsoleColor.RESET + " Active users: "
                    + ConsoleColor.CYAN_BOLD + activeUsers + ConsoleColor.RESET);
        }
    }

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
     * Notifies all client handlers of the server shutdown.
     */
    public static void alertServerShutdown() {
        isServerClosing = true;
        for (ClientHandler handler : clientHandlers.values()) {
            handler.deliverMessageToClient(
                    ConsoleColor.RED_BOLD + "Server is shutting down. Please disconnect." + ConsoleColor.RESET);
        }
    }

    /**
     * Disconnects all connected clients.
     * Iterates through all active client handlers and starts their disconnection
     * process.
     */
    public static void terminateAllConnections() {
        for (ClientHandler handler : clientHandlers.values()) {
            handler.terminateConnection();
        }
    }

    /**
     * Disconnects this client from the server.
     * Sends a disconnection message to the client and closes the socket.
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
     * Broadcasts a given message to all connected clients.
     * The message is sent to each client connected to the server.
     *
     * @param message The message to be broadcast.
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
     * Broadcasts a system message to all connected clients.
     *
     * @param systemMessage The system message to be broadcast.
     */
    private void sendSystemNotification(String systemMessage) {
        if (!isServerClosing) {
            for (ClientHandler clientHandler : clientHandlers.values()) {
                if (this != clientHandler) { // Avoid sending the message to the user who just joined
                    clientHandler
                            .deliverMessageToClient(
                                    ConsoleColor.ORANGE_BOLD + "[CHAT]: " + ConsoleColor.RESET + systemMessage);
                }
            }
        }
    }

    /**
     * Sends a message to the connected client.
     * Writes the message to the client's output stream and flushes it to ensure
     * delivery.
     * If an IOException occurs during sending, it closes the resources associated
     * with this client.
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
     * Closes resources associated with this client handler.
     * Ensures that the input reader, output writer, and client socket are closed
     * properly.
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

    private static String getCurrentTimestamp() {
        return "[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "]";
    }

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