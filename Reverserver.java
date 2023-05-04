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
    public Reverserver(int port) throws IOException{
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(5000); // finish up, if no new clients come in 5 seconds
        executorService.submit(() -> {
            try {
                while(running) {
                    final Socket socket = serverSocket.accept();
                    // Generally we should launch a new thread the serve the client, so that
                    // other clients can come in while we are serving this one.
                    executorService.submit(() -> {
                        BufferedReader input = null;
                        PrintWriter output = null;
                        try {
                            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                            String line = input.readLine();
                            while(line != null && line.length() > 0) {
                                output.println(new StringBuilder(line).reverse());
                                output.flush();
                                line = input.readLine();
                            }
                        }
                        catch(IOException ignored) { }
                        finally {
                            try { if(input != null) input.close(); } catch(IOException ignored) { }
                            output.close();
                            try { if(socket != null) socket.close(); } catch(IOException ignored) { }
                        }
                    });
                }
            }
            catch(SocketTimeoutException e) { System.out.println("Server timeout, closing up the shop"); }
            catch(IOException e) { System.out.println("Reverserver error: " + e); }
            finally { // after exiting the while loop
                try {
                    serverSocket.close();
                    System.out.println("Reverserver closed, good night");
                } catch(IOException ignored) { }
            }
        });
    }
    
    public void terminate() {
        running = false;
        executorService.shutdownNow();
        System.out.println("Reverserver terminated");
    }
    
    // A main method for demonstration purposes.
    public static void main(String[] args) {
        try {
            final int port = 7777;
            Reverserver reverserver = new Reverserver(port);
            Socket s = new Socket(InetAddress.getLocalHost(), port);
            PrintWriter output = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
            BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
            output.println("Reverse this!"); output.flush();
            System.out.println(input.readLine());
            output.println("Here's another line for you to reverse"); output.flush();
            System.out.println(input.readLine());
            output.println(""); // to tell the reverserver to close the connection with this client
            s.close();
            reverserver.terminate();
        } catch(Exception ignored) {}
    }
}