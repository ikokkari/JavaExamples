import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * Modern UDP-based chat client with Swing GUI.
 *
 * Compatible with modern ChatServer implementation.
 *
 * Features:
 * - Proper resource management
 * - Explicit UTF-8 encoding
 * - Error message handling from server
 * - Graceful shutdown
 * - Better logging
 * - Thread-safe GUI updates
 */
public class ChatClient extends JFrame implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(ChatClient.class.getName());
    private static final int PACKET_SIZE = 512;
    private static final int SOCKET_TIMEOUT_MS = 1000;

    private final DatagramSocket clientSocket;
    private final InetAddress serverAddress;
    private final int serverPort;
    private final String nickname;
    private final ExecutorService executor;
    private volatile boolean running = true;

    // GUI components
    private final JTextArea outputArea;
    private final JTextField inputField;

    /**
     * Creates a chat client connected to specified server.
     *
     * @param serverAddress Server IP address
     * @param serverPort Server port
     * @param nickname User's nickname
     * @throws IOException if socket creation fails
     */
    public ChatClient(InetAddress serverAddress, int serverPort, String nickname) throws IOException {
        super("Chat Client - " + nickname);

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.nickname = nickname;

        // Create UDP socket with timeout for receive operations
        this.clientSocket = new DatagramSocket();
        this.clientSocket.setSoTimeout(SOCKET_TIMEOUT_MS);

        // Single thread for receiving messages
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ChatClient-Receiver-" + nickname);
            t.setDaemon(false); // Want to receive remaining messages on shutdown
            return t;
        });

        // Build GUI
        this.outputArea = new JTextArea(20, 50);
        this.outputArea.setEditable(false);
        this.outputArea.setLineWrap(true);
        this.outputArea.setWrapStyleWord(true);

        this.inputField = new JTextField(40);

        initializeGUI();

        // Start receiving messages
        executor.submit(this::receiveLoop);

        // Send JOIN message to server
        sendToServer("@JOIN " + nickname);

        LOGGER.info("ChatClient created for " + nickname);
    }

    /**
     * Initialize Swing GUI components.
     */
    private void initializeGUI() {
        // Top panel with input field and buttons
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.add(new JLabel("Message:"), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        topPanel.add(inputPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> handleSendMessage());
        buttonPanel.add(sendButton);

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> handleQuit());
        buttonPanel.add(quitButton);

        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Output area with scroll pane
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        // Layout
        setLayout(new BorderLayout(5, 5));
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Status bar
        JLabel statusBar = new JLabel("Connected to " + serverAddress.getHostAddress() + ":" + serverPort);
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        add(statusBar, BorderLayout.SOUTH);

        // Input field action (Enter key sends message)
        inputField.addActionListener(e -> handleSendMessage());

        // Window closing handler
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleQuit();
            }
        });

        // Finalize window
        pack();
        setLocationRelativeTo(null); // Center on screen
        setVisible(true);

        // Focus on input field
        inputField.requestFocusInWindow();
    }

    /**
     * Handle send message action.
     */
    private void handleSendMessage() {
        String message = inputField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        // Don't allow messages that look like commands (user confusion)
        if (message.startsWith("@")) {
            appendToOutput("ERROR: Messages cannot start with '@' (reserved for commands)");
            return;
        }

        // Send to server (server will add nickname prefix)
        sendToServer(message);

        // Clear input field
        inputField.setText("");
        inputField.requestFocusInWindow();
    }

    /**
     * Handle quit action.
     */
    private void handleQuit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to quit?",
                "Confirm Quit",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            sendToServer("@QUIT");

            // Give server time to process QUIT
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            close();
        }
    }

    /**
     * Send message to server.
     */
    private void sendToServer(String message) {
        try {
            byte[] data = message.getBytes(StandardCharsets.UTF_8);

            // Check message size
            if (data.length > PACKET_SIZE) {
                appendToOutput("ERROR: Message too long (max " + PACKET_SIZE + " bytes)");
                LOGGER.warning("Message too long: " + data.length + " bytes");
                return;
            }

            DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    serverAddress, serverPort
            );

            clientSocket.send(packet);

            LOGGER.fine("Sent to server: " + message);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to send message", e);
            appendToOutput("ERROR: Failed to send message - " + e.getMessage());
        }
    }

    /**
     * Receive loop - runs in background thread.
     */
    private void receiveLoop() {
        byte[] buffer = new byte[PACKET_SIZE];

        LOGGER.info("Receive loop started");

        while (running) {
            try {
                // Create fresh packet for each receive
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Block waiting for packet (with timeout)
                clientSocket.receive(packet);

                // Extract message with explicit encoding
                byte[] data = java.util.Arrays.copyOfRange(
                        packet.getData(), 0, packet.getLength()
                );
                String message = new String(data, StandardCharsets.UTF_8);

                // Display message in GUI (thread-safe)
                appendToOutput(message);

                LOGGER.fine("Received: " + message);

                // Check for special server messages
                if (message.startsWith("OK:") || message.startsWith("ERROR:")) {
                    // Server response to our command
                    if (message.contains("Goodbye")) {
                        LOGGER.info("Server confirmed quit");
                    }
                }

            } catch (SocketTimeoutException e) {
                // No message received within timeout - this is normal, continue
            } catch (SocketException e) {
                // Socket closed (during shutdown) - expected
                if (running) {
                    LOGGER.log(Level.SEVERE, "Socket error", e);
                }
                break;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error receiving message", e);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error in receive loop", e);
            }
        }

        LOGGER.info("Receive loop terminated");
    }

    /**
     * Append message to output area (thread-safe).
     * Must be called from any thread.
     */
    private void appendToOutput(String message) {
        // Use SwingUtilities to ensure thread-safe GUI updates
        SwingUtilities.invokeLater(() -> {
            outputArea.append(message + "\n");

            // Auto-scroll to bottom
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    /**
     * Gracefully close the client.
     */
    @Override
    public void close() {
        if (!running) {
            return; // Already closing
        }

        LOGGER.info("Closing ChatClient for " + nickname);

        running = false;

        // Close socket (interrupts receive())
        clientSocket.close();

        // Shutdown executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Close window
        dispose();

        LOGGER.info("ChatClient closed");
    }

    /**
     * Launch a chat client.
     *
     * @param serverAddress Server IP address
     * @param serverPort Server port
     * @param nickname User nickname
     * @return ChatClient instance
     * @throws IOException if connection fails
     */
    public static ChatClient launch(InetAddress serverAddress, int serverPort, String nickname)
            throws IOException {
        return new ChatClient(serverAddress, serverPort, nickname);
    }

    /**
     * Launch a chat client connecting to localhost.
     */
    public static ChatClient launch(int serverPort, String nickname) throws IOException {
        return launch(InetAddress.getLoopbackAddress(), serverPort, nickname);
    }

    /**
     * Main method for standalone execution.
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setSystemLookAndFeel();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not set system look and feel", e);
        }

        if (args.length != 2) {
            System.err.println("USAGE: java ChatClient <port> <nickname>");
            System.exit(1);
        }

        try {
            int port = Integer.parseInt(args[0]);
            String nickname = args[1];

            // Validate nickname
            if (nickname.isEmpty() || nickname.length() > 20) {
                System.err.println("ERROR: Nickname must be 1-20 characters");
                System.exit(1);
            }

            if (!nickname.matches("[a-zA-Z0-9_-]+")) {
                System.err.println("ERROR: Nickname must be alphanumeric (plus _ and -)");
                System.exit(1);
            }

            // Launch client
            ChatClient client = launch(port, nickname);

            LOGGER.info("ChatClient launched successfully");

        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid port number");
            System.exit(1);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start client", e);
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to connect to server:\n" + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }
}