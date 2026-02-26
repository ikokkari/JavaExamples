import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * Simple UDP-based chat server demonstrating datagram networking.
 *
 * WARNING: This is a teaching example. UDP is unsuitable for production chat
 * because it provides no reliability, ordering, or delivery guarantees.
 * Production systems should use TCP or implement reliability on top of UDP.
 *
 * Protocol:
 * - @JOIN nickname - Register with server
 * - @QUIT - Leave chat
 * - any other text - Broadcast to all clients
 *
 * Limitations:
 * - Messages limited to 512 bytes
 * - No reliability (UDP can drop packets)
 * - No message ordering guarantees
 * - No encryption
 * - Basic nickname collision handling
 */
public class ChatServer implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());
    private static final int PACKET_SIZE = 512;
    private static final int MAX_NICKNAME_LENGTH = 20;
    private static final int RECEIVE_BUFFER_SIZE = 65536; // OS buffer for incoming packets

    private final DatagramSocket serverSocket;
    private final ExecutorService executor;
    private final Map<ClientAddress, Client> clients; // Thread-safe map
    private volatile boolean running = true;

    /**
     * Represents a registered chat client.
     */
    private static class Client {
        final String nickname;
        final InetAddress address;
        final int port;
        final Instant joinedAt;

        Client(String nickname, InetAddress address, int port) {
            this.nickname = nickname;
            this.address = address;
            this.port = port;
            this.joinedAt = Instant.now();
        }

        @Override
        public String toString() {
            return nickname + "@" + address.getHostAddress() + ":" + port;
        }
    }

    /**
     * Composite key for identifying clients by address and port.
     */
    private static class ClientAddress {
        final InetAddress address;
        final int port;

        ClientAddress(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ClientAddress)) return false;
            ClientAddress other = (ClientAddress) o;
            return address.equals(other.address) && port == other.port;
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, port);
        }
    }

    /**
     * Creates a chat server on the specified port.
     */
    public ChatServer(int port) throws IOException {
        // Create UDP socket
        this.serverSocket = new DatagramSocket(port);

        // Increase receive buffer to handle bursts (default is often too small)
        this.serverSocket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);

        // Use concurrent map for thread-safe client management
        this.clients = new ConcurrentHashMap<>();

        // Single thread for receiving packets (UDP is sequential)
        // Could use multiple threads, but need careful synchronization
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ChatServer-Receiver");
            t.setDaemon(false); // Want to process remaining packets on shutdown
            return t;
        });

        LOGGER.info("ChatServer started on port " + port);

        // Start receiving packets
        executor.submit(this::receiveLoop);
    }

    /**
     * Main packet receiving loop.
     */
    private void receiveLoop() {
        byte[] buffer = new byte[PACKET_SIZE];

        while (running) {
            try {
                // Create fresh packet for each receive (avoid buffer reuse issues)
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Block waiting for packet
                serverSocket.receive(packet);

                // Process packet
                handlePacket(packet);

            } catch (SocketException e) {
                // Socket closed during shutdown - expected
                if (running) {
                    LOGGER.log(Level.SEVERE, "Socket error", e);
                }
                break;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error receiving packet", e);
            } catch (Exception e) {
                // Catch-all to prevent loop from crashing
                LOGGER.log(Level.SEVERE, "Unexpected error in receive loop", e);
            }
        }

        LOGGER.info("Receive loop terminated");
    }

    /**
     * Process a received datagram packet.
     */
    private void handlePacket(DatagramPacket packet) {
        try {
            // Extract message with explicit encoding
            byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            String message = new String(data, StandardCharsets.UTF_8).trim();

            // Validate message
            if (message.isEmpty()) {
                LOGGER.fine("Received empty packet");
                return;
            }

            InetAddress senderAddress = packet.getAddress();
            int senderPort = packet.getPort();
            ClientAddress clientAddr = new ClientAddress(senderAddress, senderPort);

            LOGGER.fine("Received from " + senderAddress + ":" + senderPort + ": " + message);

            // Handle commands
            if (message.startsWith("@JOIN ")) {
                handleJoin(clientAddr, message.substring(6).trim());
            }
            else if (message.equals("@QUIT")) {
                handleQuit(clientAddr);
            }
            else if (message.startsWith("@")) {
                // Unknown command
                sendToClient(clientAddr, "ERROR: Unknown command");
            }
            else {
                // Regular chat message
                handleMessage(clientAddr, message);
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error handling packet", e);
        }
    }

    /**
     * Handle client join request.
     */
    private void handleJoin(ClientAddress clientAddr, String nickname) throws IOException {
        // Validate nickname
        if (nickname.isEmpty()) {
            sendToClient(clientAddr, "ERROR: Nickname cannot be empty");
            return;
        }

        if (nickname.length() > MAX_NICKNAME_LENGTH) {
            sendToClient(clientAddr, "ERROR: Nickname too long (max " + MAX_NICKNAME_LENGTH + ")");
            return;
        }

        if (!nickname.matches("[a-zA-Z0-9_-]+")) {
            sendToClient(clientAddr, "ERROR: Nickname must be alphanumeric");
            return;
        }

        // Check if client already registered
        if (clients.containsKey(clientAddr)) {
            sendToClient(clientAddr, "ERROR: You are already joined as " + clients.get(clientAddr).nickname);
            return;
        }

        // Check for nickname collision
        boolean nicknameTaken = clients.values().stream()
                .anyMatch(c -> c.nickname.equalsIgnoreCase(nickname));

        if (nicknameTaken) {
            sendToClient(clientAddr, "ERROR: Nickname '" + nickname + "' is already taken");
            return;
        }

        // Register client
        Client client = new Client(nickname, clientAddr.address, clientAddr.port);
        clients.put(clientAddr, client);

        LOGGER.info("Client joined: " + client);

        // Send confirmation to client
        sendToClient(clientAddr, "OK: Joined as " + nickname);

        // Broadcast to all clients
        broadcast(nickname + " has joined the chat", null);
    }

    /**
     * Handle client quit request.
     */
    private void handleQuit(ClientAddress clientAddr) throws IOException {
        Client client = clients.remove(clientAddr);

        if (client != null) {
            LOGGER.info("Client quit: " + client);
            sendToClient(clientAddr, "OK: Goodbye");
            broadcast(client.nickname + " has left the chat", clientAddr);
        } else {
            sendToClient(clientAddr, "ERROR: You are not registered");
        }
    }

    /**
     * Handle regular chat message.
     */
    private void handleMessage(ClientAddress clientAddr, String message) throws IOException {
        Client client = clients.get(clientAddr);

        if (client == null) {
            sendToClient(clientAddr, "ERROR: You must JOIN before sending messages");
            return;
        }

        // Broadcast message with sender's nickname
        String formattedMessage = client.nickname + ": " + message;
        broadcast(formattedMessage, null);

        LOGGER.fine("Broadcasted: " + formattedMessage);
    }

    /**
     * Send message to specific client.
     */
    private void sendToClient(ClientAddress clientAddr, String message) throws IOException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        // Check message size
        if (data.length > PACKET_SIZE) {
            LOGGER.warning("Message too large to send: " + data.length + " bytes");
            data = "ERROR: Message too large".getBytes(StandardCharsets.UTF_8);
        }

        DatagramPacket packet = new DatagramPacket(
                data, data.length,
                clientAddr.address, clientAddr.port
        );

        serverSocket.send(packet);
    }

    /**
     * Broadcast message to all clients except excludeAddr.
     */
    private void broadcast(String message, ClientAddress excludeAddr) {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        if (data.length > PACKET_SIZE) {
            LOGGER.warning("Broadcast message too large, truncating");
            data = Arrays.copyOf(data, PACKET_SIZE);
        }

        // Send to all clients
        for (Map.Entry<ClientAddress, Client> entry : clients.entrySet()) {
            ClientAddress addr = entry.getKey();

            // Skip excluded address
            if (excludeAddr != null && addr.equals(excludeAddr)) {
                continue;
            }

            try {
                DatagramPacket packet = new DatagramPacket(
                        data, data.length,
                        addr.address, addr.port
                );
                serverSocket.send(packet);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to send to " + entry.getValue(), e);
            }
        }
    }

    /**
     * Gracefully shutdown the server.
     */
    @Override
    public void close() {
        LOGGER.info("Shutting down ChatServer...");

        running = false;

        try {
            // Notify all clients
            broadcast("Server is shutting down", null);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error notifying clients of shutdown", e);
        }

        // Close socket (interrupts receive())
        serverSocket.close();

        // Shutdown executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        LOGGER.info("ChatServer shutdown complete");
    }

    /**
     * Get current client count (for monitoring).
     */
    public int getClientCount() {
        return clients.size();
    }

    /**
     * Main method for demonstration.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("USAGE: java ChatServer <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        try (ChatServer server = new ChatServer(port)) {
            // Add shutdown hook for graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutdown hook triggered");
                server.close();
            }));

            LOGGER.info("ChatServer running. Press Ctrl+C to stop.");

            // Keep main thread alive
            Thread.sleep(Long.MAX_VALUE);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start server", e);
            System.exit(1);
        } catch (InterruptedException e) {
            LOGGER.info("Server interrupted");
        }
    }
}