import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

// Demonstrate the java.util.concurrent utility classes Semaphore, Future, BlockingQueue
// and CountDownLatch for concurrency control, with demonstration worker task of finding
// a large prime number.

public class BigPrimes {

    // Interface for the continuation that each PrimeFinder task calls after completion.
    private interface FinalCall {
        void finish(BigInteger prime) throws InterruptedException;
    }

    /* A class representing the worker task of finding one random prime. */
    private static class PrimeFinder implements Callable<BigInteger> {
        private static final AtomicInteger ticket_machine = new AtomicInteger(0);
        private final int id; // The numerical ID of this task.
        private final int bits; // How many bits the prime number should contain.
        private final FinalCall done; // Continuation to execute after finding the prime.
        public PrimeFinder(int bits, FinalCall done) {
            this.id = ticket_machine.getAndIncrement();
            this.bits = bits;
            this.done = done;
        }
        public PrimeFinder(int bits) {
            this(bits, null);
        }
        public BigInteger call() throws InterruptedException {
            System.out.println("Starting PrimeFinder #" + id + ".");
            // The method BigInteger.probablePrime does all the heavy lifting for us.
            BigInteger prime = BigInteger.probablePrime(bits, ThreadLocalRandom.current());
            System.out.println("PrimeFinder #" + id + " found prime: " + primeString(prime));            
            // The work is completed, so call the continuation given to this task.
            if(done != null) {
                done.finish(prime);
            }
            return prime;
        }
    }

    // The ExecutorService to manage the PrimeFinder tasks.
    private static final ExecutorService es = Executors.newFixedThreadPool(5);

    // Template method superclass for finding n random primes of given bit length.
    public abstract static class PrimeCollector {
        public List<BigInteger> findPrimes(int n, int bits) throws InterruptedException {
            // The list of prime numbers collected.
            ArrayList<BigInteger> result = new ArrayList<>();
            // Pass the buck to the template method to do the actual work.
            collectPrimes(n, bits, result);
            // We are confident about this, putting our head on the chopping block.
            assert result.size() == n;
            // The result should contain n random prime numbers.
            return result;
        }
        // Subclasses override this template method to find the n primes in different ways.
        protected abstract void collectPrimes(int n, int bits, List<BigInteger> result)
        throws InterruptedException; 
    }

    // Find the primes using semaphores as control structure.
    public static class PrimesUsingSemaphore extends PrimeCollector {
        protected void collectPrimes(int n, int bits, List<BigInteger> result)
        throws InterruptedException {
            // The semaphore used for mutual exclusion to guard mutations of arraylist.
            Semaphore mayModifyList = new Semaphore(1);
            // The semaphore that this thread uses to wait for completion of n tasks.
            Semaphore allDone = new Semaphore(-n + 1);    
            for(int i = 0; i < n; i++) {
                es.submit(new PrimeFinder(bits, prime -> { 
                            try {
                                mayModifyList.acquire();
                                result.add(prime);
                            }
                            finally {
                                mayModifyList.release();
                                allDone.release();
                            } })
                );  
            }
            allDone.acquire(); // Block until all n started tasks have completed.
        }
    }

    public static class PrimesUsingFuture extends PrimeCollector {
        protected void collectPrimes(int n, int bits, List<BigInteger> result)
        throws InterruptedException {
            // The list of Future tickets for our submitted PrimeFinder tasks.
            ArrayList<Future<BigInteger>> futures = new ArrayList<>();
            // Submit the individual tasks and store the Future tickets into the list.
            for(int i = 0; i < n; i++) {
                futures.add(es.submit(new PrimeFinder(bits))); // (no continuation here)
            }        
            // Loop through the tickets one by one and collect the results.
            for(Future<BigInteger> f: futures) {
                try {
                    result.add(f.get()); // Blocks until the task is complete.
                } catch(ExecutionException ignored) { }
            }
        }
    }

    public static class PrimesUsingCountDownLatch extends PrimeCollector {
        protected void collectPrimes(int n, int bits, List<BigInteger> result)
        throws InterruptedException {
            // Let's decorate the given list to mutate it in a thread safe fashion.
            List<BigInteger> sResult = Collections.synchronizedList(result);
            // The CountdownLatch makes this thread wait until all workers are done.
            CountDownLatch allDone = new CountDownLatch(n);
            for(int i = 0; i < n; i++) {
                es.submit(new PrimeFinder(bits, prime -> {
                            sResult.add(prime);
                            allDone.countDown(); // Decrement the countdown by one.
                        }));
            }
            allDone.await(); // The Latch blocks until all n tasks have completed. 
        }
    }

    public static class PrimesUsingBlockingQueue extends PrimeCollector {
        protected void collectPrimes(int n, int bits, List<BigInteger> result)
        throws InterruptedException {
            // The blocking queue in which the workers add the prime numbers they find. 
            BlockingQueue<BigInteger> primes = new ArrayBlockingQueue<>(n);
            // For BlockingQueue, put and take methods are thread-safe.
            for(int i = 0; i < n; i++) {
                es.submit(new PrimeFinder(bits, primes::put));
            }
            // The blocking queue will block until next prime becomes available.
            for(int i = 0; i < n; i++) {
                result.add(primes.take());
            }
        }
    }

    // Since our primes will get rather long, prettify their printing.
    private static String primeString(BigInteger prime) {
        String s = prime.toString();
        if(s.length() < 50) { return s; }
        else {
            return s.substring(0, 20) + "[..." + (s.length() - 40) + " digits...]" 
            + s.substring(s.length() - 20);
        }
    }    
    
    public static void main(String[] args) throws InterruptedException {
        PrimeCollector collector = new PrimesUsingBlockingQueue();
        long startTime = System.currentTimeMillis();
        List<BigInteger> primes = collector.findPrimes(10, 1000);
        long endTime = System.currentTimeMillis();
        System.out.print("Found the primes in " + (endTime - startTime) + " ms. ");
        System.out.println("The primes found are: ");
        for(BigInteger p: primes) {
            System.out.println(primeString(p));
        }
    }

    // Shut down the executor service of this class.
    public static void shutdown() {
        es.shutdownNow();
    }
}