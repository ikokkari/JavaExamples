import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient extends JFrame {
    
    private static final int PACKET_SIZE = 512;
    private DatagramPacket receivedPacket;
    private DatagramPacket sentPacket;
    private DatagramSocket clientSocket;
    private volatile boolean running = true;
    private ExecutorService es = Executors.newFixedThreadPool(1);
    
    // The method to send a message to the server. 
    private void sendMessage(String message) {
        try {
            byte[] ba = message.getBytes();
            sentPacket.setData(ba);
            sentPacket.setLength(ba.length);
            clientSocket.send(sentPacket);
        } catch(IOException e) { System.out.println("ChatClient error: " + e); }        
    }
    
    public ChatClient(InetAddress address, int port, final String nick) {
        // First, set up the desired Swing interface.
        JPanel topPanel = new JPanel();        
        JButton quit = new JButton("Quit");
        topPanel.add(quit);
        final JTextField input = new JTextField(40);
        topPanel.add(input);
        this.add(topPanel, BorderLayout.NORTH);
        final JTextArea output = new JTextArea();
        JScrollPane sb = new JScrollPane(output);
        sb.setPreferredSize(new Dimension(500,300));
        this.add(sb, BorderLayout.CENTER);
        this.setTitle("Chat for " + nick);
        this.pack();
        this.setVisible(true);
        
        // Next, the client itself.
        try {
            clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(1000);
            receivedPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
            sentPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE, address, port);
        } catch(Exception e) { 
            System.out.println("ChatClient error: " + e);
            return;
        }
        
        sendMessage("@JOIN" + nick);
        
        input.addActionListener(ae -> {
            sendMessage(nick + ":" + input.getText());
            input.setText("");
        });
        
        quit.addActionListener(ae -> {
            sendMessage("@QUIT");
            ChatClient.this.terminate();
        });
        
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                sendMessage("@QUIT");
                ChatClient.this.terminate();
            }
        });
        
        /* A background thread to listen to the messages coming from the Server. */
        es.submit(() -> {
            try {
                while(running) {
                    try {
                        // Wait for the next incoming datagram.
                        clientSocket.receive(receivedPacket);
                        // Extract the message from the datagram.
                        byte[] bytes = receivedPacket.getData();
                        bytes = java.util.Arrays.copyOfRange(bytes, 0, receivedPacket.getLength());
                        String message = new String(bytes);
                        output.append(message + "\n");
                    }
                    catch(SocketTimeoutException e) { /* No message this time */ }
                }
            }
            catch(IOException e) {
                System.out.println("ChatClient error: " + e);
            }
            finally {
                clientSocket.close();
                System.out.println("ChatClient terminated");
            }
        });
    }
    
    public void terminate() {
        running = false;
        es.shutdownNow();
        this.dispose();
    }
    
    // For demonstration purposes.
    public static void launch(int port, String nick) throws Exception {
        new ChatClient(InetAddress.getLocalHost(), port, nick);   
    }

    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println("Usage: ChatClient port nick");
        }
        else {
            launch(Integer.parseInt(args[0]), args[1]);
        }
    }
}