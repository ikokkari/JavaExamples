import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ConcurrencyDemo {

    // An inner class for a Runnable task that we use in the next examples. The task
    // simply first waits a random time and then terminates.
      
    private static class SimpleWaiter implements Runnable {
        private static final AtomicInteger ticket_machine = new AtomicInteger(0);
        private final int id;
        public SimpleWaiter() {
            this.id = ticket_machine.incrementAndGet();
        }
        @Override public void run() {
            System.out.println("Starting waiter #" + id + ".");
            try {
                // Sleep between 2 and 4 seconds
                Thread.sleep(ThreadLocalRandom.current().nextInt(2000) + 2000);
            }
            catch(InterruptedException ignored) { }
            System.out.println("Finishing waiter #" + id + ".");
        }
    }
    
    // Demonstrate how to create and launch new background Threads.
    public static void simpleThreadDemo(int n) {
        for(int i = 0; i < n; i++) {
            Thread t = new Thread(new SimpleWaiter());
            t.start();
        }
        System.out.println("All waiters created, returning from method.");
    }

    // The previous example implemented using an ExecutorService instead of explicitly
    // creating threads. See what happens if you set this up with different argument.
    private static final ExecutorService es = Executors.newFixedThreadPool(5);
    
    public static void simpleExecutorDemo(int n) { // Try n = 2, 5, 10.
        for(int i = 0; i < n; i++) {
            es.submit(new SimpleWaiter());
        }
        System.out.println("All waiters submitted, returning from method.");
    }
    
}