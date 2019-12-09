import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*; // for AtomicInteger
import java.math.BigInteger;

// Demonstrate the java.util.concurrent utility classes Semaphore, Future, BlockingQueue
// and CountDownLatch for concurrency control, with demonstration worker task of finding
// a large prime number.

public class BigPrimes {
    
    // Since our primes will get rather long, prettify their printing.
    private static String primeString(BigInteger prime) {
        String s = prime.toString();
        if(s.length() < 50) { return s; }
        else {
            return s.substring(0, 20) + "[..." + (s.length() - 40) + " digits...]" 
            + s.substring(s.length() - 20);
        }
    }
    
    // Interface for the continuation that each PrimeFinder task calls after completion.
    private interface FinalCall {
        public void finish(BigInteger prime) throws InterruptedException;
    }
    
    /* A class representing the worker task of finding a random prime. */
    private static class PrimeFinder implements Callable<BigInteger> {
        private static AtomicInteger count = new AtomicInteger(0); // Thread-safe ID generator.
        private int id; // The numerical ID of this task.
        private int bits; // How many bits the prime number should contain.
        private FinalCall done; // Continuation to execute after finding the prime.
        public PrimeFinder(int bits, FinalCall done) {
            this.id = count.getAndIncrement();
            this.bits = bits;
            this.done = done;
        }
        public PrimeFinder(int bits) { this(bits, null); }
        public BigInteger call() throws InterruptedException {
            System.out.println("Starting PrimeFinder #" + id + ".");
            BigInteger prime = BigInteger.probablePrime(bits, ThreadLocalRandom.current());
            System.out.println("PrimeFinder #" + id + " found prime: " + primeString(prime));            
            // The work is completed, so call the continuation given to this task.
            if(done != null) { done.finish(prime); }
            return prime;
        }
    }
    
    /* Find n random primes, using a semaphore to wait until all tasks have finished. */
    public static List<BigInteger> findPrimesUsingSemaphore(int n, int bits) 
    throws InterruptedException {
        // The ExecutorService to manage the PrimeFinder tasks.
        ExecutorService es = Executors.newFixedThreadPool(5);
        // The list of prime numbers that we find.
        ArrayList<BigInteger> result = new ArrayList<BigInteger>();
        // The semaphore used for mutual exclusion to guard mutations of arraylist.
        Semaphore mayModifyList = new Semaphore(1);
        // The semaphore that this thread uses to wait for completion of n tasks.
        Semaphore allDone = new Semaphore(-n + 1);
        for(int i = 0; i < n; i++) {
            es.submit(new PrimeFinder(bits, prime -> { 
                try { mayModifyList.acquire(); result.add(prime); }
                finally { mayModifyList.release(); allDone.release(); } })
            );
        }
        allDone.acquire(); // blocks until all n started tasks have completed 
        es.shutdownNow(); // tell the ExecutorService to release its threads
        assert result.size() == n; return result;
    }
    
    /* Find n random primes, using Future tickets to wait until all tasks have finished. */
    public static List<BigInteger> findPrimesUsingFutures(int n, int bits)
    throws InterruptedException {
        // The ExecutorService to manage the PrimeFinder tasks.
        ExecutorService es = Executors.newFixedThreadPool(5);
        // The list of prime numbers that we find.
        ArrayList<BigInteger> result = new ArrayList<BigInteger>();
        // The list of Future tickets for our submitted PrimeFinder tasks.
        ArrayList<Future<BigInteger>> futures = new ArrayList<Future<BigInteger>>();        
        // Submit the individual tasks and store the Future tickets into the list.
        for(int i = 0; i < n; i++) {
            futures.add(es.submit(new PrimeFinder(bits))); // no continuation
        }        
        // Loop through the tickets one by one and collect the results.
        for(Future<BigInteger> f: futures) {
            try {
                result.add(f.get()); // blocks until that task is done
            } catch(ExecutionException e) { } // hey could happen...
        }
        es.shutdownNow(); // tell the ExecutorService to release its threads
        assert result.size() == n; return result;
    }       
        
    public static List<BigInteger> findPrimesUsingCountDownLatch(int n, int bits)
    throws InterruptedException {
        // The ExecutorService to manage the PrimeFinder tasks.
        ExecutorService es = Executors.newFixedThreadPool(5);
        // The list of prime numbers that we find.
        ArrayList<BigInteger> result = new ArrayList<BigInteger>();
        // Decorate the arraylist to make its mutations thread-safe.
        List<BigInteger> sResult = Collections.synchronizedList(result);
        // The CountdownLatch to make this thread wait until all workers are done.
        CountDownLatch allDone = new CountDownLatch(n);
        for(int i = 0; i < n; i++) {
            es.submit(new PrimeFinder(bits, prime -> { sResult.add(prime); allDone.countDown(); }));
        }
        allDone.await(); // blocks until all n started tasks have completed 
        es.shutdownNow(); // tell the ExecutorService to release its threads
        assert result.size() == n; return result;                
    }
    
    public static List<BigInteger> findPrimesUsingBlockingQueue(int n, int bits)
    throws InterruptedException {
        // The ExecutorService to manage the PrimeFinder tasks.
        ExecutorService es = Executors.newFixedThreadPool(5);
        // The list of prime numbers that we find.
        ArrayList<BigInteger> result = new ArrayList<BigInteger>();
        // The blocking queue in which the workers add the prime numbers they find. 
        BlockingQueue<BigInteger> primes = new ArrayBlockingQueue<BigInteger>(n);
        // For BlockingQueue, put and take methods are blocking.
        for(int i = 0; i < n; i++) {
            es.submit(new PrimeFinder(bits, prime -> primes.put(prime)));
        }
        // The blocking queue will block until next prime becomes available.
        for(int i = 0; i < n; i++) { result.add(primes.take()); }
        es.shutdownNow(); // tell the ExecutorService to release its threads
        assert result.size() == n; return result;                
    }
}