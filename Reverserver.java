import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Reverserver {
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    public Reverserver(int serverPort) throws IOException{
        serverSocket = new ServerSocket(serverPort);
        serverSocket.setSoTimeout(5000); // finish up, if no new clients come in 5 seconds
        executorService.submit(() -> {
            try {
                while(running) {
                    Socket socket = serverSocket.accept();
                    // Generally we should launch a new thread the serve the client, so that
                    // other clients can come in while we are serving this one.
                    executorService.submit(() -> {
                        // Java try-with-resources
                        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))
                        ) {
                            String line = input.readLine();
                            while(line != null && line.length() > 0) {
                                output.println(new StringBuilder(line).reverse());
                                output.flush();
                                line = input.readLine();
                            }
                        }
                        catch(IOException ignored) { }
                        // finally-block is automatically synthesized with try-with-resources
                    });
                }
            }
            catch(SocketTimeoutException e) { System.out.println("Server timeout, closing up the shop"); }
            catch(IOException e) { System.out.println("Reverserver error: " + e); }
            finally { // after exiting the while loop
                Reverserver.this.terminate();
                System.out.println("Reverserver closed, good night");
            }
        });
    }
    
    public void terminate() {
        try {
            running = false;
            serverSocket.close();
            executorService.shutdownNow();
            System.out.println("Reverserver terminated");
        } catch(IOException ignored) {}
    }
    
    // A main method for demonstration purposes.
    public static void main(String[] args) {
        try {
            final int serverPort = 7777;
            new Reverserver(serverPort);
            Socket s = new Socket(InetAddress.getLocalHost(), serverPort);
            PrintWriter output = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
            BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
            output.println("Reverse this!");
            output.flush();
            System.out.println(input.readLine());
            output.println("Here's another line for you to reverse");
            output.flush();
            System.out.println(input.readLine());
            output.println(""); // Tell the reverserver to close the connection with this client
            s.close();
        } catch(Exception ignored) {}
    }
}