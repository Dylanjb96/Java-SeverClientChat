package ie.atu.sw.Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ie.atu.sw.ConsoleDesign.ConsoleColor;
import ie.atu.sw.ConsoleDesign.ConsoleLoadingMeter;
import ie.atu.sw.ConsoleDesign.ConsolePrint;

/**
 * ChatServer class responsible for managing a multi-threaded chat server.
 * This server listens for incoming client connections on a designated port
 * and delegates the handling of each client to a separate ChatClientHandler
 * thread.
 */

public class ChatServer {
    private static final Set<ClientHandler> connectedClients = Collections.synchronizedSet(new HashSet<>());
    private static final ExecutorService clientHandlerThreadPool = Executors.newFixedThreadPool(4);
    private static volatile boolean isServerActive = true;
    private static int serverPort = 65534; // Default port
    private static final int MAXIMUM_CLIENTS = 4; // Maximum number of clients

    /**
     * Entry point for launching the chat server application.
     * Configures the server, initializes the server socket, and begins
     * accepting client connections.
     */
    public static void main(String[] args) {
        // Load server settings from the configuration properties file
        initializeConfiguration();

        // Use the server port from command line arguments if specified, overriding
        // default settings
        if (args.length > 0) {
            try {
                serverPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                ConsolePrint.printError("Invalid command line port number. Using port from config file.");
            }
        }

        try {
            ServerSocket serverSocket = startServer();
            initiateServerShutdown(serverSocket);
            confirmingClientConnections(serverSocket);
        } catch (IOException | InterruptedException e) {
            ConsolePrint.printError("Server error: " + e.getMessage());
        }
    }

    /**
     * Reads and applies the server configuration from a properties file.
     * Sets the server port from the configuration file or defaults to a predefined
     * value.
     */
    private static void initializeConfiguration() {
        Properties properties = new Properties();
        String configFilePath = "src/ie/atu/sw/Configuration/configuration.properties";

        System.out.println(ConsoleColor.ORANGE_BOLD + "Loading Configuration File" + ConsoleColor.RESET);

        int totalSteps = 50; // Define the total steps for the progress meter
        for (int i = 0; i <= totalSteps; i++) {
            try {
                Thread.sleep(20); // Simullate the progress meter with a delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ConsoleLoadingMeter.printProgress(i, totalSteps);
        }

        // Ensure the file exists before loading
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            ConsolePrint.printWarning(
                    "configuration.properties file not found at " + configFilePath + ". Using default settings.");
            serverPort = 65534; // Default port if file not found
            return;
        }

        try (InputStream input = new FileInputStream(configFile)) {
            // Load properties from the file
            properties.load(input);

            // Retrieve server port value from properties, default to 65534 if not present
            serverPort = Integer.parseInt(properties.getProperty("server_port", "65534"));

            // Log the loaded port for debugging
            ConsolePrint.printInfo("Server port loaded: " + serverPort);

        } catch (IOException e) {
            ConsolePrint.printError("Error reading configuration file: " + e.getMessage());
        } catch (NumberFormatException e) {
            ConsolePrint.printError("Invalid port number in configuration. Using default port.");
            serverPort = 65534; // Default port in case of an invalid value in the configuration
        }
    }

    /**
     * Creates and initializes the server socket for the chat server.
     * Displays startup messages to indicate successful initialization.
     *
     * @return The initialized ServerSocket instance.
     * @throws IOException          If an error occurs during the initialization of
     *                              the server socket.
     * @throws InterruptedException If the thread is interrupted during the
     *                              initialization process.
     */

    private static ServerSocket startServer() throws IOException, InterruptedException {
        System.out.println(ConsoleColor.ORANGE_BOLD + "Chat Server is initializing..." + ConsoleColor.RESET);

        int totalSteps = 100;
        for (int i = 0; i <= totalSteps; i++) {
            Thread.sleep(20); // Simulate server setup
            ConsoleLoadingMeter.printProgress(i, totalSteps);
        }

        ServerSocket serverSocket = new ServerSocket(serverPort); // If no IP address is specified, default to using
                                                                  // localhost

        System.out.println(ConsoleColor.GREEN_BOLD + "Server is running on port " + ConsoleColor.RESET
                + ConsoleColor.ORANGE_BOLD + serverPort + ConsoleColor.RESET);
        System.out.println(
                "Press " + ConsoleColor.ORANGE_UNDERLINED + "\\q" + ConsoleColor.RESET + " to end the server.");
        return serverSocket;
    }

    /**
     * Continuously listens for and accepts incoming client connections.
     * Each connection is handled by a dedicated ChatClientHandler instance.
     * This process runs as long as the server remains active.
     *
     * @param serverSocket The ServerSocket instance used to accept incoming
     *                     connections.
     */

    private static void confirmingClientConnections(ServerSocket serverSocket) {
        while (isServerActive) {
            try {
                Socket clientSocket = serverSocket.accept(); // Accept connection once

                if (connectedClients.size() < MAXIMUM_CLIENTS) {
                    System.out.println(
                            ConsoleColor.GREEN_BOLD + "[" + getPresentTime() + "] Connection established with: "
                                    + clientSocket.getInetAddress().getHostAddress() + ConsoleColor.RESET);

                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    connectedClients.add(clientHandler);
                    clientHandlerThreadPool.execute(clientHandler); // Handle client in a separate thread
                } else {
                    // Notify client that the server is full
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    writer.write(
                            ConsoleColor.RED_BOLD + "Server is full. Please try again later." + ConsoleColor.RESET);
                    writer.newLine();
                    writer.flush();
                    clientSocket.close(); // Close the connection
                    ConsolePrint.printWarning(
                            "Connection rejected from: " + clientSocket.getInetAddress().getHostAddress() +
                                    " - Server is full.");
                }
            } catch (IOException e) {
                if (isServerActive) {
                    ConsolePrint.printError("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     * The message is timestamped with the current server time before being sent.
     * Uses a synchronized block to ensure thread-safe access to the list of
     * connected clients.
     *
     * @param message The message to broadcast to all connected clients.
     */
    public void broadcastMessage(String message) {
        String timestampedMessage = "[" + getPresentTime() + "] " + message;
        synchronized (connectedClients) {
            for (ClientHandler clientHandler : connectedClients) {
                clientHandler.deliverMessageToClient(timestampedMessage);
            }
        }
    }

    /**
     * Retrieves the current server time formatted as a string.
     * The time is formatted in the "HH:mm:ss" pattern (e.g., "14:30:15").
     *
     * @return A string representation of the current time.
     */
    private static String getPresentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /**
     * Launches a separate thread to monitor the console for a shutdown command.
     * Entering '\q' triggers the server shutdown process.
     *
     * @param serverSocket The ServerSocket instance to close during shutdown.
     */

    private static void initiateServerShutdown(ServerSocket serverSocket) {
        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (isServerActive) {
                    if ("\\q".equals(scanner.nextLine())) {
                        closeServer(serverSocket);
                    }
                }
            }
        }).start();
    }

    /**
     * Gracefully shuts down the chat server by closing all client connections
     * and the server socket. Ensures that all resources associated with the
     * server are released.
     *
     * @param serverSocket The ServerSocket instance to be closed during shutdown.
     * @throws IOException If an error occurs while closing the server socket.
     */

    private static void closeServer(ServerSocket serverSocket) {
        try {
            isServerActive = false; // Stop the server loop
            ClientHandler.alertServerShutdown(); // Notify all clients about the shutdown
            ClientHandler.terminateAllConnections(); // Disconnect all active clients
            if (!serverSocket.isClosed()) {
                serverSocket.close(); // Release the server socket
            }
            clientHandlerThreadPool.shutdownNow(); // Terminate all client handler threads
            ConsolePrint.printInfo("Server has been successfully shut down.");
        } catch (IOException e) {
            ConsolePrint.printError("Error while shutting down the server: " + e.getMessage());
        }
    }

}