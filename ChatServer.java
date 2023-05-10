import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private static final int PACKET_SIZE = 512;
    private final List<Client> clients = new ArrayList<>();
    private volatile boolean running = true;
    private DatagramSocket serverSocket;
    private DatagramPacket receivedPacket;
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    // Objects of the inner class Client represent the registered clients in the chat.
    private class Client {
        public String nick; // Client's chosen nickname
        private final DatagramPacket clientPacket; // Client contact info

        public void sendMessage(String message) throws IOException {
            byte[] bytes = message.getBytes();
            clientPacket.setData(bytes);
            clientPacket.setLength(bytes.length);
            serverSocket.send(clientPacket);
        }

        public Client(DatagramPacket initialPacket, String nick) {
            this.nick = nick;
            this.clientPacket = new DatagramPacket(
                    new byte[PACKET_SIZE], PACKET_SIZE,
                    initialPacket.getAddress(), initialPacket.getPort()
            );
        }
    }
    
    public ChatServer(int serverPort) {
        try {
            serverSocket = new DatagramSocket(serverPort);
            serverSocket.setSoTimeout(60000);
        }
        catch(Exception e) {
            System.out.println("Error: Can't open ChatServer socket"); return;
        }        
        receivedPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        executorService.submit(new LogicThread());
    }

    public void sendMessageToAll(String message) throws IOException {
        for(Client client: clients) { client.sendMessage(message); }
    }

    // The background thread to implement the chat server's mainloop.
    private class LogicThread implements Runnable {
        public void run() {
            try {
                while(running) {
                    // Wait for the next incoming datagram.
                    serverSocket.receive(receivedPacket);
                    // Extract the message from the datagram.
                    byte[] bytes = receivedPacket.getData();
                    bytes = Arrays.copyOfRange(bytes, 0, receivedPacket.getLength());
                    String message = new String(bytes);
                    // Do what the message tells us to do.
                    if(message.startsWith("@JOIN")) {
                        Client client = new Client(receivedPacket, message.substring(5));
                        clients.add(client);
                        sendMessageToAll("New client " + client.nick + " has joined the chat");
                    }
                    else if(message.equals("@QUIT")) {
                        for(Client client: clients) { // who wants to quit?
                            if(client.clientPacket.getAddress().equals(receivedPacket.getAddress()) &&
                               client.clientPacket.getPort() == receivedPacket.getPort()) {
                                 clients.remove(client);
                                 sendMessageToAll(client.nick + " has quit the chat");
                                 break; // Can't continue iterating collection after mutation
                            }                        
                        }
                    }
                    else { sendMessageToAll(message); }
                }
            }
            catch(SocketTimeoutException e) {
                System.out.println("Socket timeout, closing server");
            }
            catch(IOException e) {
                System.out.println("ChatServer error: " + e );
            }
            finally {
                serverSocket.close();
                System.out.println("ChatServer terminated, good night");
                terminate();
            }
        }            
    }

    private void terminate() {
        running = false;
        executorService.shutdownNow();
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("USAGE: ChatServer port");
        }
        else {
            new ChatServer(Integer.parseInt(args[0]));
        }
    }
}