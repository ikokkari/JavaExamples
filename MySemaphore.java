import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A counting semaphore built from a {@link Lock} and {@link Condition}.
 * <p>
 * A standard exercise in concurrency is to prove that semaphores, locks,
 * and condition variables are equivalent in expressive power by implementing
 * each in terms of the others. This class implements a counting semaphore
 * on top of {@link ReentrantLock} and its {@link Condition}.
 * <p>
 * The core invariant: {@code permits} tracks how many more {@code acquire()}
 * calls can succeed without blocking. When it drops to zero, further callers
 * park on the condition variable until some other thread calls {@code release()}.
 *
 * <h3>Puzzles for students</h3>
 * <ol>
 *   <li>Assuming the lock is FIFO-fair, is this semaphore also FIFO — i.e.,
 *       are threads guaranteed to acquire permits in the order they called
 *       {@code acquire()}? (Hint: think about what happens between
 *       {@code await()} returning and the {@code while} re-check.)</li>
 *   <li>What goes wrong if you remove the lock operations from
 *       {@code release()}? Construct a concrete interleaving where the
 *       semaphore breaks.</li>
 *   <li>Why does {@code acquire()} use a {@code while} loop instead of
 *       a simple {@code if}? (Hint: spurious wakeups, and also the
 *       "stolen wakeup" when another thread acquires first.)</li>
 * </ol>
 *
 * @author Ilkka Kokkarinen
 */
public class MySemaphore {

    private int permits;
    private final Lock mutex;
    private final Condition permitAvailable;

    /**
     * Create a semaphore with the given number of initial permits.
     *
     * @param permits the initial permit count (may be zero)
     * @param fair    if {@code true}, the underlying lock uses a FIFO
     *                queue; if {@code false}, threads may acquire out of
     *                order (but with higher throughput)
     * @throws IllegalArgumentException if permits is negative
     */
    public MySemaphore(int permits, boolean fair) {
        if (permits < 0) {
            throw new IllegalArgumentException(
                    "Initial permits must be non-negative: " + permits);
        }
        this.permits = permits;
        this.mutex = new ReentrantLock(fair);
        this.permitAvailable = mutex.newCondition();
    }

    /**
     * Acquire one permit, blocking until one is available.
     *
     * @throws InterruptedException if the calling thread is interrupted
     *                              while waiting for a permit
     */
    public void acquire() throws InterruptedException {
        mutex.lock();
        try {
            // The while loop is essential, not just defensive:
            //   1. Condition.await() may return spuriously.
            //   2. Between the signal and re-acquiring the lock, another
            //      thread might have grabbed the permit ("stolen wakeup").
            while (permits < 1) {
                permitAvailable.await();
            }
            permits--;
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Release one permit, waking one waiting thread if any.
     * <p>
     * Note: the lock and try/finally are required here too — without
     * them, the increment of {@code permits} and the {@code signal()}
     * are not atomic with respect to {@code acquire()}, and a thread
     * can observe a stale value or miss the signal entirely.
     */
    public void release() {
        mutex.lock();
        try {
            permits++;
            // Signal (not signalAll): exactly one waiter can use the
            // permit we just added, so waking one is sufficient and
            // avoids a thundering-herd of threads all re-checking the
            // condition only for all but one to go back to sleep.
            permitAvailable.signal();
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Return the current number of available permits.
     * This is a snapshot — by the time the caller acts on it,
     * the value may already have changed.
     *
     * @return the current permit count
     */
    public int availablePermits() {
        mutex.lock();
        try {
            return permits;
        } finally {
            mutex.unlock();
        }
    }

    // -----------------------------------------------------------------------
    // Demo: a bounded resource pool using the semaphore.
    // -----------------------------------------------------------------------

    /**
     * Simulate 8 workers competing for 3 permits. Each worker acquires
     * a permit, holds it for a random interval, then releases it.
     * The output shows the interleaving and confirms that no more than
     * 3 workers are ever "inside" simultaneously.
     */
    public static void main(String[] args) throws InterruptedException {
        final int PERMITS = 3;
        final int WORKERS = 8;
        var semaphore = new MySemaphore(PERMITS, true);

        // Use virtual threads — lightweight, perfect for I/O-bound or
        // wait-heavy tasks, and available since Java 21.
        var threads = new Thread[WORKERS];
        for (int i = 0; i < WORKERS; i++) {
            final int id = i;
            threads[i] = Thread.ofVirtual().name("worker-" + id).start(() -> {
                try {
                    for (int round = 0; round < 3; round++) {
                        semaphore.acquire();
                        System.out.printf("[%s] acquired (permits left: %d)%n",
                                Thread.currentThread().getName(),
                                semaphore.availablePermits());
                        // Simulate holding the resource.
                        Thread.sleep((long) (Math.random() * 100));
                        semaphore.release();
                        System.out.printf("[%s] released%n",
                                Thread.currentThread().getName());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        for (var t : threads) { t.join(); }
        System.out.println("All workers finished. Final permits: "
                + semaphore.availablePermits());
    }
}
