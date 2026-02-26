import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Introduction to concurrency in Java. Originally written when thread pools
 * seemed modern; updated for Java 21+ with virtual threads, thread builders,
 * and auto-closeable executor services.
 *
 * Virtual threads (Java 21, JEP 444) are the biggest change to Java concurrency
 * since the java.util.concurrent package was added in Java 5. They are lightweight
 * threads managed by the JVM rather than the OS, so you can create millions of
 * them without running out of memory or OS thread handles. They make the
 * "one thread per task" model practical again.
 *
 * @author Ilkka Kokkarinen
 */
public class ConcurrencyDemo {

    // -----------------------------------------------------------------------
    // A SIMPLE RUNNABLE TASK
    // -----------------------------------------------------------------------

    private static class SimpleWaiter implements Runnable {
        // AtomicInteger: a thread-safe counter. incrementAndGet() is guaranteed
        // atomic — no two waiters will ever get the same id, even if created
        // simultaneously from different threads.
        private static final AtomicInteger TICKET_MACHINE = new AtomicInteger(0);
        private final int id;

        public SimpleWaiter() {
            this.id = TICKET_MACHINE.incrementAndGet();
        }

        @Override
        public void run() {
            // Thread.currentThread() lets us inspect which thread is running us.
            String threadInfo = Thread.currentThread().isVirtual()
                    ? "virtual" : "platform";
            System.out.println("Starting waiter #" + id + " on " + threadInfo + " thread.");
            try {
                // Thread.sleep with Duration (Java 19+): more readable than
                // raw milliseconds, and interruptible as always.
                Thread.sleep(Duration.ofMillis(
                        ThreadLocalRandom.current().nextInt(2000) + 500));
            } catch (InterruptedException ignored) {
                // Runnable.run() cannot throw checked exceptions, so we're
                // forced into this classic antipattern. In real code, you'd
                // restore the interrupt flag: Thread.currentThread().interrupt();
            }
            System.out.println("Finishing waiter #" + id + ".");
        }
    }

    // -----------------------------------------------------------------------
    // DEMO 1: PLATFORM THREADS (the traditional approach)
    // -----------------------------------------------------------------------
    // Each task gets its own OS-level thread. Creating thousands of these is
    // expensive — each one allocates ~1 MB of stack space from the OS.

    public static void platformThreadDemo(int n) {
        System.out.println("=== Platform threads (" + n + " waiters) ===");
        for (int i = 0; i < n; i++) {
            // Thread.ofPlatform() (Java 21): the new builder API for creating
            // traditional OS threads. Replaces new Thread(runnable).
            Thread t = Thread.ofPlatform()
                    .name("waiter-", i)  // named threads make debugging easier
                    .start(new SimpleWaiter());
        }
        System.out.println("All platform waiters started.\n");
    }

    // -----------------------------------------------------------------------
    // DEMO 2: VIRTUAL THREADS (Java 21 — the big deal)
    // -----------------------------------------------------------------------
    // Virtual threads are lightweight, JVM-managed threads. They are cheap to
    // create (a few hundred bytes each), so the "one thread per task" model
    // becomes practical even for millions of concurrent tasks. Under the hood,
    // the JVM schedules virtual threads onto a small pool of carrier (platform)
    // threads using a work-stealing ForkJoinPool.

    public static void virtualThreadDemo(int n) {
        System.out.println("=== Virtual threads (" + n + " waiters) ===");
        for (int i = 0; i < n; i++) {
            // Thread.ofVirtual(): the builder for virtual threads.
            // Same Runnable, radically different resource cost.
            Thread.ofVirtual()
                    .name("v-waiter-", i)
                    .start(new SimpleWaiter());
        }
        System.out.println("All virtual waiters started.\n");
    }

    // -----------------------------------------------------------------------
    // DEMO 3: EXECUTOR SERVICES (traditional thread pool)
    // -----------------------------------------------------------------------
    // A fixed thread pool limits concurrency: at most 3 waiters run at once,
    // the rest queue up. Good when tasks are CPU-bound or you need backpressure.
    //
    // Since Java 19, ExecutorService implements AutoCloseable, so you can use
    // it in try-with-resources. close() calls shutdown() + awaitTermination()
    // automatically — no more forgetting to shut down the pool.

    public static void fixedPoolDemo(int n) {
        System.out.println("=== Fixed thread pool (3 threads, " + n + " waiters) ===");
        // try-with-resources on ExecutorService (Java 19+): the pool is
        // guaranteed to be shut down when the block exits.
        try (var pool = Executors.newFixedThreadPool(3)) {
            for (int i = 0; i < n; i++) {
                pool.submit(new SimpleWaiter());
            }
            System.out.println("All waiters submitted to fixed pool.");
        } // pool.close() is called here — blocks until all tasks finish.
        System.out.println("Fixed pool shut down.\n");
    }

    // -----------------------------------------------------------------------
    // DEMO 4: VIRTUAL THREAD EXECUTOR (Java 21 — the modern default)
    // -----------------------------------------------------------------------
    // This executor creates a new virtual thread for each submitted task.
    // There is no pool size to tune — virtual threads are so cheap that
    // pooling them would be pointless. This is the recommended approach
    // for I/O-bound workloads (web servers, database calls, file I/O).

    public static void virtualPoolDemo(int n) {
        System.out.println("=== Virtual thread executor (" + n + " waiters) ===");
        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < n; i++) {
                pool.submit(new SimpleWaiter());
            }
            System.out.println("All waiters submitted to virtual executor.");
        }
        System.out.println("Virtual executor shut down.\n");
    }

    // -----------------------------------------------------------------------
    // DEMO 5: HOW MANY VIRTUAL THREADS CAN YOU CREATE?
    // -----------------------------------------------------------------------
    // The answer: a lot. Try doing this with platform threads and watch your
    // OS run out of resources around 2,000–10,000 threads.

    public static void scalabilityDemo() {
        System.out.println("=== Scalability: creating 100,000 virtual threads ===");
        var start = Instant.now();
        // Each thread sleeps for 1 second. With platform threads, 100,000
        // simultaneous sleepers would need ~100 GB of stack space. With virtual
        // threads, the JVM handles it on a handful of carrier threads.
        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 100_000; i++) {
                pool.submit(() -> {
                    try { Thread.sleep(Duration.ofSeconds(1)); }
                    catch (InterruptedException ignored) { }
                });
            }
        } // blocks until all 100,000 threads complete
        var elapsed = Duration.between(start, Instant.now());
        System.out.println("100,000 virtual threads completed in " + elapsed.toMillis() + " ms.\n");
    }

    // -----------------------------------------------------------------------
    // MAIN
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        // Uncomment the demos you want to see. Each one is self-contained.

        platformThreadDemo(5);

        // Give them a moment to finish before the next demo's output interleaves.
        sleep(4000);

        virtualThreadDemo(5);
        sleep(4000);

        fixedPoolDemo(10);       // try-with-resources blocks until all done
        virtualPoolDemo(10);     // ditto

        scalabilityDemo();       // the mic drop
    }

    // A little helper to keep main readable.
    private static void sleep(long millis) {
        try { Thread.sleep(Duration.ofMillis(millis)); }
        catch (InterruptedException ignored) { }
    }
}