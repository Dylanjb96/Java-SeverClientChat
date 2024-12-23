# **Multi-User Chat Application**

## **Design Overview**
This project implements a multi-user chat application that supports concurrent clients connecting to a server. Users can send public messages, private messages, and view a list of active users. The system is designed using Java with a focus on multithreading and robust error handling.

### **Key Features**
- **Multi-User Support**: Up to four clients can connect to the server simultaneously.
- **Private Messaging**: Clients can send direct messages to other users with the `/pm` command.
- **Active Users Listing**: Use the `/users` command to see the list of connected users.
- **Server Commands**: Server admin can shut down the server using `\q`.
- **Server Limit**: If the maximum number of users is reached, additional clients receive a "Server is full" message.
- **Customizable Configuration**: Server settings (port) are loaded from a `configuration.properties` file.
- **Reconnection Support**: Clients can retry connecting if the server is unavailable.
- **Custom Console Formatting**: Colors and formatting enhance user experience.
- **Progress Bar**: Visual loading bar for better user feedback during the connection process.

---

## **Architecture**
The application follows a **client-server architecture**:
1. **Server**:
   - Manages client connections using threads.
   - Shows current time when client connects and disconnects
   - Broadcasts messages to all connected clients or specific users.
   - Handles commands like `/pm`, `/users`, and `/help`.
   - Limits the number of concurrent connections.
2. **Client**:
   - Connects to the server and allows users to send/receive messages.
   - Shows current time when messages are received and sent.
   - Displays server responses and command outputs.
   - Reconnects to the server in case of disconnections.
3. **Console**: 
   - Provide a console-based interface with colours and formatting.
   - A visually appealing loading bar is displayed while the application processes connections, giving users a real-time status update.

---

## **Classes**
### **Server Components**
- **`ChatServer`**: Manages server startup, client connections, and shutdown.
- **`ClientHandler`**: Handles communication with individual clients.

### **Client Components**
- **`ChatClient`**: Manages user input, server connection, and message handling.

### **ConsoleDesign**
- **`ConsoleColor`**: Provides color formatting for console output.
- **`ConsoleLoadingMeter`**: Simulates a loading progress meter.
- **`ConsolePrint`**: Prints formatted messages with predefined styles.

---

## **Multithreading**
The server uses a fixed thread pool to manage multiple client connections efficiently. Each client connection runs in its own thread, allowing simultaneous communication.

---

## **Error Handling**
- Invalid input handling for commands and port numbers.
- Graceful disconnection when the server shuts down or a client exits.
- Server full notification when the connection limit is reached.

---

## **Configuration**
The application reads settings (e.g., server port) from a `configuration.properties` file. Default values are used if the file is missing or contains invalid data.

---

## **Instructions for Running the Application**
### **Running the Server**
1. Open the project in your preferred IDE (e.g., IntelliJ IDEA, Eclipse, or VS Code).
2. Navigate to the `ChatServer` class in the `ie.atu.sw.Server` package.
3. Click the `Run` button (or equivalent in your IDE) to start the server.
4. The server will initialize and run on the port specified in the `configuration.properties` file. If the file is missing or invalid, a default port (65534) will be used.

### **Running the Client**
1. Open the project in your preferred IDE (e.g., IntelliJ IDEA, Eclipse, or VS Code).
2. Navigate to the `ChatClient` class in the `ie.atu.sw.Client` package.
3. Click the `Run` button (or equivalent in your IDE) to start the client.
4. Follow the on-screen instructions:
   - Press `ENTER` to use the default server IP and port.
   - Provide a custom IP address or port if required.
5. Once connected, you can:
   - Type `/pm username message` to send private messages.
   - Type `/users` to see the list of active users.
   - Type `/help` for a list of commands.
   - Type `\q` to exit the chat.

---

## **References**
- Java Socket Programming [Oracle Documentation](https://docs.oracle.com/javase/tutorial/networking/sockets/index.html)
- Multithreading in Java [Baeldung](https://www.baeldung.com/java-multithreading)
- Console Color and Loading Meter Output(ConsoleColor, ConsolePrint & ConsoleLoadingMeter): Taken from Dr.John Healy