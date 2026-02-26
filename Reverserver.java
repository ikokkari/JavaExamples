import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * Modern TCP echo server that reverses lines sent by clients.
 *
 * Key improvements over legacy version:
 * - Proper resource management (no leaks)
 * - Explicit UTF-8 encoding (cross-platform compatibility)
 * - Virtual threads for scalability (Java 21+)
 * - Comprehensive error handling and logging
 * - Input validation (DoS protection)
 * - Graceful shutdown
 * - Explicit protocol with QUIT command
 * - Testable design (AutoCloseable, port discovery)
 *
 * For educational use in computer networks course.
 */
public class Reverserver implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(Reverserver.class.getName());
    private static final int MAX_LINE_LENGTH = 8192; // Prevent DoS via huge lines

    private final ServerSocket serverSocket;
    private final ExecutorService acceptorExecutor;
    private final ExecutorService clientExecutor;
    private volatile boolean running = true;

    /**
     * Creates a reverse server on the specified port.
     *
     * @param port The port to listen on (use 0 for random available port)
     * @throws IOException if the server socket cannot be created
     */
    public Reverserver(int port) throws IOException {
        // Create server socket - will throw IOException if port unavailable
        this.serverSocket = new ServerSocket(port);

        // Separate executor for accept loop (single thread)
        this.acceptorExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Reverserver-Acceptor");
            t.setDaemon(true); // Don't prevent JVM shutdown
            return t;
        });

        // Virtual threads for client handlers - scales to thousands of connections
        // For Java < 21, use: Executors.newCachedThreadPool()
        this.clientExecutor = Executors.newVirtualThreadPerTaskExecutor();

        // Start accepting connections
        acceptorExecutor.submit(this::acceptLoop);

        LOGGER.info("Reverserver started on port " + getPort());
    }

    /**
     * Returns the actual port the server is listening on.
     * Useful when port 0 was specified (random port).
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * Main accept loop - runs in dedicated thread.
     * Accepts incoming connections and spawns handler threads.
     */
    private void acceptLoop() {
        while (running) {
            try {
                // Blocks until client connects (or socket closed)
                Socket clientSocket = serverSocket.accept();

                // Log connection (useful for monitoring)
                LOGGER.info("Client connected from " + clientSocket.getRemoteSocketAddress());

                // Handle client in separate thread
                clientExecutor.submit(() -> handleClient(clientSocket));

            } catch (IOException e) {
                // Only log if this is unexpected (not during shutdown)
                if (running) {
                    LOGGER.log(Level.SEVERE, "Error accepting connection", e);
                }
                // If we're shutting down, this is expected - exit loop quietly
            }
        }
        LOGGER.info("Accept loop terminated");
    }

    /**
     * Handles a single client connection.
     *
     * Protocol:
     * - Client sends lines of text
     * - Server responds with reversed line
     * - Client sends empty line or "QUIT" to disconnect
     * - Client sends line > MAX_LINE_LENGTH: error and disconnect
     *
     * @param socket The client socket (will be closed when done)
     */
    private void handleClient(Socket socket) {
        // Use try-with-resources to ensure ALL resources are closed
        // Order matters: socket must be outermost to ensure it's closed last
        try (socket; // Java 9+ allows this syntax
             BufferedReader input = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter output = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                     true) // auto-flush on println()
        ) {

            // Set socket timeout to prevent hanging on slow/malicious clients
            socket.setSoTimeout(30000); // 30 second timeout for reads

            String line;
            int linesProcessed = 0;

            // Read lines until client disconnects or sends termination signal
            while ((line = input.readLine()) != null) {

                // Check for explicit termination command
                if (line.equals("QUIT")) {
                    LOGGER.fine("Client sent QUIT command");
                    output.println("BYE");
                    break;
                }

                // Check for empty line (legacy termination method)
                if (line.isEmpty()) {
                    LOGGER.fine("Client sent empty line, terminating");
                    break;
                }

                // Validate input length (DoS protection)
                if (line.length() > MAX_LINE_LENGTH) {
                    LOGGER.warning("Client sent oversized line (" + line.length() + " chars)");
                    output.println("ERROR: Line too long (max " + MAX_LINE_LENGTH + " characters)");
                    break;
                }

                // Core functionality: reverse the line
                String reversed = new StringBuilder(line).reverse().toString();
                output.println(reversed);

                linesProcessed++;
            }

            LOGGER.info("Client disconnected. Processed " + linesProcessed + " lines.");

        } catch (SocketTimeoutException e) {
            LOGGER.warning("Client timed out after inactivity");
        } catch (IOException e) {
            // Log unexpected I/O errors
            LOGGER.log(Level.WARNING, "Error handling client", e);
        } catch (Exception e) {
            // Catch-all for unexpected errors (shouldn't happen, but defensive)
            LOGGER.log(Level.SEVERE, "Unexpected error in client handler", e);
        }
        // Socket and streams automatically closed by try-with-resources
    }

    /**
     * Gracefully shuts down the server.
     *
     * 1. Stops accepting new connections
     * 2. Waits for existing clients to finish (up to 30 seconds)
     * 3. Forces termination of remaining clients if necessary
     */
    @Override
    public void close() throws IOException {
        LOGGER.info("Shutting down Reverserver...");

        // Signal accept loop to stop
        running = false;

        // Close server socket - causes accept() to throw IOException
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing server socket", e);
        }

        // Shutdown accept loop executor
        shutdownExecutor(acceptorExecutor, "acceptor", 5);

        // Gracefully shutdown client handlers
        shutdownExecutor(clientExecutor, "client handlers", 30);

        LOGGER.info("Reverserver shutdown complete");
    }

    /**
     * Helper method to gracefully shutdown an executor service.
     * Attempts graceful shutdown, then forces if necessary.
     */
    private void shutdownExecutor(ExecutorService executor, String name, int timeoutSeconds) {
        executor.shutdown(); // Prevent new tasks, allow existing to complete

        try {
            // Wait for existing tasks to finish
            if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                LOGGER.warning(name + " did not terminate gracefully, forcing shutdown");
                executor.shutdownNow(); // Force termination

                // Wait a bit more for forced termination
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.severe(name + " did not terminate even after forced shutdown");
                }
            } else {
                LOGGER.info(name + " terminated gracefully");
            }
        } catch (InterruptedException e) {
            LOGGER.warning(name + " shutdown interrupted");
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // Restore interrupt status
        }
    }

    /**
     * Demonstration main method showing server usage.
     *
     * Creates server, connects client, sends some lines, disconnects.
     */
    public static void main(String[] args) {
        // Configure logging to show INFO and above
        Logger.getLogger("").setLevel(Level.INFO);

        try {
            final int serverPort = 7777;

            // Create server (try-with-resources ensures cleanup)
            try (Reverserver server = new Reverserver(serverPort)) {

                // Give server time to start (not necessary in production, but helps demo)
                Thread.sleep(100);

                // Create client connection
                try (Socket clientSocket = new Socket(InetAddress.getLoopbackAddress(), serverPort);
                     PrintWriter output = new PrintWriter(
                             new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8),
                             true); // auto-flush
                     BufferedReader input = new BufferedReader(
                             new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8))
                ) {

                    // Test 1: Basic reversal
                    System.out.println("=== Test 1: Basic reversal ===");
                    output.println("Hello, World!");
                    System.out.println("Sent: Hello, World!");
                    System.out.println("Received: " + input.readLine());

                    // Test 2: Unicode support
                    System.out.println("\n=== Test 2: Unicode support ===");
                    output.println("Héllo 世界! 🌍");
                    System.out.println("Sent: Héllo 世界! 🌍");
                    System.out.println("Received: " + input.readLine());

                    // Test 3: Multiple lines
                    System.out.println("\n=== Test 3: Multiple lines ===");
                    String[] testLines = {
                            "The quick brown fox",
                            "jumps over the lazy dog",
                            "TCP is reliable!"
                    };

                    for (String line : testLines) {
                        output.println(line);
                        System.out.println("Sent: " + line);
                        System.out.println("Received: " + input.readLine());
                    }

                    // Test 4: Graceful disconnect
                    System.out.println("\n=== Test 4: Graceful disconnect ===");
                    output.println("QUIT");
                    System.out.println("Sent: QUIT");
                    System.out.println("Received: " + input.readLine()); // Should get "BYE"

                } // Client resources auto-closed

                System.out.println("\n=== Client disconnected ===");

                // Keep server running briefly to see logs
                Thread.sleep(1000);

            } // Server auto-closed via AutoCloseable

            System.out.println("\n=== Server shutdown complete ===");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in main", e);
            System.exit(1);
        }
    }
}