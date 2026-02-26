import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrate concurrency control mechanisms in java.util.concurrent using the
 * worker task of finding large probable prime numbers. Six different approaches
 * are shown, from low-level Semaphores to high-level CompletableFutures and
 * virtual threads. Updated for Java 21+.
 *
 * @author Ilkka Kokkarinen
 */
public class BigPrimes {

    // -----------------------------------------------------------------------
    // THE WORKER TASK
    // -----------------------------------------------------------------------

    /** Callback that each PrimeFinder task invokes after finding its prime. */
    @FunctionalInterface  // Marks this as usable with lambdas.
    private interface FinalCall {
        void finish(BigInteger prime) throws InterruptedException;
    }

    /** A Callable task that finds one random probable prime of given bit length. */
    private static class PrimeFinder implements Callable<BigInteger> {
        private static final AtomicInteger TICKET_MACHINE = new AtomicInteger(0);
        private final int id;
        private final int bits;
        private final FinalCall done;

        public PrimeFinder(int bits, FinalCall done) {
            this.id = TICKET_MACHINE.getAndIncrement();
            this.bits = bits;
            this.done = done;
        }

        public PrimeFinder(int bits) { this(bits, null); }

        @Override
        public BigInteger call() throws InterruptedException {
            System.out.println("Starting PrimeFinder #" + id + ".");
            BigInteger prime = BigInteger.probablePrime(bits, ThreadLocalRandom.current());
            System.out.println("PrimeFinder #" + id + " found: " + primeString(prime));
            if (done != null) { done.finish(prime); }
            return prime;
        }
    }

    // -----------------------------------------------------------------------
    // TEMPLATE: collect n primes using different concurrency strategies
    // -----------------------------------------------------------------------

    public abstract static class PrimeCollector {
        /** Find n random primes of the given bit length. */
        public List<BigInteger> findPrimes(int n, int bits) throws InterruptedException {
            var result = new ArrayList<BigInteger>();
            collectPrimes(n, bits, result);
            assert result.size() == n;
            return result;
        }

        /** Subclasses override this to find primes using different control mechanisms. */
        protected abstract void collectPrimes(int n, int bits, List<BigInteger> result)
                throws InterruptedException;

        @Override public String toString() {
            // Strip the enclosing class prefix for cleaner output.
            return getClass().getSimpleName();
        }
    }

    // -----------------------------------------------------------------------
    // APPROACH 1: SEMAPHORE
    // -----------------------------------------------------------------------
    // Two semaphores: one for mutual exclusion (guarding the shared list),
    // one for waiting until all n tasks have completed.

    public static class PrimesUsingSemaphore extends PrimeCollector {
        @Override
        protected void collectPrimes(int n, int bits, List<BigInteger> result)
                throws InterruptedException {
            var mayModifyList = new Semaphore(1);
            // A semaphore initialized to -(n-1) will become available (reach 0)
            // only after n release() calls — one from each completed task.
            var allDone = new Semaphore(-n + 1);
            try (var pool = Executors.newFixedThreadPool(5)) {
                for (int i = 0; i < n; i++) {
                    pool.submit(new PrimeFinder(bits, prime -> {
                        try {
                            mayModifyList.acquire();
                            result.add(prime);
                        } finally {
                            mayModifyList.release();
                            allDone.release();
                        }
                    }));
                }
                allDone.acquire(); // Block until all n tasks have completed.
            }
        }
    }

    // -----------------------------------------------------------------------
    // APPROACH 2: FUTURE
    // -----------------------------------------------------------------------
    // Submit tasks, get Future tickets, then collect results. The simplest
    // approach when you just need to fan-out work and gather results.

    public static class PrimesUsingFuture extends PrimeCollector {
        @Override
        protected void collectPrimes(int n, int bits, List<BigInteger> result)
                throws InterruptedException {
            var futures = new ArrayList<Future<BigInteger>>();
            try (var pool = Executors.newFixedThreadPool(5)) {
                for (int i = 0; i < n; i++) {
                    futures.add(pool.submit(new PrimeFinder(bits)));
                }
                for (Future<BigInteger> f : futures) {
                    try {
                        result.add(f.get()); // Blocks until this particular task completes.
                    } catch (ExecutionException ignored) { }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // APPROACH 3: COUNTDOWN LATCH
    // -----------------------------------------------------------------------
    // A latch initialized to n; each worker counts it down by one. The main
    // thread awaits until the count reaches zero.

    public static class PrimesUsingCountDownLatch extends PrimeCollector {
        @Override
        protected void collectPrimes(int n, int bits, List<BigInteger> result)
                throws InterruptedException {
            // Decorate the list for thread-safe mutation from worker threads.
            List<BigInteger> safeResult = Collections.synchronizedList(result);
            var allDone = new CountDownLatch(n);
            try (var pool = Executors.newFixedThreadPool(5)) {
                for (int i = 0; i < n; i++) {
                    pool.submit(new PrimeFinder(bits, prime -> {
                        safeResult.add(prime);
                        allDone.countDown();
                    }));
                }
                allDone.await();
            }
        }
    }

    // -----------------------------------------------------------------------
    // APPROACH 4: BLOCKING QUEUE
    // -----------------------------------------------------------------------
    // Workers put() primes into a bounded queue; the main thread take()s them
    // out. Both operations block when the queue is full or empty, respectively.

    public static class PrimesUsingBlockingQueue extends PrimeCollector {
        @Override
        protected void collectPrimes(int n, int bits, List<BigInteger> result)
                throws InterruptedException {
            BlockingQueue<BigInteger> primes = new ArrayBlockingQueue<>(n);
            try (var pool = Executors.newFixedThreadPool(5)) {
                for (int i = 0; i < n; i++) {
                    pool.submit(new PrimeFinder(bits, primes::put));
                }
                for (int i = 0; i < n; i++) {
                    result.add(primes.take()); // Blocks until next prime is available.
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // APPROACH 5: COMPLETABLE FUTURE (Java 8+)
    // -----------------------------------------------------------------------
    // CompletableFuture is the "promise" of Java: a composable, chainable
    // representation of an async computation. Unlike plain Future, you can
    // attach callbacks (thenApply, thenAccept) and combine multiple futures
    // (allOf, anyOf) without blocking.

    public static class PrimesUsingCompletableFuture extends PrimeCollector {
        @Override
        protected void collectPrimes(int n, int bits, List<BigInteger> result)
                throws InterruptedException {
            List<BigInteger> safeResult = Collections.synchronizedList(result);
            // supplyAsync runs each task on the common ForkJoinPool.
            // We could pass a custom executor as second argument.
            var futures = new CompletableFuture<?>[n];
            for (int i = 0; i < n; i++) {
                final int taskId = i;
                futures[i] = CompletableFuture
                        .supplyAsync(() -> {
                            System.out.println("Starting CF PrimeFinder #" + taskId + ".");
                            var prime = BigInteger.probablePrime(bits, ThreadLocalRandom.current());
                            System.out.println("CF PrimeFinder #" + taskId + " found: "
                                    + primeString(prime));
                            return prime;
                        })
                        .thenAccept(safeResult::add);  // Chain: when done, add to result.
            }
            // CompletableFuture.allOf: returns a future that completes when ALL
            // of the given futures complete. join() blocks until that happens.
            CompletableFuture.allOf(futures).join();
        }
    }

    // -----------------------------------------------------------------------
    // APPROACH 6: VIRTUAL THREADS (Java 21)
    // -----------------------------------------------------------------------
    // With virtual threads, there's no need for a fixed pool size. The executor
    // creates one virtual thread per task — cheap enough to scale to millions.
    // The code is structurally identical to the Future approach, but with a
    // fundamentally different threading model underneath.

    public static class PrimesUsingVirtualThreads extends PrimeCollector {
        @Override
        protected void collectPrimes(int n, int bits, List<BigInteger> result)
                throws InterruptedException {
            var futures = new ArrayList<Future<BigInteger>>();
            // newVirtualThreadPerTaskExecutor: one virtual thread per submit().
            // No pool size to tune — virtual threads are JVM-managed and cheap.
            try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < n; i++) {
                    futures.add(pool.submit(new PrimeFinder(bits)));
                }
                for (Future<BigInteger> f : futures) {
                    try {
                        result.add(f.get());
                    } catch (ExecutionException ignored) { }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // UTILITY
    // -----------------------------------------------------------------------

    /** Truncate long prime numbers for readable output. */
    private static String primeString(BigInteger prime) {
        String s = prime.toString();
        if (s.length() < 50) return s;
        return s.substring(0, 20) + "[..." + (s.length() - 40) + " digits...]"
                + s.substring(s.length() - 20);
    }

    // -----------------------------------------------------------------------
    // MAIN: run all six approaches back to back
    // -----------------------------------------------------------------------

    public static void main(String[] args) throws InterruptedException {
        int count = 10;
        int bits = 1000;

        PrimeCollector[] collectors = {
                new PrimesUsingSemaphore(),
                new PrimesUsingFuture(),
                new PrimesUsingCountDownLatch(),
                new PrimesUsingBlockingQueue(),
                new PrimesUsingCompletableFuture(),
                new PrimesUsingVirtualThreads()
        };

        for (PrimeCollector collector : collectors) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Strategy: " + collector);
            System.out.println("=".repeat(60));

            var start = Instant.now();
            List<BigInteger> primes = collector.findPrimes(count, bits);
            var elapsed = Duration.between(start, Instant.now());

            System.out.println("Found " + primes.size() + " primes in "
                    + elapsed.toMillis() + " ms.");
        }
    }
}